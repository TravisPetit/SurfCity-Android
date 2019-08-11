package de.fqsmedia.cedrik.surfcity_android.ssb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.fqsmedia.cedrik.surfcity_android.ssb.data.content.Mention
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.*
import de.fqsmedia.cedrik.surfcity_android.ssb.data.utils.DBTypeConverters
import de.fqsmedia.cedrik.surfcity_android.utils.SingletonHolder

@Database(
    entities = [About::class, Follow::class, Message::class, Feed::class, PrivateMessage::class, Pub::class, Mention::class, Blob::class],
    version = 1
)

@TypeConverters(DBTypeConverters::class)

abstract class SurfCityDatabase : RoomDatabase(){
    abstract fun messageDAO(): MessageDAO
    abstract fun feedDAO(): FeedDAO
    abstract fun blobDAO(): BlobDAO
    abstract fun pubDAO() : PubDAO
    abstract fun mentionDAO() : MentionDAO
    abstract fun processDAO() : ProcessDAO
    abstract fun followDAO() : FollowDAO
    abstract fun privateMessageDAO() : PrivateMessageDAO

    companion object: SingletonHolder<SurfCityDatabase, Context>({
        Room.databaseBuilder(it, SurfCityDatabase::class.java, "surfcity_db")
            .addCallback(object: RoomDatabase.Callback(){
            }).build()
    })
}