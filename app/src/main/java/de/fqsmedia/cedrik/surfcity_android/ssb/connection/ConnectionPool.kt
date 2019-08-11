package de.fqsmedia.cedrik.surfcity_android.ssb.connection

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.squareup.moshi.Moshi
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.ExtendedMessage
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.MessageContent
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.MessageModel
import de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories.FeedRepository
import de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories.MessageRepository
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCMessage
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCRequestQueue
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okio.ByteString
import java.util.*
import kotlin.collections.ArrayList

class ConnectionPool(
    val identity: Identity,
    private val networkIdentifier: ByteString = Constants.SSB_NETWORKIDENTIFIER,
    val context: Context
) {

    private val connectionPool: MutableList<PeerConnection> = ArrayList()
    private val moshi: Moshi = Constants.getMoshi()

    private val messageRepository = MessageRepository(context)
    private val feedRepository = FeedRepository(context)

    init {
        /*pubRepository.getAllPubs().observe(lifecycleOwner,
            Observer<List<Pub>> {
                    pubs -> for(pub in pubs) add(pub.host, pub.port, okio.ByteString.of(*Base64.decode(pub.pubkey.keyHash, Base64.NO_WRAP)))
            })*/
    }


    @SuppressLint("CheckResult")
    fun add(host: String, port: Int, remoteKey: ByteString) {
        for (connection in connectionPool) {
            if (connection.host == host) {
                return
            }
        }
        val peerConnection = PeerConnection(
            identity,
            networkIdentifier,
            remoteKey
        )
        val scheduler = Schedulers.newThread()
        peerConnection.connectToPeer(host, port)
            .subscribeOn(Schedulers.io())
            .observeOn(scheduler)
            .subscribe { success ->
                if (success) {
                    connectionPool.add(peerConnection)
                    Log.d("SYNC", "Connected to: $host")
                    val requestQueue = RPCRequestQueue(moshi)
                    peerConnection.openWriteThread()
                    peerConnection.listenToPeer()
                        .subscribeOn(Schedulers.io())
                        .observeOn(scheduler)
                        .subscribeBy(
                            onNext = { rpcMessage ->
                                Log.d("RESPONSE", rpcMessage.toString())
                                when {
                                    rpcMessage.endError -> {
                                        Log.d(
                                            "ENDERROR",
                                            moshi.adapter(Boolean::class.java).fromJson(rpcMessage.body.utf8()).toString()
                                        )
                                    }
                                    rpcMessage.requestNumber > 0 -> {
                                        requestQueue.add(rpcMessage)
                                        requestQueue.processRequest(peerConnection, context)
                                    }
                                    else -> {
                                        handleMessage(rpcMessage)
                                    }
                                }
                            },
                            onError = { throwable ->
                                throwable.printStackTrace()
                            })


                    peerConnection.createWants()
                }
            }
    }

    fun sync() {
        Log.d("Starting Sync for: ", identity.getString())
        val peers = feedRepository.getAllPeers()

        Log.d("Peers: ", "" + peers.size)
        Log.d("Connections: ", "" + connectionPool.size)
        for (connection in connectionPool) {
            connection.requestHistories(peers)
        }


    }


    fun closeAll() {
        for (connection in connectionPool) {
            connection.close()
        }
    }

    private fun handleMessage(message: RPCMessage) {
        val messageJson = message.body.utf8()

        if (messageJson.contains(".box") && !messageJson.contains("\"content\":{\"type")) {
            handlePrivateMessage(messageJson)
        } else {
            try {
                moshi.adapter(ExtendedMessage::class.java).fromJson(messageJson)?.let {
                    messageRepository.saveMessage(it)
                }
            } catch (e: Exception) {
                Log.d("NOT HANDLED MESSAGE:  ", message.body.utf8())
            }
        }


    }

    private fun handlePrivateMessage(message: String) {
        val parser = JsonParser()
        val element: JsonObject = parser.parse(message).asJsonObject.getAsJsonObject("value")
        Log.d("PRIVATE: ", message)
        var previous: String?
        try {
            previous = element.get("previous").asString
        } catch (e: UnsupportedOperationException) {
            previous = null
        }
        messageRepository.saveMessage(
            ExtendedMessage(
                MessageModel(
                    if (previous != null) RPCIdentifier.fromString(previous) else null,
                    RPCIdentifier.fromString(element.get("author").asString)!!,
                    element.get("sequence").asInt,
                    Date(element.get("timestamp").asLong),
                    element.get("hash").asString,
                    MessageContent.PrivateMessage(element.get("content").asString),
                    element.get("signature").asString
                )
            )
        )
    }
}



