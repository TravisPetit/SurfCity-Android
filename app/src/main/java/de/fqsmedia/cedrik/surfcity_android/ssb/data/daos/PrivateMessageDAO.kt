package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.PrivateMessage

@Dao
interface PrivateMessageDAO {
    @Query("SELECT * FROM PrivateMessage ORDER BY sequence DESC")
    fun getAll(): LiveData<List<PrivateMessage>>

    @Query("DELETE FROM PrivateMessage")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(message: PrivateMessage)
}