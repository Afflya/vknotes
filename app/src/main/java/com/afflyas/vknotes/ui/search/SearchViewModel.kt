package com.afflyas.vknotes.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.afflyas.vknotes.repository.search.SearchRepository
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val repository: SearchRepository): ViewModel() {

    /**
     * String to search for
     */
    private val query = MutableLiveData<String>()

    private val repoResult = Transformations.map(query) {
        repository.loadNotes(it)
    }
    /**
     * the LiveData of paged lists for the UI to observe
     */
    val notes = Transformations.switchMap(repoResult) { it.pagedList }!!
    /**
     * represents the refresh status to show to the user. Separate from networkState, this
     * value is importantly only when refresh is requested.
     */
    val networkState = Transformations.switchMap(repoResult) { it.networkState }!!
    /**
     * represents the network request status to show to the user
     */
    val refreshState = Transformations.switchMap(repoResult) { it.refreshState }!!

    /**
     * get current query value
     */
    fun currentQuery(): String? = query.value
    /**
     * set queryString and execute search request if it is not the same as current query
     *
     * @param queryString - string to search for
     * @return request has been accepted
     */
    fun loadNotes(queryString: String?) : Boolean{
        if (query.value == queryString) {
            return false
        }
        query.value = queryString
        return true
    }
    /**
     * refreshes the whole data and fetches it from scratch.
     */
    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }
    /**
     * retries any failed requests.
     */
    fun retry(){
        val data = repoResult.value
        data?.retry?.invoke()
    }

}