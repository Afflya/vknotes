package com.afflyas.vknotes.di

import com.afflyas.vknotes.core.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Module for main activity
 */
@Suppress("unused")
@Module
abstract class MainActivityModule {
    /**
     * Generate sub component with fragment's modules in it
     */
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity
}