package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo

data class Channel(
    @ColumnInfo(name = "channel")  val channel: String,
    @ColumnInfo(name = "subscribed") val subscribed: Boolean?
)