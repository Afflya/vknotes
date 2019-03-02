package com.afflyas.vknotes.ui.settings

import androidx.lifecycle.ViewModel
import com.afflyas.vknotes.repository.user.UserInfoRepository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(repository: UserInfoRepository): ViewModel() {

    private val repoResult = repository.loadUserInfo()

    val userInfo = repoResult.userInfo

    val loadingState = repoResult.networkState

    /**
     * reloads data
     */
    fun reload() {
        repoResult.reload.invoke()
    }

}
