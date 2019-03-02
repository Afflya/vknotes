package com.afflyas.vknotes.ui.notes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.afflyas.afflyasnavigation.ANBottomNavigation
import com.afflyas.vknotes.R
import com.afflyas.vknotes.core.MainActivity
import com.afflyas.vknotes.core.MainActivityViewModel
import com.afflyas.vknotes.databinding.FragmentNotesBinding
import com.afflyas.vknotes.repository.Status
import com.afflyas.vknotes.ui.common.ItemNoteCallback
import com.afflyas.vknotes.ui.common.RetryCallback
import com.afflyas.vknotes.ui.common.TopBarBuilder
import com.afflyas.vknotes.vo.VKNote
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class NotesFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = R.layout.fragment_notes
    }

    @Inject
    lateinit var mainActivity: MainActivity

    /**
     * Custom factory to enable injecting into ViewModel
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var fragmentBinding: FragmentNotesBinding

    private lateinit var notesViewModel: NotesViewModel

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
         * set default top bar
         */
        TopBarBuilder(mainActivity).build()

        return fragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activityViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(MainActivityViewModel::class.java)
        /**
         * Set current destination to highlight item in bot nav
         */
        activityViewModel.destinationFragment.value = FRAGMENT_TAG

        notesViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(NotesViewModel::class.java)

        subscribeUI()
    }

    private fun subscribeUI(){

        val state = notesViewModel.networkState.value?.status
        if(state == null || state == Status.FAILED){
            notesViewModel.retry()
        }

        /**
         *
         * Set bot nav listener to scroll on top / refresh data when clicked on item of this fragment
         *
         */
        mainActivity.setBotNavListener(ANBottomNavigation.OnTabSelectedListener { position, _ ->
            val navController = NavHostFragment.findNavController(this@NotesFragment)

            when(position){
                0 -> {
                    if((fragmentBinding.recyclerView.layoutManager as LinearLayoutManager)
                            .findFirstCompletelyVisibleItemPosition() == 0){
                        notesViewModel.refresh()
                    }else{
                        fragmentBinding.recyclerView.smoothScrollToPosition(0)
                    }
                }
                1 -> {
                    navController.navigate(R.id.action_global_searchFragment)
                }
                2 -> {
                    navController.navigate(R.id.action_global_settingsFragment)
                }
            }
            true
        })

        subscribeAdapter()
        subscribeSwipeRefresh()

    }

    private val retryCall = object : RetryCallback {
        override fun retry() {
            notesViewModel.retry()
        }
    }

    /**
     * Open EditorFragment and pass clicked VKNote as argument
     */
    private val openNoteCall = object : ItemNoteCallback {
        override fun onItemClick(vkNote: VKNote) {
            val navController = NavHostFragment.findNavController(this@NotesFragment)
            val args = Bundle()
            args.putParcelable("note", vkNote)
            navController.navigate(R.id.action_global_editorFragment, args)
        }
    }

    /**
     * create adapter and set it to recycler view
     *
     * subscribe adapter to observe network state and list of items
     */
    private fun subscribeAdapter(){
        val adapter = NotesPagedAdapter(retryCall, openNoteCall)

        fragmentBinding.recyclerView.adapter = adapter

        notesViewModel.notes.observe(this, Observer<PagedList<VKNote>> {
            adapter.submitList(it)
        })

        notesViewModel.networkState.observe(this, Observer {
            fragmentBinding.networkState = it
            adapter.setNetworkState(it)
        })

    }
    /**
     * subscribe swipe refresh to observe refresh state and call refresh method in the view model
     */
    private fun subscribeSwipeRefresh(){
        notesViewModel.refreshState.observe(this, Observer { refreshState ->
            fragmentBinding.refreshState = refreshState
        })

        fragmentBinding.swipeRefresh.setOnRefreshListener {
            notesViewModel.refresh()
        }

    }
}
