package com.afflyas.vknotes.repository.search

import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.core.AppExecutors
import com.afflyas.vknotes.repository.PagingRepoResponse
import com.afflyas.vknotes.vo.VKNote
import javax.inject.Inject

class SearchRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val vkApiService: VKApiService
){

    fun loadNotes(query: String) : PagingRepoResponse<VKNote> {
        val sourceFactory = SearchDataSourceFactory(appExecutors.networkIO(),vkApiService, query)

        val livePagedList = LivePagedListBuilder(sourceFactory,VKApiService.DEFAULT_PAGE_SIZE)
                // provide custom executor for network requests, otherwise it will default to
                // Arch Components' IO pool which is also used for disk access
                .setFetchExecutor(appExecutors.networkIO())
                .build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }

        return PagingRepoResponse(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }

}