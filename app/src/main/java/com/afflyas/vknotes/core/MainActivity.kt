package com.afflyas.vknotes.core

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.afflyas.afflyasnavigation.ANBottomNavigation
import com.afflyas.vknotes.R
import com.afflyas.vknotes.sp.AppSharedPrefsCommon
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKAccessTokenTracker
import com.vk.sdk.VKSdk
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    lateinit var activityViewModel: MainActivityViewModel

    /**
     * Custom factory to enable injecting into ViewModel
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * track VK access token and open LoginActivity when its become invalid
     */
    var vkAccessTokenTracker: VKAccessTokenTracker = object : VKAccessTokenTracker() {
        override fun onVKAccessTokenChanged(oldToken: VKAccessToken?, newToken: VKAccessToken?) {
            if (newToken == null) {
                // VKAccessToken is invalid
                Log.d(App.DEV_TAG, javaClass.simpleName + " VKAccessToken is invalid")
                launchLoginActivity()
            }
        }
    }

    /**
     * Close MainActivity and launch LoginActivity without animation
     */
    fun launchLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        //Enable injections
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        if (AppSharedPrefsCommon.isDarkThemeEnabled(this)) {
            setTheme(R.style.AppTheme_Translucent_Dark)
        }else{
            setTheme(R.style.AppTheme_Translucent_Light)
        }

        //Check if user is logged in
        if(!VKSdk.isLoggedIn()){
            launchLoginActivity()
            return
        }

        setContentView(R.layout.activity_main)

        //Always show notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (window.attributes as WindowManager.LayoutParams).layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        activityViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)
        subscribeUI()

        vkAccessTokenTracker.startTracking()

    }

    /**
     *
     * Fixes crash when navigate using action after popUp
     *
     */
    override fun onBackPressed() {
        if(!Navigation.findNavController(this,R.id.navHostFragment).navigateUp()){
            super.onBackPressed()
        }
    }

    private fun subscribeUI(){
        subscribeNavigation()
        subscribeFAB()
    }

    /**
     * Subscribe FAb to open EditorFragment with to args
     * Meaning to create new Note
     */
    private fun subscribeFAB(){
        fab.setOnClickListener {
            Navigation.findNavController(this@MainActivity, R.id.navHostFragment)
                    .navigate(R.id.action_global_editorFragment)
        }
    }

    private fun subscribeNavigation(){

        /**
         * Handle changing of hide on scroll preference
         */
        activityViewModel.scrollHideEnabled.observe(this, Observer {
            botNav.restoreBottomNavigation()
            topBar.restoreTopBar()

            botNav.enableBehaviorTranslation(it)
            topBar.enableBehaviorTranslation(it)
        })

        setDefaultBotNavListener()

        /**
         * Handle navigation bars when new fragment opens
         *
         * 1 - Highlight item in bot nav by currently opening fragment
         * 2 - Show FAB if destination is NotesFragment
         * 3 - Restore visible states for navigation bars
         *
         */
        activityViewModel.destinationFragment.observe(this, Observer {

            when(it){
                R.layout.fragment_notes -> {
                    botNav.setCurrentItem(0, false)
                }
                R.layout.fragment_search -> {
                    botNav.setCurrentItem(1, false)
                }
                R.layout.fragment_settings -> {
                    botNav.setCurrentItem(2, false)
                }
            }

            if(it == R.layout.fragment_notes){
                fab.show()
            }else{
                fab.hide()
            }


            botNav.restoreBottomNavigation()
            topBar.restoreTopBar()
        })
    }

    /**
     * Apply custom click listener to bot nav
     *
     * Applying fragments in bot nav menu to scroll on top / refresh data
     * when clicked item of current fragment
     */
    fun setBotNavListener(botNavListener: ANBottomNavigation.OnTabSelectedListener?){
        if(botNavListener == null){
            setDefaultBotNavListener()
        }else{
            botNav.setOnTabSelectedListener(botNavListener)
        }
    }

    /**
     * Apply default click listener to bot nav that opens fragment by clicked button
     */
    fun setDefaultBotNavListener(){
        botNav.setOnTabSelectedListener { position, _ ->
            val navController = Navigation.findNavController(this@MainActivity, R.id.navHostFragment)

            when(position){
                0 -> {
                    navController.navigate(R.id.action_global_notesFragment)
                }
                1 -> {
                    navController.navigate(R.id.action_global_searchFragment)
                }
                2 -> {
                    navController.navigate(R.id.action_global_settingsFragment)
                }
            }
            true
        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

}


