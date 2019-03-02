package com.afflyas.vknotes.ui.editor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.core.AppExecutors
import com.afflyas.vknotes.vo.VKNote
import com.vk.sdk.VKAccessToken
import java.io.IOException
import javax.inject.Inject

class EditorViewModel @Inject constructor(
        private val appExecutors: AppExecutors,
        private val vkApiService: VKApiService): ViewModel() {

    var note: VKNote? = null

    val editingStatus = MutableLiveData<EditingStatus>()

    /**
     * edit or create note
     *
     * 1 - check if there is text for note
     * 2 - delete current one if note.id passed as argument is not 0
     * 3 - then create new note
     *
     */
    fun saveNote(){
        val note = this.note

        val text = note?.text

        if(text == null || text == "") return

        editingStatus.value = EditingStatus.PUSHING

        appExecutors.networkIO().execute {
            try {
                //Delete current note (cannot edit notes in VK)
                if(note.id > 0){
                    val requestDelete = vkApiService.deleteNotes(
                            token = VKAccessToken.currentToken().accessToken,
                            messageIds = note.id.toString())
                    val responseDelete = requestDelete.execute()
                    if(!responseDelete.isSuccessful){
                        editingStatus.postValue(EditingStatus.FAILED)
                        return@execute
                    }
                }

                val requestCreate = vkApiService.createNote(
                        token = VKAccessToken.currentToken().accessToken,
                        message = text,
                        userId = VKAccessToken.currentToken().userId
                )

                val responseCreate = requestCreate.execute()

                if(responseCreate.isSuccessful) {
                    editingStatus.postValue(EditingStatus.SUCCESS)
                }else{
                    editingStatus.postValue(EditingStatus.FAILED)
                }
            }catch (ioException: IOException){
                editingStatus.postValue(EditingStatus.FAILED)
            }
        }
    }

    /**
     * Delete note from VK by its id
     */
    fun deleteNote(){
        val note = this.note
        if(note != null && note.id > 0){
            editingStatus.value = EditingStatus.PUSHING
            appExecutors.networkIO().execute {
                try {
                    val request = vkApiService.deleteNotes(
                            token = VKAccessToken.currentToken().accessToken,
                            messageIds = note.id.toString())
                    val response = request.execute()
                    if(response.isSuccessful){
                        editingStatus.postValue(EditingStatus.SUCCESS)
                    }else{
                        editingStatus.postValue(EditingStatus.FAILED)
                    }
                }catch (ioException: IOException){
                    editingStatus.postValue(EditingStatus.FAILED)
                }
            }
        }
    }
}

/**
 * EDITING - Note is editing by user
 * PUSHING - Api call in process -> show loading UI
 * SUCCESS - Api call has been finished successful -> close fragment
 * FAILED  - Api call has been failed  -> show snack with repeat
 */
enum class EditingStatus {
    EDITING,
    PUSHING,
    SUCCESS,
    FAILED
}
