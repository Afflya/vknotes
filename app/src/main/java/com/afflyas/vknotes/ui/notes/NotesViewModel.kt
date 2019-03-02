package com.afflyas.vknotes.ui.notes

import androidx.lifecycle.ViewModel
import com.afflyas.vknotes.repository.notes.NotesRepository
import javax.inject.Inject

class NotesViewModel @Inject constructor(private val repository: NotesRepository): ViewModel() {

    private val repoResult = repository.loadNotes()
    /**
     * the LiveData of paged lists for the UI to observe
     */
    val notes = repoResult.pagedList
    /**
     * represents the refresh status to show to the user. Separate from networkState, this
     * value is importantly only when refresh is requested.
     */
    val networkState = repoResult.networkState
    /**
     * represents the network request status to show to the user
     */
    val refreshState = repoResult.refreshState
    /**
     * refresh all the data
     */
    fun refresh() {
        repoResult.refresh.invoke()
    }
    /**
     * retries any failed requests.
     */
    fun retry(){
        if(notes.value == null){
            refresh()
        }else{
            repoResult.retry.invoke()
        }
    }
}
