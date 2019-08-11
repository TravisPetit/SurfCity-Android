package de.fqsmedia.cedrik.surfcity_android.ssb.data.models

import com.squareup.moshi.JsonClass
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import java.util.*

@JsonClass(generateAdapter = true)
class ExtendedMessage (
    val key: RPCIdentifier,
    val value: MessageModel,
    val timestamp: Date
) {
    constructor(
        value: MessageModel,
        timestamp: Date
    ): this(value.createMessageId(), value, timestamp)

    constructor(
        value: MessageModel
    ): this(value.createMessageId(), value, Date(System.currentTimeMillis()))
}