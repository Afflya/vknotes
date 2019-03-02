package com.afflyas.vknotes.repository

import androidx.lifecycle.LiveData

/**
 * Data class that contains response of executing api request and its current status
 *
 * Used for requests without paging
 */
data class SolidRepoResponse<T>(
        // the LiveData of api response for the UI to observe
        val userInfo: LiveData<T>,
        // represents the network request status to show to the user
        val networkState: LiveData<NetworkState>,
        // reloads request.
        val reload: () -> Unit)
