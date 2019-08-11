package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

data class Attendee(
    @ColumnInfo(name = "link")  val link: RPCIdentifier,
    @ColumnInfo(name = "remove") val remove: Boolean?
)