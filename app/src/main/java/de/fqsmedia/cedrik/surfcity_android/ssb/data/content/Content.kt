package de.fqsmedia.cedrik.surfcity_android.ssb.data.content

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class Content(
    @ColumnInfo(name = "type") var type: String,
    @Embedded(prefix = "post_") val post: Post? = null,
    @Embedded(prefix = "about_") val about: About? = null,
    @Embedded(prefix = "pub_") val pub: Pub? = null,
    @Embedded(prefix = "post_") val privateMessage: PrivateMessage? = null,
    @Embedded(prefix = "channel_") val channel: Channel? = null,
    @Embedded(prefix = "contact_") val contact: Contact? = null,
    @Embedded(prefix = "vote_") val vote: Vote? = null
)
