package de.fqsmedia.cedrik.surfcity_android.dagger

import dagger.Component
import de.fqsmedia.cedrik.surfcity_android.MainActivity
import de.fqsmedia.cedrik.surfcity_android.ssb.SurfCity
import de.fqsmedia.cedrik.surfcity_android.ssb.data.models.MessageModel
import de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories.MessageRepository
import de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories.FeedRepository
import de.fqsmedia.cedrik.surfcity_android.ssb.data.repositories.PubRepository
import javax.inject.Singleton

@Singleton
@Component(modules = [SurfCityModule::class])
interface SurfCityComponent {
    fun inject(activity: MainActivity)
    fun inject(repository: MessageRepository)
    fun inject(messageModel: MessageModel)
    fun inject(repository: FeedRepository)
    fun inject(repository: PubRepository)
    fun inject(surfCity: SurfCity)

    @Component.Builder
    interface Builder {
        fun build(): SurfCityComponent
        fun module(surfCityModule: SurfCityModule): Builder
    }
}