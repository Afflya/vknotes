package com.afflyas.vknotes.core

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afflyas.vknotes.sp.AppSharedPrefsCommon
import com.afflyas.vknotes.sp.booleanLiveData
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(sPrefs: SharedPreferences): ViewModel(){

    /**
     * Contains value R.layout.* of displayed fragment
     */
    val destinationFragment = MutableLiveData<Int>()

    /**
     * Listen preference to enable hide top and bottom bars on content scrolling
     */
    val scrollHideEnabled = sPrefs.booleanLiveData(AppSharedPrefsCommon.KEY_SCROLL_HIDE, true)

}