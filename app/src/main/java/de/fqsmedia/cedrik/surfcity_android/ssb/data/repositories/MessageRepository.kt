package de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories

import android.content.Context
import android.os.AsyncTask
import android.util.Base64
import androidx.lifecycle.LiveData
import com.squareup.moshi.Moshi
import de.fqsmedia.cedrik.surfcity_android.dagger.DaggerSurfCityComponent
import de.fqsmedia.cedrik.surfcity_android.dagger.SurfCityModule
import de.fqsmedia.cedrik.surfcity_android.ssb.data.SurfCityDatabase
import de.fqsmedia.cedrik.surfcity_android.ssb.data.content.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.MentionDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.MessageDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.PrivateMessageDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Message
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.ExtendedMessage
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.MessageContent
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.MessageModel
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.SecretHandler
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import java.util.*
import javax.inject.Inject

class MessageRepository(context: Context) {
    @Inject
    lateinit var database: SurfCityDatabase
    @Inject
    lateinit var moshi: Moshi
    @Inject
    lateinit var secretHandler: SecretHandler

    private val messageDAO: MessageDAO
    private val mentionDAO: MentionDAO
    private val privateMessageDAO: PrivateMessageDAO
    private val allMessages: LiveData<List<Message>>

    init {
        DaggerSurfCityComponent.builder().module(SurfCityModule(context)).build().inject(this)
        messageDAO = database.messageDAO()
        mentionDAO = database.mentionDAO()
        privateMessageDAO = database.privateMessageDAO()
        allMessages = messageDAO.getAll()
    }

    fun insertMesssage(message: MessageModel)
    {
        val messageID = message.createMessageId()

        val content: Content =
            when (message.content.type) {
                "post" -> {
                    val postContent = message.content as MessageContent.Post

                    postContent.mentions?.map {
                        Mention(it.link, it.name, messageID)
                    }?.let {
                        mentionDAO.insertAll(*it.toTypedArray())
                    }
                    Content(
                        type = message.content.type as String,
                        post = Post(postContent.text, postContent.root, postContent.branch, postContent.channel)
                    )
                }

                "about" -> {
                    val aboutContent = message.content as MessageContent.About
                    Content(
                        type = message.content.type as String,
                        about = About(
                            aboutContent.about,
                            aboutContent.image,
                            aboutContent.name,
                            if (aboutContent.attendee != null) Attendee(
                                aboutContent.attendee.link,
                                aboutContent.attendee.remove
                            ) else null
                        )
                    )
                }

                "pub" -> {
                    val pubContent = message.content as MessageContent.Pub
                    Content(
                        type = message.content.type as String,
                        pub = Pub(Address(pubContent.address.host, pubContent.address.port, pubContent.address.key))
                    )
                }

                "channel" -> {
                    val channelContent = message.content as MessageContent.Channel
                    Content(
                        type = message.content.type as String,
                        channel = Channel(channelContent.channel, channelContent.subscribed)
                    )
                }

                "contact" -> {
                    val contactContent = message.content as MessageContent.Contact
                    Content(
                        type = message.content.type as String,
                        contact = Contact(contactContent.contact, contactContent.following, contactContent.blocking)
                    )
                }

                "vote" -> {
                    val voteContent = message.content as MessageContent.Vote
                    Content(
                        type = message.content.type as String,
                        vote = Vote(
                            voteContent.channel,
                            VoteInfo(voteContent.vote.link, voteContent.vote.value, voteContent.vote.expression)
                        )
                    )
                }

                null -> {
                    val privateContent = message.content as MessageContent.PrivateMessage
                    val decrypted = getDecryptedMessage(privateContent.box)
                    if(decrypted != null){
                        privateMessageDAO.insert(
                            de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.PrivateMessage(
                                messageID,
                                message.sequence,
                                decrypted
                            )
                        )
                    }
                    Content(
                        type = "private",
                        privateMessage = PrivateMessage(privateContent.box)
                    )
                }

                else -> Content("not_yet_implemented")
            }


        messageDAO.insert(
            Message(
                messageID,
                message.timestamp,
                message.author,
                message.previous,
                message.sequence,
                message.hash,
                content,
                message.signature,
                Date(System.currentTimeMillis()),
                message.toJson()
            )
        )
    }


    private fun getDecryptedMessage(message: String): String? {
        return secretHandler.getIdentity().decryptPrivateMessage(message)
    }

    fun getAll(): LiveData<List<Message>> {
        return messageDAO.getAll()
    }

    fun getByID(id: RPCIdentifier): List<Message> {
        return messageDAO.getMessageByID(id)
    }

    fun getByIDAndSequence(id: RPCIdentifier, sequence: Int): Message? {
        return messageDAO.getMessageByIDAndSequence(id, sequence)
    }

    fun saveMessage(message: ExtendedMessage) {
        insertMesssage(message.value)
    }

    fun getAllByType(type: String): LiveData<List<Message>> {
        return messageDAO.getMessageByType(type)
    }

    fun getPrivateMessages() : LiveData<List<de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.PrivateMessage>> {
        return privateMessageDAO.getAll()
    }

    fun getMessageByTypeAndAuthor(type: String, author: RPCIdentifier): LiveData<List<Message>> {
        return messageDAO.getMessageByTypeAndAuthor(author.toString(), type)
    }

    fun submitNewMessage(text: String, channel: String?) {
        class SubmitNewMessageTask : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val id = RPCIdentifier(
                    Base64.encodeToString(secretHandler.getIdentity().getPublicKey(), Base64.NO_WRAP),
                    RPCIdentifier.AlgoType.ED25519,
                    RPCIdentifier.IdentifierType.IDENTITY
                )
                val previousMessage = messageDAO.getMostRecentMessageFromAuthor(id.toString())
                val previousMessageID = previousMessage?.id
                val newSequence = (previousMessage?.sequence ?: 0) + 1
                val post = MessageContent.Post(text = text, channel = channel)
                val timestamp = Date(System.currentTimeMillis())
                val message = MessageModel(
                    previousMessageID,
                    id,
                    newSequence,
                    timestamp,
                    content = post,
                    identity = secretHandler.getIdentity()
                )
                insertMesssage(message)

                return null
            }
        }
    }


}