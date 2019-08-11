package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Blob

@Dao
interface BlobDAO {
    @Query("SELECT * FROM Blob")
    fun getAll(): LiveData<List<Blob>>

    @Query("DELETE FROM Blob")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(blob: Blob)

    @Delete
    fun delete(blob: Blob)
}