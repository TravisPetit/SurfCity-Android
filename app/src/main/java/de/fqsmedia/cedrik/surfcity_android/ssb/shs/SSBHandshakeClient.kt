package de.fqsmedia.cedrik.surfcity_android.ssb.shs

import com.goterl.lazycode.lazysodium.interfaces.SecretBox
import com.goterl.lazycode.lazysodium.interfaces.Sign
import com.goterl.lazycode.lazysodium.utils.Key
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import okio.*

import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.secretUnbox
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.secretBox
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toKey
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.verifySignDetached

class SSBHandshakeClient(
    ed25519Identity: Identity,
    serverKey: ByteString,
    networkIdentifier: ByteString = Constants.SSB_NETWORKIDENTIFIER
)
    : SSBHandshake(ed25519Identity, networkIdentifier) {

    init {
        remoteKey = serverKey
    }

    private var detachedSignature: ByteString? = null

    override fun computeSharedSecrets() {
        val curve25519ServerKey = ByteArray(Sign.CURVE25519_PUBLICKEYBYTES)
        lazySodium.convertPublicKeyEd25519ToCurve25519(curve25519ServerKey, remoteKey!!.toByteArray())

        sharedSecretab = lazySodium.cryptoScalarMult(localEphemeralKeyPair.secretKey, remoteEphemeralKey?.toKey())
        sharedSecretaB = lazySodium.cryptoScalarMult(localEphemeralKeyPair.secretKey, curve25519ServerKey.toByteString().toKey())
        sharedSecretAb = Key.fromBytes(ed25519Identity.deriveSharedSecretAb(remoteEphemeralKey!!.toByteArray()))
    }

    fun authenticateMessage(): ByteString {
        val hash = ByteString.of(*sharedSecretab!!.asBytes).sha256()
        val message = ByteString.of(*networkIdentifier.toByteArray(), *remoteKey!!.toByteArray(), *hash.toByteArray())
        detachedSignature = ed25519Identity.sign(message.toByteArray()).toByteString()

        val finalMessage = ByteString.of(*detachedSignature!!.toByteArray(), *ed25519Identity.getPublicKey())
        val zeroNonce = ByteArray(SecretBox.NONCEBYTES).toByteString()
        val boxKey = ByteString.of(*networkIdentifier.toByteArray(), *sharedSecretab!!.asBytes, *sharedSecretaB!!.asBytes).sha256()

        return secretBox(finalMessage, zeroNonce, boxKey)
    }

    fun verifyServerAcceptResponse(message: ByteString): Boolean {
        val zeroNonce = ByteArray(SecretBox.NONCEBYTES).toByteString()
        val responseKey = ByteString.of(*networkIdentifier.toByteArray(), *sharedSecretab!!.asBytes, *sharedSecretaB!!.asBytes, *sharedSecretAb!!.asBytes).sha256()
        val hash = ByteString.of(*sharedSecretab!!.asBytes).sha256()

        val expectedMessage = ByteString.of(*networkIdentifier.toByteArray(), *detachedSignature!!.toByteArray(), *ed25519Identity.getPublicKey(), *hash.toByteArray())

        secretUnbox(message, zeroNonce, responseKey)?.let {
            completed = verifySignDetached(it, expectedMessage, remoteKey!!)
            return completed
        }

        return false
    }

}