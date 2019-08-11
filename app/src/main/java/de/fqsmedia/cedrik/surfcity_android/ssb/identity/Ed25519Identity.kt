package de.fqsmedia.cedrik.surfcity_android.ssb.identity

import android.util.Base64
import com.goterl.lazycode.lazysodium.interfaces.Sign
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString
import java.nio.charset.Charset

class Ed25519Identity : Identity {


    private var keyPair: KeyPair? = null
    private var publicKey: ByteArray? = null

    private val lazySodium = Constants.lazySodium

    constructor(publicKey: ByteArray, secretKey: ByteArray) {
        keyPair = KeyPair(Key.fromBytes(publicKey), Key.fromBytes(secretKey))
    }

    constructor(keyPair: KeyPair) {
        this.keyPair = keyPair
    }

    constructor(publicKey: ByteArray) {
        this.publicKey = publicKey
    }

    override fun sign(message: ByteArray): ByteArray {
        if (keyPair != null) {
            val signature = ByteArray(Sign.BYTES)
            if (lazySodium.cryptoSignDetached(signature, message, message.size.toLong(), keyPair?.secretKey?.asBytes))
                return signature
            else
                throw IdentityException("Could not sign with Identity.")
        } else
            throw IdentityException("Can't sign with public only identity.")
    }

    override fun sign(message: String, charset: Charset): ByteArray {
        return sign(message.toByteArray(charset))
    }

    override fun getPublicKey(): ByteArray {
        if (keyPair != null) {
            return keyPair!!.publicKey.asBytes
        } else if (publicKey != null) {
            return publicKey as ByteArray
        }

        throw IdentityException("Trying to get public key from empty identity.")
    }

    override fun getString(): String {
        return "@${Base64.encodeToString(getPublicKey(), Base64.NO_WRAP)}.ed25519"
    }

    override fun getSecret(): String {
        if (keyPair != null) {
            return "${Base64.encodeToString(
                keyPair!!.secretKey.asBytes,
                Base64.NO_WRAP
            )}.ed25519"
        }
        throw IdentityException("Public identity has no secret key.")
    }

    override fun deriveSharedSecretAb(publicKey: ByteArray): ByteArray {
        val curveSecretKey = ByteArray(Sign.CURVE25519_SECRETKEYBYTES)
        lazySodium.convertSecretKeyEd25519ToCurve25519(curveSecretKey, keyPair?.secretKey?.asBytes)
        return lazySodium.cryptoScalarMult(Key.fromBytes(curveSecretKey), Key.fromBytes(publicKey)).asBytes
    }

    override fun decryptPrivateMessage(message: String): String? {
        val encoded = Base64.decode(message.removeSuffix(".box"), Base64.DEFAULT)
        val nonce = encoded.sliceArray(0 until 24)
        val key = lazySodium.cryptoScalarMult(keyPair?.secretKey, Key.fromBytes(encoded.sliceArray(24 until 56)))
        var recipients = encoded.sliceArray(56 until encoded.size)

        for(i in 0..8){
            if(recipients.size < 49)
                return null

            val decryptedHeader = HelperFunctions.secretUnbox(
                recipients.sliceArray(0 until 49).toByteString(),
                nonce.toByteString(),
                key.asBytes.toByteString()
            )


            if(decryptedHeader != null){
                val numberRecipients = decryptedHeader[0].toInt()
                return HelperFunctions.secretUnbox(
                    encoded.sliceArray(56+numberRecipients*49 until encoded.size).toByteString(),
                    nonce.toByteString(),
                    decryptedHeader.substring(1, decryptedHeader.size)
                )?.utf8()
            }
            recipients = recipients.sliceArray(49 until recipients.size)
        }
        return null
    }

    override fun verify(signature: ByteArray, message: ByteArray): Boolean {
        return lazySodium.cryptoSignVerifyDetached(signature, message, message.size, publicKey)
    }


}
