package com.afflyas.vknotes.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.afflyas.vknotes.R
import com.afflyas.vknotes.core.MainActivity
import com.afflyas.vknotes.core.MainActivityViewModel
import com.afflyas.vknotes.databinding.FragmentSettingsBinding
import com.afflyas.vknotes.repository.Status
import com.afflyas.vknotes.sp.AppSharedPrefsCommon
import com.afflyas.vknotes.ui.common.TopBarBuilder
import com.vk.sdk.VKSdk
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class SettingsFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = R.layout.fragment_settings
    }

    @Inject
    lateinit var mainActivity: MainActivity

    /**
     * Custom factory to enable injecting into ViewModel
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var fragmentBinding: FragmentSettingsBinding

    private lateinit var fragmentViewModel: SettingsViewModel

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

        mainActivity.setDefaultBotNavListener()

        return fragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activityViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(MainActivityViewModel::class.java)
        /**
         * Set current destination to highlight item in bot nav
         */
        activityViewModel.destinationFragment.value = FRAGMENT_TAG

        fragmentViewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(SettingsViewModel::class.java)

        subscribeUI()
    }

    private fun subscribeUI(){

        /**
         * Reload request when fragment first open or when request was failed
         */
        val state = fragmentViewModel.loadingState.value?.status
        if(state == null || state == Status.FAILED){
            fragmentViewModel.reload()
        }

        /**
         * update user's info in data binding
         */
        fragmentViewModel.userInfo.observe(this, Observer {
            fragmentBinding.user = it
        })

        /**
         * show toast with error message when request fails
         */
        fragmentViewModel.loadingState.observe(this, Observer { loadingState ->
            fragmentBinding.networkState = loadingState
            if(loadingState.status == Status.FAILED){
                Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show()
            }
        })

        /**
         * logout from VK and launch Loginctivity
         */
        fragmentBinding.logoutButton.setOnClickListener {
            VKSdk.logout()
            mainActivity.launchLoginActivity()
        }

        /**
         * Reload all data on swipe refresh
         */
        fragmentBinding.swipeRefresh.setOnRefreshListener {
            fragmentViewModel.reload()
        }


        /**
         * Change theme preference and recreate acitvity
         */
        fragmentBinding.themeSwitch.isChecked = AppSharedPrefsCommon.isDarkThemeEnabled(mainActivity)
        fragmentBinding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppSharedPrefsCommon.setDarkThemeEnabled(mainActivity, isChecked)
            mainActivity.recreate()
        }


        /**
         * change preferences on switch
         */
        fragmentBinding.scrollSwitch.isChecked = AppSharedPrefsCommon.isScrollHideEnabled(mainActivity)
        fragmentBinding.scrollSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppSharedPrefsCommon.setScrollHideEnabled(mainActivity, isChecked)
        }

        /**
         * Open app page in play market
         */
        fragmentBinding.review.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.afflyas.vknotes")
            startActivity(intent)
        }

        /**
         * Open promoted app page in play market
         */
        fragmentBinding.memoryLeak.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.afflyas.memoryleak")
            startActivity(intent)
        }
    }


}
