package de.fqsmedia.cedrik.surfcity_android.ssb.data.models


import android.util.Base64
import com.squareup.moshi.JsonClass
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import java.util.*
import de.fqsmedia.cedrik.surfcity_android.utils.HelperFunctions.Companion.toByteString

@JsonClass(generateAdapter = true)
class MessageModel(
    var previous: RPCIdentifier?,
    var author: RPCIdentifier,
    var sequence: Int,
    var timestamp: Date,
    var hash: String = RPCIdentifier.AlgoType.SHA256.algo,
    var content: MessageContent,
    var signature: String? = null
) {

    val moshi = Constants.getMoshi()

    constructor(
        previous: RPCIdentifier?,
        author: RPCIdentifier,
        sequence: Int,
        timestamp: Date,
        hash: String = RPCIdentifier.AlgoType.SHA256.algo,
        content: MessageContent,
        identity: Identity
    ) : this(previous, author, sequence, timestamp, hash, content, null) {
        val signatureRegex = Regex(",\\s*\"signature\":\\s*((\"[a-zA-Z0-9+/]*={0,3}.sig.[a-zA-Z0-9+/]+\")|null)")

        val json = moshi.adapter(MessageModel::class.java).indent("  ").toJson(this)
        val removedSignature = signatureRegex.replace(json, "")

        val newSig = identity.sign(removedSignature)
        val encodedSig = Base64.encodeToString(newSig, Base64.NO_WRAP)
        signature = "$encodedSig.sig.ed25519"
    }

    fun createMessageId(): RPCIdentifier {
        val encodedId = moshi.adapter(MessageModel::class.java).indent("  ")
            .toJson(this)
            .toByteArray()
            .toByteString()
            .sha256()
            .base64()


        return RPCIdentifier(encodedId, RPCIdentifier.AlgoType.SHA256, RPCIdentifier.IdentifierType.MESSAGE)
    }

    fun toJson(): String {
        return moshi.adapter(MessageModel::class.java).toJson(this)
    }
}