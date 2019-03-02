package com.afflyas.vknotes.ui.common

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.appcompat.widget.Toolbar
import com.afflyas.vknotes.R
import com.afflyas.vknotes.core.MainActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Class to customize shared top bar with number of fragments
 */
class TopBarBuilder constructor(private val mainActivity: MainActivity) {

    private val resources = mainActivity.resources

    var title: String? = mainActivity.resources.getString(R.string.app_name)
    @ColorRes var topBarBackground: Int = 0
    var tintColor: Int = android.R.color.white
    @SuppressLint("PrivateResource")
    var navigationIcon: Int = androidx.appcompat.R.drawable.abc_ic_ab_back_material
    var displayNavigationButton: Boolean = false
    var customView: View? = null

    fun build(){

        val toolbar = Toolbar(mainActivity)

        toolbar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        toolbar.title = title

        val color: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = resources.getColor(tintColor, null)
        }else{
            @Suppress("DEPRECATION")
            color = resources.getColor(tintColor)
        }

        if(topBarBackground == 0){
            val typedValue = TypedValue()
            val theme = mainActivity.theme
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            topBarBackground = typedValue.data
        }

        mainActivity.topBar.setBackgroundColor(topBarBackground)

        toolbar.setTitleTextColor(color)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toolbar.navigationIcon = resources.getDrawable(navigationIcon, null)
                toolbar.navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

                //mainActivity.topBar.background = resources.getDrawable(topBarBackground, null)
            }else{
                @Suppress("DEPRECATION")
                toolbar.navigationIcon = resources.getDrawable(navigationIcon)
                toolbar.navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

                //@Suppress("DEPRECATION")
                //mainActivity.topBar.background = resources.getDrawable(topBarBackground)
            }
        }

        if(customView != null){
            toolbar.setContentInsetsAbsolute(0,0)
            toolbar.setContentInsetsRelative(0,0)
            toolbar.addView(customView)
        }

        mainActivity.topBar.removeAllViewsInLayout()
        mainActivity.topBar.addView(toolbar)

        mainActivity.setSupportActionBar(toolbar)

        val actionBar = mainActivity.supportActionBar
        //actionBar?.setDisplayShowHomeEnabled(displayNavigationButton)
        actionBar?.setDisplayHomeAsUpEnabled(displayNavigationButton)


    }

}