package de.fqsmedia.cedrik.surfcity_android.ssb.data.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

@Entity(tableName = "Blob")
data class Blob(
    @PrimaryKey @ColumnInfo(name = "id") val id: RPCIdentifier,
    @ColumnInfo(name = "distance") val distance: Int,
    @ColumnInfo(name = "size") val size: Long,
    @ColumnInfo(name = "location") val location: Uri
)