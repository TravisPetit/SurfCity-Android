package de.fqsmedia.cedrik.surfcity_android.ssb.rpc

import android.util.Log
import okio.ByteString
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString
import java.lang.RuntimeException

class RPCProtocol {
    companion object {
        const val HEADER_SIZE = 9

        private const val STREAM: Byte = 0b00001000
        private const val ENDERROR: Byte = 0b00000100
        const val BINARY: Byte = 0b00000000
        private const val UTF8: Byte = 0b00000001
        private const val JSON: Byte = 0b00000010

        enum class RPCBodyType {
            UTF8, JSON, BINARY
        }

        fun getBodyLength(header: ByteString): Int {
            if (header.size < HEADER_SIZE) {
                Log.e("EXCEPTION: ", "Wrong header size: ${header.size}")
                throw RuntimeException("Header wrong size.")
            }
            return ByteBuffer.wrap(header.substring(1, 5).toByteArray()).order(ByteOrder.BIG_ENDIAN).int
        }

        fun decode(encoded: ByteString): RPCMessage {
            val header = encoded.substring(0, HEADER_SIZE)

            val flags = header[0]
            val stream = (flags and STREAM) != 0x00.toByte()
            val enderror = (flags and ENDERROR) != 0x00.toByte()

            val bodyType = when {
                flags and JSON != 0x00.toByte() -> Companion.RPCBodyType.JSON
                flags and UTF8 != 0x00.toByte() -> Companion.RPCBodyType.UTF8
                else -> Companion.RPCBodyType.BINARY
            }

            val bodyLength = ByteBuffer.wrap(header.substring(1, 5).toByteArray()).order(ByteOrder.BIG_ENDIAN).int
            val requestNumber = ByteBuffer.wrap(header.substring(5, 9).toByteArray()).order(ByteOrder.BIG_ENDIAN).int
            val body = encoded.substring(HEADER_SIZE, (bodyLength + HEADER_SIZE))

            return RPCMessage(stream, enderror, bodyType, bodyLength, requestNumber, body)
        }


        fun goodbye(requestNumber: Int): ByteString {
            return encode(
                RPCMessage(
                    false,
                    true,
                    Companion.RPCBodyType.BINARY,
                    9,
                    requestNumber,
                    ByteArray(9).toByteString()
                )
            )
        }

        fun encode(rpcMessage: RPCMessage): ByteString {
            return encode(
                rpcMessage.body,
                rpcMessage.stream,
                rpcMessage.endError,
                rpcMessage.bodyType,
                rpcMessage.requestNumber
            )
        }

        private fun encode(
            body: ByteString,
            stream: Boolean = true,
            endError: Boolean = false,
            bodyType: RPCBodyType = Companion.RPCBodyType.JSON,
            requestNumber: Int = 0
        ): ByteString {
            var headerFlags = 0x00.toByte()
            headerFlags = headerFlags or JSON
            if (stream) headerFlags = headerFlags or STREAM
            if (endError) headerFlags = headerFlags or ENDERROR
            headerFlags = when (bodyType) {
                Companion.RPCBodyType.JSON -> headerFlags or JSON
                Companion.RPCBodyType.UTF8 -> headerFlags or UTF8
                Companion.RPCBodyType.BINARY -> headerFlags
            }

            val bodyLength = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(body.size).array()
            val requestNumberArray = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(requestNumber).array()

            return ByteString.of(headerFlags, *bodyLength, *requestNumberArray, *body.toByteArray())
        }
    }
}