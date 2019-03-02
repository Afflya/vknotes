package com.afflyas.vknotes.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

/**
 * Data class that contains response of executing api request and its current status
 *
 * Used for requests with paging
 */
data class PagingRepoResponse<T> (
    // the LiveData of paged lists for the UI to observe
    val pagedList: LiveData<PagedList<T>>,
    // represents the network request status to show to the user
    val networkState: LiveData<NetworkState>,
    // represents the refresh status to show to the user. Separate from networkState, this
    // value is importantly only when refresh is requested.
    val refreshState: LiveData<NetworkState>,
    // refreshes the whole data and fetches it from scratch.
    val refresh: () -> Unit,
    // retries any failed requests.
    val retry: () -> Unit)