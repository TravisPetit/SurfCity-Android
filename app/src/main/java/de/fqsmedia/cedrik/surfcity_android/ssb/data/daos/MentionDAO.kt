package de.fqsmedia.cedrik.surfcity_android.ssb.data.daos

import androidx.room.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.content.Mention

@Dao
interface MentionDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg mentions: Mention)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(mention: Mention)

    @Delete
    fun delete(mention: Mention)

    @Query("DELETE FROM Mention")
    fun deleteAll()

}