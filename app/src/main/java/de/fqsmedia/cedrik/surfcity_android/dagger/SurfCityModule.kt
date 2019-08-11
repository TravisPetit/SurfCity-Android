package de.fqsmedia.cedrik.surfcity_android.dagger

import android.content.Context
import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.SodiumAndroid
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import de.fqsmedia.cedrik.surfcity_android.ssb.data.SurfCityDatabase
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.SecretHandler
import de.fqsmedia.cedrik.surfcity_android.utils.Constants
import java.nio.charset.StandardCharsets
import javax.inject.Singleton

@Module
class SurfCityModule(private var context: Context) {

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    fun provideDB(context: Context): SurfCityDatabase = SurfCityDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideLazySodium(): LazySodiumAndroid = LazySodiumAndroid(SodiumAndroid(), StandardCharsets.UTF_8)

    @Provides
    @Singleton
    fun provideSecretHandler(): SecretHandler = SecretHandler(context)

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Constants.getMoshi()
}
