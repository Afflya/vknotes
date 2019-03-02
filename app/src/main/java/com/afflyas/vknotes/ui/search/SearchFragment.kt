package com.afflyas.vknotes.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
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
import com.afflyas.vknotes.databinding.FragmentSearchBinding
import com.afflyas.vknotes.ui.common.ItemNoteCallback
import com.afflyas.vknotes.ui.common.RetryCallback
import com.afflyas.vknotes.ui.common.TopBarBuilder
import com.afflyas.vknotes.vo.VKNote
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SearchFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = R.layout.fragment_search
    }

    @Inject
    lateinit var mainActivity: MainActivity

    /**
     * Custom factory to enable injecting into ViewModel
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var fragmentBinding: FragmentSearchBinding

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var activityViewModel: MainActivityViewModel

    private lateinit var searchView : SearchView

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
         * set top bar with search view in it
         */
        searchView = inflater.inflate(R.layout.topbar_search,container,false) as SearchView
        TopBarBuilder(mainActivity).apply {
            customView = searchView
        }.build()

        return fragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activityViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(MainActivityViewModel::class.java)
        /**
         * Set current destination to highlight item in bot nav
         */
        activityViewModel.destinationFragment.value = FRAGMENT_TAG

        searchViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(SearchViewModel::class.java)

        subscribeUI()
    }

    private fun subscribeUI(){

        subscripeAdapter()
        subscribeSwipeRefresh()
        subscribeSearch()

        /**
         *
         * Set bot nav listener to scroll on top / refresh data when clicked on item of this fragment
         *
         */
        mainActivity.setBotNavListener(ANBottomNavigation.OnTabSelectedListener { position, _ ->
            val navController = NavHostFragment.findNavController(this@SearchFragment)

            when(position){
                0 -> {
                    navController.navigate(R.id.action_global_notesFragment)
                }
                1 -> {
                    if((fragmentBinding.recyclerView.layoutManager as LinearLayoutManager)
                                    .findFirstCompletelyVisibleItemPosition() == 0){
                        searchViewModel.refresh()
                    }else{
                        fragmentBinding.recyclerView.smoothScrollToPosition(0)
                    }
                }
                2 -> {
                    navController.navigate(R.id.action_global_settingsFragment)
                }
            }
            true
        })

    }

    /**
     * create adapter and set it to recycler view
     *
     * subscribe adapter to observe network state and list of items
     */
    private fun subscripeAdapter(){
        val adapter = SearchPagedAdapter(retryCall, openNoteCall)

        fragmentBinding.recyclerView.adapter = adapter

        searchViewModel.notes.observe(this, Observer<PagedList<VKNote>> {
            adapter.submitList(it)
        })

        searchViewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })

    }

    /**
     * subscribe swipe refresh to observe refresh state and call refresh method in the view model
     */
    private fun subscribeSwipeRefresh(){
        searchViewModel.refreshState.observe(this, Observer {
            fragmentBinding.refreshState = it
        })

        fragmentBinding.swipeRefresh.setOnRefreshListener {
            if(searchViewModel.currentQuery().isNullOrEmpty()) {
                fragmentBinding.swipeRefresh.isRefreshing = false
            }else{
                searchViewModel.refresh()
            }
        }
    }
    /**
     * subscribe search view to call load images after text submit
     */
    private fun subscribeSearch(){

        if(searchView.query.isEmpty())
            searchView.setQuery(searchViewModel.currentQuery(), false)


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {

                //Hide the keyboard
                if(mainActivity.currentFocus != null){
                    val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(mainActivity.currentFocus!!.windowToken, 0)
                }

                if(query.isNotEmpty()){
                    if(searchViewModel.loadNotes(query)){
                        fragmentBinding.recyclerView.scrollToPosition(0)
                        (fragmentBinding.recyclerView.adapter as? SearchPagedAdapter)?.submitList(null)
                    }
                }
                return false
            }

            override fun onQueryTextChange(query: String): Boolean { return false }
        })
    }

    private val retryCall = object : RetryCallback {
        override fun retry() {
            searchViewModel.retry()
        }
    }

    /**
     * Open EditorFragment and pass clicked VKNote as argument
     */
    private val openNoteCall = object : ItemNoteCallback {
        override fun onItemClick(vkNote: VKNote) {
            val navController = NavHostFragment.findNavController(this@SearchFragment)
            val args = Bundle()
            args.putParcelable("note", vkNote)
            navController.navigate(R.id.action_global_editorFragment, args)
        }
    }

}
