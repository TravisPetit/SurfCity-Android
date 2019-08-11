package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo
import androidx.room.Embedded
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

data class About(
    @ColumnInfo(name = "about") val about: RPCIdentifier,
    @ColumnInfo(name = "image") val image: RPCIdentifier?,
    @ColumnInfo(name = "name") val name: String?,
    @Embedded(prefix = "attendee_") val attendee: Attendee?
)