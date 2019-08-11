package de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import de.fqsmedia.cedrik.surfcity_android.dagger.DaggerSurfCityComponent
import de.fqsmedia.cedrik.surfcity_android.dagger.SurfCityModule
import de.fqsmedia.cedrik.surfcity_android.ssb.data.SurfCityDatabase
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.FeedDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Feed
import javax.inject.Inject

class FeedRepository(context: Context) {
    @Inject
    lateinit var database: SurfCityDatabase
    private val feedDAO: FeedDAO

    init {
        DaggerSurfCityComponent.builder().module(SurfCityModule(context)).build().inject(this)
        feedDAO = database.feedDAO()
    }

    fun getAllPeers(): List<Feed> {
        return feedDAO.getAll()
    }

    fun getPeersFollowing(): LiveData<List<Feed>> {
        return feedDAO.getFollowing()
    }

}