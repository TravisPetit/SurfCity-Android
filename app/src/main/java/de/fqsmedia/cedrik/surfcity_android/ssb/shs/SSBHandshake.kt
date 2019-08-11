package de.fqsmedia.cedrik.surfcity_android.ssb.shs

import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.SodiumAndroid
import com.goterl.lazycode.lazysodium.interfaces.Auth
import com.goterl.lazycode.lazysodium.interfaces.Hash
import com.goterl.lazycode.lazysodium.interfaces.SecretBox
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import okio.*
import java.nio.charset.StandardCharsets

import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString


abstract class SSBHandshake(
    val ed25519Identity: Identity,
    val networkIdentifier: ByteString = Constants.SSB_NETWORKIDENTIFIER
) {

    protected val lazySodium = LazySodiumAndroid(SodiumAndroid(), StandardCharsets.UTF_8)

    protected var sharedSecretab: Key? = null
    protected var sharedSecretaB: Key? = null
    protected var sharedSecretAb: Key? = null

    var localEphemeralKeyPair: KeyPair = lazySodium.cryptoKxKeypair()
    var remoteEphemeralKey: ByteString? = null
    var remoteKey: ByteString? = null
    var completed = false

    private fun createHMAC(key: ByteString, message: ByteString): ByteString {
        val hmac = ByteArray(Auth.BYTES)
        lazySodium.cryptoAuth(hmac, message.toByteArray(), message.size.toLong(), key.toByteArray())
        return hmac.toByteString()
    }

    private fun hash256(message: ByteArray): ByteArray {
        val hash256 = ByteArray(Hash.SHA256_BYTES)
        lazySodium.cryptoHashSha256(hash256, message, message.size.toLong())
        return hash256
    }

    fun boxStream(): SSBStream {
        val localToRemoteKey =
            ByteString.of(
                *ByteString.of(*networkIdentifier.toByteArray(), *sharedSecretab!!.asBytes, *sharedSecretaB!!.asBytes, *sharedSecretAb!!.asBytes).sha256().sha256().toByteArray(),
                *remoteKey!!.toByteArray()
            ).sha256()

        val remoteToLocalKey =
            ByteString.of(
                *ByteString.of(*networkIdentifier.toByteArray(), *sharedSecretab!!.asBytes, *sharedSecretaB!!.asBytes, *sharedSecretAb!!.asBytes).sha256().sha256().toByteArray(),
                *ed25519Identity.getPublicKey()
            ).sha256()

        val localToRemoteNonce = createHMAC(networkIdentifier, remoteEphemeralKey!!).substring(0, SecretBox.NONCEBYTES)
        val remoteToLocalNonce = createHMAC(networkIdentifier, localEphemeralKeyPair.publicKey.asBytes.toByteString()).substring(0, SecretBox.NONCEBYTES)

        return SSBStream(localToRemoteKey, remoteToLocalKey, Buffer().write(localToRemoteNonce), Buffer().write(remoteToLocalNonce))
    }

    fun helloMessage(): ByteString {
        val hmac = createHMAC(networkIdentifier, ByteString.of(*localEphemeralKeyPair.publicKey.asBytes))
        return ByteString.of(*hmac.toByteArray(), *localEphemeralKeyPair.publicKey.asBytes)
    }

    fun isHelloMessage(message: ByteString): Boolean {
        if (message.size != 64)
            return false

        val mac = message.substring(0, 32)
        val remoteEphemeralKey = message.substring(32, 64)
        val expectedMac = createHMAC(networkIdentifier, remoteEphemeralKey)

        if (mac == expectedMac) {
            this.remoteEphemeralKey = remoteEphemeralKey
            computeSharedSecrets()
            return true
        }
        return false
    }

    protected abstract fun computeSharedSecrets()
}