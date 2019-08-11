package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Message
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier

@Dao
interface MessageDAO {
    @Query("SELECT * FROM message ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<Message>>

    @Query("DELETE FROM message")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMultiple(vararg message: Message)

    @Delete
    fun delete(message: Message)

    @Query("SELECT * FROM message WHERE id = :id")
    fun getMessageByID(id: RPCIdentifier): List<Message>

    @Query("SELECT * FROM message WHERE id = :id AND sequence = :sequence")
    fun getMessageByIDAndSequence(id: RPCIdentifier, sequence: Int) : Message?

    @Query("SELECT * FROM message WHERE author = :author ORDER BY sequence DESC LIMIT 1")
    fun getMostRecentMessageFromAuthor(author: String): Message?

    @Query("SELECT * FROM message WHERE author = :author AND type = :type ORDER BY timestamp DESC")
    fun getMessageByTypeAndAuthor(author: String, type: String): LiveData<List<Message>>

    @Query("SELECT * FROM message WHERE type = :type ORDER BY timestamp DESC")
    fun getMessageByType(type: String): LiveData<List<Message>>
}