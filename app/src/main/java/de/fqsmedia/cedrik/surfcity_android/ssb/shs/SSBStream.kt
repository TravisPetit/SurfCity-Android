package de.fqsmedia.cedrik.surfcity_android.ssb.shs

import com.goterl.lazycode.lazysodium.interfaces.SecretBox
import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import java.nio.ByteBuffer
import java.nio.ByteOrder

import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.secretUnbox
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.secretBox
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.increment

import kotlin.math.ceil
import kotlin.math.min

class SSBStream(
    private val clientToServerKey: ByteString,
    private val serverToClientKey: ByteString,
    private val clientToServerNonce: Buffer,
    private val serverToClientNonce: Buffer
) {

    companion object {
        const val HEADER_SIZE = 34
        const val MAX_MESSAGE_SIZE = 4096
    }

    fun sendToClient(message: ByteString): ByteString {
        return encrypt(message, serverToClientKey, serverToClientNonce)
    }

    fun readFromClient(source: BufferedSource): ByteString? {
        return decrypt(source, clientToServerKey, clientToServerNonce)
    }

    fun sendToServer(message: ByteString): ByteString {
        return encrypt(message, clientToServerKey, clientToServerNonce)
    }

    fun readFromServer(source: BufferedSource): ByteString {
        return decrypt(source, serverToClientKey, serverToClientNonce)
    }

    fun createGoodbye(key: ByteString, nonce: Buffer): ByteString {
        return encryptMessage(ByteArray(18).toByteString(), key, nonce)
    }


    private fun decrypt(source: BufferedSource, key: ByteString, nonce: Buffer): ByteString {
        val messages = Buffer()

        var decryptedMessage = decryptMessage(source, key, nonce)
        while(decryptedMessage != null){
            messages.write(decryptedMessage)
            decryptedMessage = decryptMessage(source, key, nonce)
        }

        val decryptedMessages = messages.readByteString()
        messages.close()

        return decryptedMessages
    }

    private fun decryptMessage(source: BufferedSource, key: ByteString, nonce: Buffer): ByteString? {


        val headerNonce = nonce.snapshot()
        val bodyNonce = nonce.snapshot().increment()

        val peekSource = source.peek()

        val encryptedHeader = Buffer()
        val headerBytes = peekSource.read(encryptedHeader, HEADER_SIZE.toLong())
        if (headerBytes == HEADER_SIZE.toLong()) {
            val header = secretUnbox(encryptedHeader.readByteString(), headerNonce, key)
            if (header != null) {
                val bodySize = header.substring(0, 2).asByteBuffer().order(ByteOrder.BIG_ENDIAN).short.toLong()
                val bodyTag = header.substring(2, header.size)

                val encryptedBody = Buffer()
                val bodyBytes = peekSource.read(encryptedBody, bodySize)
                return if (bodyBytes == bodySize) {
                    val encryptedBodyWithBodyTag = ByteString.of(*bodyTag.toByteArray(), *encryptedBody.readByteArray())
                    val decryptedBody = secretUnbox(encryptedBodyWithBodyTag, bodyNonce, key)

                    peekSource.close()
                    encryptedBody.close()
                    encryptedHeader.close()

                    source.skip(HEADER_SIZE + bodyBytes)
                    nonce.increment()
                    nonce.increment()
                    decryptedBody
                } else {
                    null
                }
            }
        } else if (headerBytes == -1L) {
            return null
        }
        return null
    }


    private fun encrypt(message: ByteString, key: ByteString, nonce: Buffer): ByteString {
        val messageCount = ceil(message.size.toFloat() / MAX_MESSAGE_SIZE.toFloat()).toInt()
        val messagesBuffer = Buffer()

        for (i in 0 until messageCount) {
            val messageStart = i * MAX_MESSAGE_SIZE
            val messageSize = min(message.size, MAX_MESSAGE_SIZE)
            val messageSegment = message.substring(messageStart, (messageStart + messageSize))
            val encryptedMessage = encryptMessage(messageSegment, key, nonce)
            messagesBuffer.write(encryptedMessage)
        }

        val encryptedMessages = messagesBuffer.readByteString()
        messagesBuffer.close()

        return encryptedMessages
    }

    private fun encryptMessage(messageSegment: ByteString, key: ByteString, nonce: Buffer): ByteString {
        val headerNonce = nonce.snapshot()
        val bodyNonce = nonce.increment().snapshot()
        nonce.increment()

        val encryptedBody = secretBox(messageSegment, bodyNonce, key)

        val headerValue = ByteString.of(
            *ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(encryptedBody.size - SecretBox.MACBYTES).array().sliceArray(
                2 until 4
            ),
            *encryptedBody.substring(0, SecretBox.MACBYTES).toByteArray()
        )

        val encryptedHeader = secretBox(headerValue, headerNonce, key)

        return ByteString.of(
            *encryptedHeader.toByteArray(),
            *encryptedBody.substring(SecretBox.MACBYTES, encryptedBody.size).toByteArray()
        )
    }
}
