package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo

data class Pub(
    @ColumnInfo(name = "address") val address: Address?
)