package de.fqsmedia.cedrik.surfcity_android.ssb.connection

import android.util.Log
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Feed
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCMessage
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCProtocol
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCRequest
import de.fqsmedia.cedrik.surfcity_android.ssb.shs.SSBHandshakeClient
import de.fqsmedia.cedrik.surfcity_android.ssb.shs.SSBStream
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import okio.*
import java.io.Closeable
import java.io.IOException
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

class PeerConnection(
    ed25519Identity: Identity,
    networkIdentifier: ByteString = Constants.SSB_NETWORKIDENTIFIER,
    remoteKey: ByteString
) {

    private val ssbHandshakeClient = SSBHandshakeClient(ed25519Identity, remoteKey, networkIdentifier)

    private var socket: Socket? = null
    private var source: BufferedSource? = null
    private var sink: BufferedSink? = null
    private var ssbStream: SSBStream? = null
    private var clientToServerRequestNumber = 0
    var host = ""
    private val executor = Executors.newFixedThreadPool(4)!!
    val writeQueue = ConcurrentLinkedQueue<RPCMessage>()

    fun connectToPeer(host: String, port: Int): Observable<Boolean> {
        val handler: ObservableOnSubscribe<Boolean> = ObservableOnSubscribe { emitter: ObservableEmitter<Boolean> ->
            val future = executor.submit {
                emitter.onNext(start(host, port))
                emitter.onComplete()
            }
            emitter.setCancellable { future.cancel(false) }
        }

        this.host = host

        return Observable.create(handler)
    }

    fun listenToPeer(): Observable<RPCMessage> {
        val handler: ObservableOnSubscribe<RPCMessage> =
            ObservableOnSubscribe { emitter: ObservableEmitter<RPCMessage> ->
                val future = executor.submit {
                    socket?.run {
                        if (isConnected && ssbHandshakeClient.completed) {
                            readFromPeer(emitter)
                        }
                    }
                }
                emitter.setCancellable { future.cancel(false) }
            }
        return Observable.create(handler)
    }

    fun requestHistories(feedList: List<Feed>) {
        val moshi = Constants.getMoshi()
        val jsonAdapter = moshi.adapter(RPCRequest.RequestCreateHistoryStream::class.java)
        if (socket?.isConnected!! && ssbHandshakeClient.completed) {
            for (feed in feedList) {
                val createHistoryStream = RPCRequest.RequestCreateHistoryStream(
                    args = listOf(
                        RPCRequest.RequestCreateHistoryStream.Arg(
                            id = feed.pubkey,
                            sequence = feed.front_seq
                        )
                    )
                )
                val body = ByteString.of(*jsonAdapter.toJson(createHistoryStream).toByteArray())
                val rpcMessage = RPCMessage(
                    true,
                    false,
                    RPCProtocol.Companion.RPCBodyType.JSON,
                    body.size,
                    ++clientToServerRequestNumber,
                    body
                )
                writeQueue.add(rpcMessage)
            }
        }
    }

    fun createWants() {
        val moshi = Constants.getMoshi()
        val jsonAdapter = moshi.adapter(RPCRequest.RequestBlobsCreateWants::class.java)
        socket?.run {
            if (isConnected && ssbHandshakeClient.completed) {

                val createWants = RPCRequest.RequestBlobsCreateWants(
                    args = listOf()
                )
                val body = ByteString.of(*jsonAdapter.toJson(createWants).toByteArray())
                val rpcMessage = RPCMessage(
                    true,
                    false,
                    RPCProtocol.Companion.RPCBodyType.JSON,
                    body.size,
                    ++clientToServerRequestNumber,
                    body
                )
                writeQueue.add(rpcMessage)
            }
        }
    }

    fun openWriteThread() {
        executor.submit {
            while (true) {
                if (writeQueue.isNotEmpty()) {
                    try {
                        sink?.run {
                            val message = writeQueue.remove()
                            val rpcEncode = RPCProtocol.encode(message)
                            Log.d("writing", message.toString())
                            val boxStreamEncode = ssbStream!!.sendToServer(rpcEncode)
                            write(boxStreamEncode)
                            flush()
                        }
                    } catch (e: IOException) {
                        Log.e("SOCKET: ", e.message)
                        socket?.let {
                            closeQuietly(it)
                        }
                    }
                }
            }
        }
    }

    private fun readFromPeer(emitter: Emitter<RPCMessage>) {
        executor.submit {
            try {
                source?.run {
                    val rpcBuffer = Buffer()
                    var rpcExpectedLength = 0

                    var decoded = ssbStream?.readFromServer(this)
                    while (decoded != null) {
                        if (rpcBuffer.size == 0L)
                            rpcExpectedLength = RPCProtocol.getBodyLength(decoded) + RPCProtocol.HEADER_SIZE

                        rpcBuffer.write(decoded)
                        while (rpcExpectedLength != 0 && rpcBuffer.size >= (rpcExpectedLength.toLong())) {
                            emitter.onNext(
                                RPCProtocol.decode(
                                    rpcBuffer.readByteString(rpcExpectedLength.toLong())
                                )
                            )
                            rpcExpectedLength =
                                if (rpcBuffer.size >= RPCProtocol.HEADER_SIZE)
                                    RPCProtocol.getBodyLength(rpcBuffer.peek().readByteString()) + RPCProtocol.HEADER_SIZE
                                else
                                    0
                        }
                        decoded = ssbStream?.readFromServer(this)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                socket?.let {
                    closeQuietly(it)
                }
            }
        }
    }

    private fun performHandshake(): Boolean {
        val buffer = Buffer()
        sink?.run {
            write(ssbHandshakeClient.helloMessage())
            flush()
            source?.run {
                var byteCount = read(buffer, 8192L)
                val serverHello = buffer.readByteString(byteCount)
                if (byteCount != -1L && ssbHandshakeClient.isHelloMessage(serverHello))
                    write(ssbHandshakeClient.authenticateMessage())
                flush()

                byteCount = read(buffer, 8192L)
                val acceptResponse = buffer.readByteString(byteCount)
                if (byteCount != -1L && ssbHandshakeClient.verifyServerAcceptResponse(acceptResponse)) {
                    ssbStream = ssbHandshakeClient.boxStream()
                    buffer.close()
                    return true
                }
            }
        }
        return false
    }

    private fun start(host: String, port: Int): Boolean {
        return try {
            if (socket == null || !socket!!.isConnected || !ssbHandshakeClient.completed) {
                Socket(host, port).run {
                    socket = this
                    source = source().buffer()
                    sink = sink().buffer()
                    clientToServerRequestNumber = 0
                    performHandshake()
                }
            } else {
                return true
            }
        } catch (e: IOException) {
            Log.d("CONNECTION", "start failed: " + e.message)
            false
        }
    }

    fun close() {
        socket?.run {
            closeQuietly(this)
        }
    }

    private fun closeQuietly(closeable: Closeable) {
        try {
            Log.d("SOCKET", "closing")
            closeable.close()
        } catch (ignored: IOException) {
        }
    }

}
