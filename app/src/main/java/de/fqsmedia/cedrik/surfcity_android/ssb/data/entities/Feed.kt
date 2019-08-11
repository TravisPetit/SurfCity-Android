package de.fqsmedia.cedrik.surfcity_android.ssb.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

@Entity(tableName = "Feed")
data class Feed(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "pubkey") val pubkey: RPCIdentifier,
    @ColumnInfo(name = "scan_low") val scan_low: Int,
    @ColumnInfo(name = "front_sequence") val front_seq: Int,
    @ColumnInfo(name = "front_previous") val front_prev: RPCIdentifier?
)