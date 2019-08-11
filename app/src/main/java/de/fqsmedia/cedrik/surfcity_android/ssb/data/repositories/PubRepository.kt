package de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories

import android.content.Context
import de.fqsmedia.cedrik.surfcity_android.dagger.DaggerSurfCityComponent
import de.fqsmedia.cedrik.surfcity_android.dagger.SurfCityModule
import de.fqsmedia.cedrik.surfcity_android.ssb.data.SurfCityDatabase
import de.fqsmedia.cedrik.surfcity_android.ssb.data.daos.PubDAO
import de.fqsmedia.cedrik.surfcity_android.ssb.data.entities.Pub
import javax.inject.Inject

class PubRepository(context: Context) {
    @Inject
    lateinit var database: SurfCityDatabase
    private val pubDAO: PubDAO

    init {
        DaggerSurfCityComponent.builder().module(SurfCityModule(context)).build().inject(this)
        pubDAO = database.pubDAO()
    }

    fun getAllPubs(): List<Pub> {
        return pubDAO.getAll()
    }
}