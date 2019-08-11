package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Feed

@Dao
interface FeedDAO {
    @Query("SELECT * FROM Feed")
    fun getAll(): List<Feed>

    @Query("DELETE FROM Feed")
    fun deleteAll()

    @Query("SELECT * FROM Feed INNER JOIN Follow ON Feed.id = Follow.who AND Follow.state > 0")
    fun getFollowing(): LiveData<List<Feed>>

    @Query("SELECT * FROM Feed INNER JOIN Follow ON Feed.id = Follow.whom AND Follow.state > 0")
    fun getFollowers(): LiveData<List<Feed>>

    @Insert
    fun insertPeers(vararg peers: Feed)

    @Insert
    fun insertPeer(peer: Feed) : Long

    @Update
    fun updatePeer(vararg peers: Feed)
}