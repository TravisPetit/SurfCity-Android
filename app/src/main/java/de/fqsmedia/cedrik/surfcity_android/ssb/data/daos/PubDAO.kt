package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.room.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Pub

@Dao
interface PubDAO {
    @Query("SELECT * FROM Pub")
    fun getAll(): List<Pub>

    @Query("DELETE FROM Pub")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(pub: Pub)

    @Delete
    fun delete(pub: Pub)
}