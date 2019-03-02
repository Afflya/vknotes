package com.afflyas.vknotes.ui.common

import com.afflyas.vknotes.vo.VKNote

interface ItemNoteCallback {

    /**
     * Used to open editor for clicked note
     */
    fun onItemClick(vkNote: VKNote)

}