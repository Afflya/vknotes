package com.afflyas.vknotes.repository.notes

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.vo.VKNote
import java.util.concurrent.Executor

class NotesDataSourceFactory (
        private val networkExecutor: Executor,
        private val vkApiService: VKApiService
): DataSource.Factory<Long, VKNote>() {

    val sourceLiveData = MutableLiveData<NotesItemKeyedDataSource>()

    override fun create(): DataSource<Long, VKNote> {
        val source = NotesItemKeyedDataSource(networkExecutor, vkApiService)
        sourceLiveData.postValue(source)
        return source
    }

}