package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo

data class Address(
    @ColumnInfo(name = "host")  val host: String,
    @ColumnInfo(name = "port") val port: Int,
    @ColumnInfo(name = "key") val key: String
)