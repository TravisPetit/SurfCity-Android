package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

data class Contact(
    @ColumnInfo(name = "contact")  val contact: RPCIdentifier,
    @ColumnInfo(name = "following") val following: Boolean?,
    @ColumnInfo(name = "blocking") val blocking: Boolean?
)