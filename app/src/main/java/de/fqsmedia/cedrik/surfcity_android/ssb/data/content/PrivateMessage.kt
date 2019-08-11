package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo

data class PrivateMessage(
    @ColumnInfo(name = "content") val content: String?
)