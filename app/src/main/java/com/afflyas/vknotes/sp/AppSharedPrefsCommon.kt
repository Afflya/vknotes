package com.afflyas.vknotes.sp

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

object AppSharedPrefsCommon {

    const val KEY_DARK_THEME = "dark_theme"
    const val KEY_SCROLL_HIDE = "scroll_hide"

    /**
     * Dark ui theme
     */
    fun isDarkThemeEnabled(context: Context): Boolean {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(KEY_DARK_THEME, false)
    }

    fun setDarkThemeEnabled(context: Context, darkThemeEnabled: Boolean) {
        PreferenceManager
                .getDefaultSharedPreferences(context).edit {
                    putBoolean(KEY_DARK_THEME, darkThemeEnabled)
                }
    }


    /**
     * Hide top and bottom bars on scroll
     */
    fun isScrollHideEnabled(context: Context): Boolean {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(KEY_SCROLL_HIDE, true)
    }

    fun setScrollHideEnabled(context: Context, scrollHideEnabled: Boolean) {
        PreferenceManager
                .getDefaultSharedPreferences(context).edit {
                    putBoolean(KEY_SCROLL_HIDE, scrollHideEnabled)
                }
    }

}