package de.fqsmedia.cedrik.surfcity_android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Message
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.PrivateMessage
import de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories.MessageRepository

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private val messageRepository : MessageRepository = MessageRepository(application)
    private var messages: LiveData<List<Message>> = messageRepository.getAll()


    fun getAll() : LiveData<List<Message>> = messages

    fun getAllByType(type: String) : LiveData<List<Message>> = messageRepository.getAllByType(type)

    fun getPrivate() : LiveData<List<PrivateMessage>> = messageRepository.getPrivateMessages()

}