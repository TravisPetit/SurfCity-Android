package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo

data class Vote(
    @ColumnInfo(name = "channel")  val channel: String?,
    @ColumnInfo(name = "vote") val vote: VoteInfo
)