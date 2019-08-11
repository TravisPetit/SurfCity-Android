package de.fqsmedia.cedrik.surfcity_android.utils

import com.goterl.lazycode.lazysodium.interfaces.SecretBox
import com.goterl.lazycode.lazysodium.utils.Key
import okio.Buffer
import okio.ByteString

class HelperFunctions {

    companion object {
        private val lazySodium = Constants.lazySodium

        fun ByteArray.increment(): ByteArray {
            for (i in size - 1 downTo 0) {
                if (this[i] == 0xFF.toByte()) {
                    this[i] = 0x00.toByte()
                } else {
                    ++this[i]
                    break
                }
            }
            return this
        }

        fun ByteString.increment(): ByteString {
            return ByteString.of(*this.toByteArray().increment())
        }

        fun Buffer.increment(): Buffer {
            val incremented = this.snapshot().increment()
            this.skip(this.size)
            this.write(incremented)
            return this
        }

        fun ByteArray.toByteString(): ByteString {
            return ByteString.of(*this)
        }

        fun ByteString.toKey(): Key {
            return Key.fromBytes(this.toByteArray())
        }

        fun secretBox(message: ByteString, nonce: ByteString, key: ByteString): ByteString {
            val encrypted = ByteArray(message.size + SecretBox.MACBYTES)
            lazySodium.cryptoSecretBoxEasy(encrypted, message.toByteArray(), message.size.toLong(), nonce.toByteArray(), key.toByteArray())
            return encrypted.toByteString()
        }

        fun secretUnbox(encrypted: ByteString, nonce: ByteString, key: ByteString): ByteString? {
            val decrypted = ByteArray(encrypted.size - SecretBox.MACBYTES)
            val valid = lazySodium.cryptoSecretBoxOpenEasy(
                decrypted,
                encrypted.toByteArray(),
                encrypted.size.toLong(),
                nonce.toByteArray(),
                key.toByteArray()
            )

            return if (valid) decrypted.toByteString() else null
        }

        fun verifySignDetached(signature: ByteString, message: ByteString, key: ByteString): Boolean {
            return lazySodium.cryptoSignVerifyDetached(signature.toByteArray(), message.toByteArray(), message.size, key.toByteArray())
        }
    }
}