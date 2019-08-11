package de.fqsmedia.cedrik.surfcity_android.ssb.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "About")
data class About(
    @PrimaryKey @ColumnInfo(name = "feed_id") val id: Int,
    @ColumnInfo(name = "nicknames") val names: List<String>
)