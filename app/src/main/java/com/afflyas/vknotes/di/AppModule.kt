package com.afflyas.vknotes.di

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.afflyas.vknotes.api.VKApiService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    /**
     *
     * Provide single retrofit instance of VKApiService
     *
     * @return instance of VKApiService
     */
    @Singleton
    @Provides
    fun provideApiService(): VKApiService {
        return Retrofit.Builder()
                .baseUrl(VKApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VKApiService::class.java)
    }

    /**
     *
     * Provide instance of default SharedPreferences
     *
     * @return SharedPreferences
     */
    @Provides
    fun provideSharedPrefs(app: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }

}