package com.afflyas.vknotes.binding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

/**
 *
 * Additional attributes for xml views using data binding
 *
 */
object BindingAdapters {

    /**
     * Set visibility by passing boolean value
     */
    @JvmStatic
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Load image from url using Glide
     */
    @JvmStatic
    @BindingAdapter("imageFromUrl")
    fun loadImageFromUrl(imageView: ImageView, url: String?) {
        if(url == null) return
        Glide.with(imageView.context).load(url).into(imageView)
    }

}