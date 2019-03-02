package com.afflyas.vknotes.core

import android.app.Activity
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.afflyas.vknotes.di.DaggerAppComponent
import com.vk.sdk.VKSdk
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKAccessTokenTracker



class App : Application(), HasActivityInjector {

    companion object {
        const val DEV_TAG = "myDev"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
//        Log.d(DEV_TAG, javaClass.simpleName + " onCreate()")

        //fix crashes in Android 4.* devices caused by vector drawables
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        //init Dagger injections
        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this)

        VKSdk.initialize(this)
    }

    override fun activityInjector() = dispatchingAndroidInjector
}