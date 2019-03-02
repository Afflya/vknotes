package com.afflyas.vknotes.ui.editor

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.afflyas.vknotes.R
import com.afflyas.vknotes.core.MainActivity
import com.afflyas.vknotes.core.MainActivityViewModel
import com.afflyas.vknotes.databinding.FragmentEditorBinding
import com.afflyas.vknotes.ui.common.TopBarBuilder
import com.afflyas.vknotes.ui.notes.NotesViewModel
import com.afflyas.vknotes.ui.search.SearchViewModel
import com.afflyas.vknotes.vo.VKNote
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class EditorFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = R.layout.fragment_editor
    }

    @Inject
    lateinit var mainActivity: MainActivity

    /**
     * Custom factory to enable injecting into ViewModel
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var fragmentBinding: FragmentEditorBinding

    private lateinit var editorViewModel: EditorViewModel

    private lateinit var notesViewModel: NotesViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var activityViewModel: MainActivityViewModel


    /**
     * Enable injections
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        fragmentBinding = DataBindingUtil.inflate(inflater,FRAGMENT_TAG, container, false)

        /**
         * set top bar with back arrow
         */
        TopBarBuilder(mainActivity)
                .apply {
                    displayNavigationButton = true
                }
                .build()

        setHasOptionsMenu(true)

        //set default listener
        mainActivity.setDefaultBotNavListener()

        return fragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activityViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(MainActivityViewModel::class.java)

        activityViewModel.destinationFragment.value = FRAGMENT_TAG

        editorViewModel = ViewModelProviders.of(this, viewModelFactory).get(EditorViewModel::class.java)

        notesViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(NotesViewModel::class.java)

        searchViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(SearchViewModel::class.java)

        subscribeUI()
    }

    private fun subscribeUI(){

        /**
         * try to load VKNote to edit.
         * When its null then create new one
         */
        if(editorViewModel.note == null){
            //get VK note for args or create empty one
            editorViewModel.note = EditorFragmentArgs.fromBundle(arguments).note

            if(editorViewModel.note == null) editorViewModel.note = VKNote(0,0,null)

            fragmentBinding.noteText.setText(editorViewModel.note!!.text, TextView.BufferType.EDITABLE)
        }

        /**
         *
         * Track status of currently editing note
         *
         * show loading state when pushing to VK in process
         *
         * return back and refresh search and notes requests when pushing ended successful
         *
         * show error toast when pushing failed
         */
        editorViewModel.editingStatus.observe(this, Observer {

            fragmentBinding.isPushing = it == EditingStatus.PUSHING

            if(it == EditingStatus.SUCCESS){
                Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show()

                //Notify that current data has changed
                searchViewModel.refresh()
                notesViewModel.refresh()

                mainActivity.onBackPressed()
            }else if(it == EditingStatus.FAILED){
                Toast.makeText(context, R.string.error_edit, Toast.LENGTH_SHORT).show()
            }
        })

        if(editorViewModel.editingStatus.value == null){
            editorViewModel.editingStatus.value = EditingStatus.EDITING
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.editor_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * show delete button only when editing exist note
     */
    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.findItem(R.id.delete)?.isVisible = editorViewModel.note?.id != 0L
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //Hide the keyboard
        if(mainActivity.currentFocus != null){
            val imm = mainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(mainActivity.currentFocus!!.windowToken, 0)
        }

        //avoid same clicks
        if(editorViewModel.editingStatus.value == EditingStatus.PUSHING){
            return super.onOptionsItemSelected(item)
        }

        when(item.itemId){
            android.R.id.home -> {
                mainActivity.onBackPressed()
            }
            R.id.delete -> {
                //delete note from VK
                editorViewModel.deleteNote()
            }
            R.id.save -> {
                //save note to VK when text is not empty
                editorViewModel.note?.text = fragmentBinding.noteText.text.toString()
                editorViewModel.saveNote()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
