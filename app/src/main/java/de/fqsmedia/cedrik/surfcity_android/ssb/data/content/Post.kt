package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

data class Post(
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "root") val root: RPCIdentifier?,
    @ColumnInfo(name = "branch") val branch: List<RPCIdentifier>?,
    @ColumnInfo(name = "channel") val channel: String?
)