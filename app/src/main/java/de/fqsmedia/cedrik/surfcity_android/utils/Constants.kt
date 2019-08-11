package de.fqsmedia.cedrik.surfcity_android.utils

import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.SodiumAndroid
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.MessageContent
import de.fqsmedia.cedrik.surfcity_android.ssb.data.utils.Adapters
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCJsonAdapterFactory
import okio.ByteString.Companion.decodeHex
import java.nio.charset.StandardCharsets

class Constants{
    companion object{
        @JvmStatic
        fun getMoshi(): Moshi = Moshi.Builder()
            .add(RPCIdentifier.IdentifierJsonAdapter())
            .add(Adapters.DataTypeAdapter())
            .add(Adapters.SingleToArrayAdapter.INSTANCE)
            .add(RPCJsonAdapterFactory())
            .add(
                PolymorphicJsonAdapterFactory.of(MessageContent::class.java, "type")
                    .withSubtype(MessageContent.Post::class.java, "post")
                    .withSubtype(MessageContent.Pub::class.java, "pub")
                    .withSubtype(MessageContent.Contact::class.java, "contact")
                    .withSubtype(MessageContent.About::class.java, "about")
                    .withSubtype(MessageContent.Channel::class.java, "channel")
                    .withSubtype(MessageContent.Vote::class.java, "vote")
                    .withSubtype(MessageContent.PrivateMessage::class.java, "private")
                    .withSubtype(MessageContent.ChessGameEnd::class.java, "chess_game_end")
                    .withSubtype(MessageContent.ChessInvite::class.java, "chess_invite")
                    .withSubtype(MessageContent.ChessInviteAccept::class.java, "chess_invite_accept")
                    .withSubtype(MessageContent.ChessMove::class.java, "chess_move")
                    .withSubtype(MessageContent.SSBChessChat::class.java, "ssb_chess_chat")
                    .withSubtype(MessageContent.Gathering::class.java, "gathering")
            )
            .build()

        @JvmStatic
        val SSB_NETWORKIDENTIFIER = "d4a1cb88a66f02f8db635ce26441cc5dac1b08420ceaac230839b755845a9ffb".decodeHex()

        @JvmStatic
        val lazySodium = LazySodiumAndroid(SodiumAndroid(), StandardCharsets.UTF_8)

        @JvmStatic
        val frontierWindow = 86400000
    }
}