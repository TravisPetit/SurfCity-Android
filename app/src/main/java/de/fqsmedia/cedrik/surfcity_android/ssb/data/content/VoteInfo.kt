package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

data class VoteInfo(
    @ColumnInfo(name = "link")  val link: RPCIdentifier,
    @ColumnInfo(name = "value") val value: Int,
    @ColumnInfo(name = "expression") val expression: String
)