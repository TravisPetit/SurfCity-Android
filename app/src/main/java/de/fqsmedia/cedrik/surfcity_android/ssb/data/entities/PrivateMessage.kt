package de.fqsmedia.cedrik.surfcity_android.ssb.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

@Entity(tableName = "PrivateMessage")
data class PrivateMessage(
    @PrimaryKey @ColumnInfo(name = "id") val id: RPCIdentifier,
    @ColumnInfo(name = "sequence") val sequence: Int,
    @ColumnInfo(name = "text") val text: String
)