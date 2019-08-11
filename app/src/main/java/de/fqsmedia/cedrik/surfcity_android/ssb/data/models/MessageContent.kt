package de.fqsmedia.cedrik.surfcity_android.ssb.data.models


import com.squareup.moshi.JsonClass
import de.fqsmedia.cedrik.surfcity_android.ssb.data.utils.Adapters
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

open class MessageContent(open val type: String?) {

    @JsonClass(generateAdapter = true)
    data class Post(
        override val type: String = "post",
        val text: String,
        val root: RPCIdentifier? = null,
        @Adapters.SingleToArray val branch: List<RPCIdentifier>? = null,
        @Adapters.SingleToArray val mentions: List<Mention>? = null,
        val channel: String? = null
    ) : MessageContent(type) {
        @JsonClass(generateAdapter = true)
        data class Mention(
            val link: String,
            val name: String?
        )
    }

    @JsonClass(generateAdapter = true)
    data class Pub(
        override val type: String = "pub",
        val address: Address
    ) : MessageContent(type) {
        @JsonClass(generateAdapter = true)
        data class Address(
            val host: String,
            val port: Int,
            val key: String
        )
    }

    @JsonClass(generateAdapter = true)
    data class About(
        @Transient override val type: String = "about",
        val about: RPCIdentifier,
        val image: RPCIdentifier?,
        val name: String?,
        val attendee: Attendee?
    ) : MessageContent(type){
        @JsonClass(generateAdapter = true)
        data class Attendee(
            val link: RPCIdentifier,
            val remove: Boolean?
        )
    }

    @JsonClass(generateAdapter = true)
    data class Channel(
        @Transient override val type: String = "channel",
        val channel: String,
        val subscribed: Boolean
    ) : MessageContent(type)

    @JsonClass(generateAdapter = true)
    data class Contact(
        override val type: String = "contact",
        val contact: RPCIdentifier,
        val following: Boolean?,
        val blocking: Boolean?
    ) : MessageContent(type)

    @JsonClass(generateAdapter = true)
    data class Vote(
        override val type: String = "vote",
        val channel: String?,
        val vote: VoteInfo
    ) : MessageContent(type) {
        @JsonClass(generateAdapter = true)
        data class VoteInfo(
            val link: RPCIdentifier,
            val value: Int,
            val expression: String
        )
    }

    @JsonClass(generateAdapter = true)
    data class ChessInvite(
        override val type: String = "chess_invite"
    ) : MessageContent(type)

    @JsonClass(generateAdapter = true)
    data class ChessInviteAccept(
        override val type: String = "chess_invite_accept"
    ) : MessageContent(type)

    @JsonClass(generateAdapter = true)
    data class ChessMove(
        override val type: String = "chess_move"
    ) : MessageContent(type)

    @JsonClass(generateAdapter = true)
    data class ChessGameEnd(
        override val type: String = "chess_game_end"
    ) : MessageContent(type)

    @JsonClass(generateAdapter = true)
    data class SSBChessChat(
        override val type: String = "ssb_chess_chat"
    ) : MessageContent(type)

    @JsonClass(generateAdapter = true)
    data class PrivateMessage(
        val box: String
    ) : MessageContent(null)

    @JsonClass(generateAdapter = true)
    data class Gathering(
        override val type: String = "gathering"
    ) : MessageContent(type)
}