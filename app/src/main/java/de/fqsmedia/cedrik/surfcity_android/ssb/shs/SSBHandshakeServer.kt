package de.fqsmedia.cedrik.surfcity_android.ssb.shs

import com.goterl.lazycode.lazysodium.interfaces.SecretBox
import com.goterl.lazycode.lazysodium.interfaces.Sign
import com.goterl.lazycode.lazysodium.utils.Key
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import okio.ByteString

import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.secretUnbox
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.secretBox
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toKey
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.verifySignDetached


class SSBHandshakeServer(
    ed25519Identity: Identity,
    networkIdentifier: ByteString = Constants.SSB_NETWORKIDENTIFIER
) : SSBHandshake(ed25519Identity, networkIdentifier)
{
    private var detachedSignature: ByteString? = null

    fun verifyClientAuthentication(message: ByteString): Boolean {
        val zeroNonce = ByteArray(SecretBox.NONCEBYTES).toByteString()
        val hashKey = ByteString.of(*networkIdentifier.toByteArray(), *sharedSecretab!!.asBytes, *sharedSecretaB!!.asBytes).sha256()
        val dataPlainText = secretUnbox(message, zeroNonce, hashKey)

        val detachedSignatureA = dataPlainText?.substring(0, 64)
        val clientLongTermPublicKey = dataPlainText?.substring(64, 96)
        val hashab = sharedSecretab!!.asBytes.toByteString().sha256()
        val expectedMessage = ByteString.of(*networkIdentifier.toByteArray(), *ed25519Identity.getPublicKey(), *hashab.toByteArray())

        if (verifySignDetached(detachedSignatureA!!, expectedMessage, clientLongTermPublicKey!!)) {
            this.remoteKey = clientLongTermPublicKey
            this.detachedSignature = detachedSignatureA

            val curve25519ClientKey = ByteArray(Sign.CURVE25519_PUBLICKEYBYTES)
            lazySodium.convertPublicKeyEd25519ToCurve25519(curve25519ClientKey, remoteKey!!.toByteArray())
            this.sharedSecretAb = lazySodium.cryptoScalarMult(localEphemeralKeyPair.secretKey, curve25519ClientKey.toByteString().toKey())
            return true
        }
        return false
    }

    fun acceptMessage(): ByteString {
        val hashab = sharedSecretab?.asBytes?.toByteString()?.sha256()
        val message = ByteString.of(*networkIdentifier.toByteArray(), *detachedSignature!!.toByteArray(), *remoteKey!!.toByteArray(), *hashab!!.toByteArray())
        val detachedSignatureB = ed25519Identity.sign(message.toByteArray()).toByteString()

        val zeroNonce = ByteArray(SecretBox.NONCEBYTES).toByteString()
        val key = ByteString.of(*networkIdentifier.toByteArray(), *sharedSecretab!!.asBytes, *sharedSecretaB!!.asBytes, *sharedSecretAb!!.asBytes).sha256()
        val body = secretBox(detachedSignatureB, zeroNonce, key)

        completed = true
        return body
    }

    override fun computeSharedSecrets() {
        sharedSecretab = lazySodium.cryptoScalarMult(
            localEphemeralKeyPair.secretKey, remoteEphemeralKey!!.toKey()
        )
        sharedSecretaB = Key.fromBytes(ed25519Identity.deriveSharedSecretAb(remoteEphemeralKey!!.toByteArray()))
    }
}
