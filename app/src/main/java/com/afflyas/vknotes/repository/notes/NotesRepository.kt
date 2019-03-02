package com.afflyas.vknotes.repository.notes

import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.core.AppExecutors
import com.afflyas.vknotes.repository.PagingRepoResponse
import com.afflyas.vknotes.vo.VKNote
import javax.inject.Inject

/**
 * Repository implementation that returns a Listing that loads data directly from network by using
 * the messages ids as keys.
 */
class NotesRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val vkApiService: VKApiService
){

    fun loadNotes() : PagingRepoResponse<VKNote>{
        val sourceFactory = NotesDataSourceFactory(appExecutors.networkIO(),vkApiService)
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(5)
                .setPageSize(VKApiService.DEFAULT_PAGE_SIZE)
                .build()
        val livePagedList = LivePagedListBuilder(sourceFactory,pagedListConfig)
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