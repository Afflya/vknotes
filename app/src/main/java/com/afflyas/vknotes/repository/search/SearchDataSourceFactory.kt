package com.afflyas.vknotes.repository.search

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.vo.VKNote
import java.util.concurrent.Executor

class SearchDataSourceFactory (
        private val networkExecutor: Executor,
        private val vkApiService: VKApiService,
        private val query: String
): DataSource.Factory<Int, VKNote>() {

    val sourceLiveData = MutableLiveData<SearchItemKeyedDataSource>()

    override fun create(): DataSource<Int, VKNote> {
        val source = SearchItemKeyedDataSource(networkExecutor, vkApiService, query)
        sourceLiveData.postValue(source)
        return source
    }

}
