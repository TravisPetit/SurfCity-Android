package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.room.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Follow

@Dao
interface FollowDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(follow: Follow)

    @Delete
    fun delete(follow: Follow)

    @Query("DELETE FROM Follow")
    fun deleteAll()

}