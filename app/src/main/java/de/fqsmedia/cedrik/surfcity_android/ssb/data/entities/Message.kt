package de.fqsmedia.cedrik.surfcity_android.ssb.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.fqsmedia.cedrik.surfcity_android.ssb.data.content.Content
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import java.util.*

@Entity(tableName = "Message")
data class Message(
    @PrimaryKey @ColumnInfo(name = "id") var id: RPCIdentifier,
    @ColumnInfo (name = "timestamp") var timestamp: Date,
    @ColumnInfo (name = "author") var author: RPCIdentifier,
    @ColumnInfo (name = "previous") var previous: RPCIdentifier?,
    @ColumnInfo(name = "sequence") var sequence: Int,
    @ColumnInfo(name = "hash") var hash: String,
    @Embedded val content: Content,
    @ColumnInfo(name = "signature") val signature: String?,
    @ColumnInfo(name = "received_timestamp") val receivedTimestamp: Date,
    @ColumnInfo(name = "raw") val raw: String = ""
    )