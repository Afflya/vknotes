package com.afflyas.vknotes.repository.search

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.repository.NetworkState
import com.afflyas.vknotes.vo.VKNote
import com.vk.sdk.VKAccessToken
import java.io.IOException
import java.util.concurrent.Executor

class SearchItemKeyedDataSource (
        private val networkExecutor: Executor,
        private val vkApiService: VKApiService,
        private val query: String) : PageKeyedDataSource<Int, VKNote>(){

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

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, VKNote>) {
        val request = vkApiService.searchNotes(
                token = token.accessToken,
                count = params.requestedLoadSize,
                offset = 0,
                userId = token.userId.toLong(),
                query = query
        )

        // update network states.
        // we also provide an initial load state to the listeners so that the UI can know when the
        // very first list is loaded.
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()

            val responseBody = response.body()?.response
            /**
             * next page exists only when total hits > page size
             */
            val nextPageExists = if(responseBody != null){
                responseBody.count > VKApiService.DEFAULT_PAGE_SIZE
            }else{
                false
            }

            val items = responseBody?.items ?: emptyList()

            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            /**
             * No previous page -> null
             * Next page if exists -> 2
             */
            callback.onResult(items, null, if (nextPageExists) VKApiService.DEFAULT_PAGE_SIZE else null)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, VKNote>) {
        //Log.d(App.DEV_TAG, javaClass.simpleName + " loadAfter")
        // set network value to loading.
        networkState.postValue(NetworkState.LOADING)
        // even though we are using async retrofit API here, we could also use sync
        // it is just different to show that the callback can be called async.

        val pageOffset: Int = params.key

        val request = vkApiService.searchNotes(
                token = token.accessToken,
                count = params.requestedLoadSize,
                offset = pageOffset,
                userId = token.userId.toLong(),
                query = query
        )

        try {
            val response = request.execute()

            if (response.isSuccessful) {

                val responseBody = response.body()?.response

                val nextPageOffset: Int?

                //set next page number as incremented current page number
                //if not all images have been loaded
                nextPageOffset = if(responseBody == null) {
                    null
                }else{
                    val totalLoaded = pageOffset * VKApiService.DEFAULT_PAGE_SIZE
                    if(responseBody.count > totalLoaded){
                        pageOffset + VKApiService.DEFAULT_PAGE_SIZE
                    }else{
                        null
                    }
                }

                val items = response.body()?.response?.items ?: emptyList()
                // clear retry since last request succeeded
                retry = null
                callback.onResult(items, nextPageOffset)
                networkState.postValue(NetworkState.LOADED)
            } else {
                retry = {
                    loadAfter(params, callback)
                }
                networkState.postValue(
                        NetworkState.error("error code: ${response.code()}"))
            }
        } catch (ioException: IOException) {
            retry = {
                loadAfter(params, callback)
            }
            networkState.postValue(NetworkState.error(ioException.message
                    ?: "unknown error"))
        }

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, VKNote>) {
        //Log.d(App.DEV_TAG, javaClass.simpleName + " loadBefore")
        // ignored, since we only ever append to our initial load
    }
}