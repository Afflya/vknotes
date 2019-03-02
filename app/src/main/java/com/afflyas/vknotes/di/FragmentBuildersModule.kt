package com.afflyas.vknotes.di

import com.afflyas.vknotes.ui.editor.EditorFragment
import com.afflyas.vknotes.ui.notes.NotesFragment
import com.afflyas.vknotes.ui.search.SearchFragment
import com.afflyas.vknotes.ui.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeNotesFragment() : NotesFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment() : SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment() : SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeEditorFragment() : EditorFragment

}