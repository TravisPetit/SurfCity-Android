package de.fqsmedia.cedrik.surfcity_android.ssb.identity

import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.SodiumAndroid
import com.goterl.lazycode.lazysodium.interfaces.Sign
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import java.nio.charset.Charset

import java.security.SecureRandom

interface Identity {


    fun sign(message: ByteArray): ByteArray
    fun sign(message: String, charset: Charset = Charsets.UTF_8): ByteArray

    fun getPublicKey(): ByteArray
    fun getString(): String
    fun getSecret(): String

    fun deriveSharedSecretAb(publicKey: ByteArray): ByteArray
    fun decryptPrivateMessage(message: String) : String?
    fun verify(signature: ByteArray, message: ByteArray): Boolean


    companion object {

        private val lazySodium = LazySodiumAndroid(SodiumAndroid())

        fun fromKeyPair(keyPair: KeyPair): Identity {
            return Ed25519Identity(keyPair)
        }

        fun fromPublicKey(publicKey: Key): Identity {
            return Ed25519Identity(publicKey.asBytes)
        }

        fun fromSecretKey(secretKey: Key): Identity {
            return Ed25519Identity(lazySodium.cryptoSignSecretKeyPair(secretKey))
        }

        private fun randomEd25519(): Identity {
            return Ed25519Identity(
                lazySodium.cryptoSignSeedKeypair(SecureRandom().generateSeed(Sign.ED25519_SEEDBYTES)))

        }

        fun random(): Identity {
            return randomEd25519()
        }
    }

}
