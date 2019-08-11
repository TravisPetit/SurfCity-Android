package de.fqsmedia.cedrik.surfcity_android.ssb.rpc

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import de.fqsmedia.cedrik.surfcity_android.ssb.connection.PeerConnection
import de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories.MessageRepository
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString

class RPCRequestQueue(val moshi: Moshi) {
    private val queue = mutableListOf<Pair<Int, RPCRequest>>()
    private val requestNumberSet = mutableSetOf<Int>()

    fun add(request: RPCMessage) {
        if (checkValidRequest(request)) {
            val moshi = Moshi.Builder().add(RPCJsonAdapterFactory()).add(RPCIdentifier.IdentifierJsonAdapter()).build()
            val jsonAdapter = moshi.adapter(RPCRequest::class.java)
            val bodyString = request.body.utf8()
            //Log.d("RPC Request, Body", bodyString)

            jsonAdapter.fromJson(bodyString)?.let {
                requestNumberSet.add(request.requestNumber)
                queue.add(Pair(request.requestNumber, it))
            }
        } else {
            Log.e("EXCEPTION: ", "Invalid request: $request")
            throw RPCException("Invalid request.")
        }
    }

    fun processRequest(connection: PeerConnection, context: Context) {
        if (queue.isNotEmpty()) {
            val messageRepo = MessageRepository(context)

            val requestPair = queue.removeAt(0)
            val request = requestPair.second
            when (request.name[0]) {
                RPCRequest.CREATE_HISTORY_STREAM -> {
                    val args = (request as RPCRequest.RequestCreateHistoryStream).args
                    if (args.isNotEmpty()) {
                        val message = messageRepo.getByIDAndSequence(args[0].id, args[0].sequence)
                        if (message != null)
                            Log.d("REQUESTED: ", message.toString())

                        endStream(requestPair.first, connection)


                    }

                }
                RPCRequest.CREATE_USER_STREAM -> {
                }
                RPCRequest.BLOBS -> {
                    when (request.name[1]) {
                        RPCRequest.GET -> {

                        }
                        RPCRequest.GET_SLICE -> {

                        }
                        RPCRequest.HAS -> {

                        }
                        RPCRequest.CHANGES -> {

                        }
                        RPCRequest.CREATE_WANTS -> {
                            respondWithEmpty(requestPair.first, connection)
                        }
                    }
                }
            }
        }
    }

    private fun respondWithEmpty(requestNumber: Int, peerConnection: PeerConnection) {
        val payload = "{}".toByteArray().toByteString()
        val response = RPCMessage(
            true,
            false,
            RPCProtocol.Companion.RPCBodyType.JSON,
            payload.size,
            -requestNumber,
            payload
        )
        peerConnection.writeQueue.add(response)
    }

    private fun endStream(requestNumber: Int, peerConnection: PeerConnection) {
        RPCProtocol.goodbye(requestNumber)
        val payload = "true".toByteArray().toByteString()
        val response = RPCMessage(
            true,
            true,
            RPCProtocol.Companion.RPCBodyType.JSON,
            payload.size,
            -requestNumber,
            payload
        )
        peerConnection.writeQueue.add(response)
    }

    private fun checkValidRequest(request: RPCMessage): Boolean {
        return request.requestNumber > 0
                && !requestNumberSet.contains(request.requestNumber)
    }
}