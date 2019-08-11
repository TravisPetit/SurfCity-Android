package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.room.Dao
import androidx.room.Query
import de.fqsmedia.cedrik.surfcity_android.ssb.rpc.RPCIdentifier
import java.util.*

@Dao
interface ProcessDAO {
    @Query("SELECT id FROM Feed")
    fun getAllFeeds() : List<Int>

    @Query("SELECT pubkey FROM Feed WHERE id = :id")
    fun getPubkeyById(id: Int) : RPCIdentifier

    @Query("UPDATE Feed SET front_sequence = :sequence, front_previous = :previous WHERE id = :id")
    fun updateFeed(id: Int, sequence: Int, previous: RPCIdentifier)

    @Query("SELECT sequence FROM Message WHERE author = :author ORDER BY sequence DESC LIMIT 1")
    fun getFrontSequence(author: RPCIdentifier) : Int

    @Query("SELECT previous FROM Message WHERE author = :author ORDER BY sequence DESC LIMIT 1")
    fun getFrontPrevious(author: RPCIdentifier) : RPCIdentifier

    @Query("SELECT about_name FROM Message INNER JOIN Feed on Message.about_about = Feed.pubkey WHERE Feed.id = :id AND about_name NOT NULL ORDER BY sequence ASC")
    fun getNamesFromMessages(id: Int) : List<String>

    @Query("SELECT nicknames FROM About WHERE feed_id = :id")
    fun getNamesById(id: Int): String?

    @Query("UPDATE About SET nicknames = :names WHERE feed_id = :id")
    fun updateNamesOfId(id: Int, names: String) : Int

    @Query("INSERT INTO About(feed_id, nicknames) VALUES(:id, :names)")
    fun insertIntoAbout(id: Int, names: String)

    @Query("DELETE FROM Message WHERE type == :type")
    fun deleteMessagesByType(type: String)

    @Query("DELETE FROM Message WHERE type NOT IN (:types)")
    fun deleteMessagesLeaveOnlyType(types: List<String>)

    @Query("DELETE FROM Message WHERE timestamp < :limit AND received_timestamp < :limit")
    fun forgetMessagesByTimeLimit(limit: Date)

    @Query("SELECT id from Feed WHERE pubkey = :pubkey")
    fun getFeedIDByKey(pubkey: RPCIdentifier): Int

    @Query("SELECT contact_contact FROM Message WHERE contact_following = 1 AND author = :key")
    fun getFollowingsByPubkey(key: RPCIdentifier) : List<RPCIdentifier>


}