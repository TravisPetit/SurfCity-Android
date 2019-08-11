package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

@Entity
data class Mention(
    @ColumnInfo(name = "link") val link: String,
    @ColumnInfo(name = "name") val name: String?,
    @PrimaryKey @ColumnInfo(name = "message_id") val messageId: RPCIdentifier
)