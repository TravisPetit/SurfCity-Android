package de.fqsmedia.cedrik.surfcity_android.ssb.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

@Entity(tableName = "Pub")
data class Pub(
    @PrimaryKey @ColumnInfo(name = "pubkey") val pubkey: RPCIdentifier,
    @ColumnInfo(name = "host") val host: String,
    @ColumnInfo(name = "port") val port: Int
)