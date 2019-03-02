package com.afflyas.vknotes.repository.notes

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.core.App
import com.afflyas.vknotes.repository.NetworkState
import com.afflyas.vknotes.vo.VKNote
import com.vk.sdk.VKAccessToken
import java.io.IOException
import java.util.concurrent.Executor

class NotesItemKeyedDataSource (
        private val networkExecutor: Executor,
        private val vkApiService: VKApiService
) : ItemKeyedDataSource<Long, VKNote>(){

    private val token = VKAccessToken.currentToken()

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            networkExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<VKNote>) {
        val request = vkApiService.getNotes(
                token = token.accessToken,
                count = params.requestedLoadSize,
                userId = token.userId.toLong()
        )

        // update network states.
        // we also provide an initial load state to the listeners so that the UI can know when the
        // very first list is loaded.
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()

            val items = response.body()?.response?.items ?: emptyList()

            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<VKNote>) {
        // set network value to loading.
        networkState.postValue(NetworkState.LOADING)
        // even though we are using async retrofit API here, we could also use sync
        // it is just different to show that the callback can be called async.
        val request = vkApiService.getNotesAfter(
                token = token.accessToken,
                count = params.requestedLoadSize,
                userId = token.userId.toLong(),
                startMessageId = params.key
        )

        try {
            val response = request.execute()

            if (response.isSuccessful) {
                val items = response.body()?.response?.items ?: emptyList()
                // clear retry since last request succeeded
                retry = null
                callback.onResult(items)
                networkState.postValue(NetworkState.LOADED)
            } else {
                retry = {
                    loadAfter(params, callback)
                }
                networkState.postValue(
                        NetworkState.error("error code: ${response.code()}"))
            }
        } catch (ioException: IOException) {
            // keep a lambda for future retry
            retry = {
                loadAfter(params, callback)
            }
            // publish the error
            networkState.postValue(NetworkState.error(ioException.message ?: "unknown error"))
        }

    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<VKNote>) {
        // ignored, since we only ever append to our initial load
    }
    /**
     * The name field is a unique identifier for message item.
     */
    override fun getKey(item: VKNote): Long {
        return item.id
    }
}