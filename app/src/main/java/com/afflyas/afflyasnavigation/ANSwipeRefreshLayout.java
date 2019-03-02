package com.afflyas.afflyasnavigation;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DisplayCutout;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 *
 * SwipeRefreshLayout that adds padding to avoid notch and translucent status bar
 *
 * Soon will be released in afflyasnavigation library
 */
public class ANSwipeRefreshLayout extends SwipeRefreshLayout {

    private boolean translucentNavigationThemeEnabled = false;
    private boolean translucentStatusThemeEnabled = false;

    /**
     *
     * system window inset values that has been set as padding
     *
     */
    private int insetTop = 0;
    private int insetLeft = 0;
    private int insetRight = 0;

    /**
     * indicates that insets were set
     */
    private boolean isInsetsSet = false;

    public ANSwipeRefreshLayout(@NonNull Context context) {
        super(context);
        init(context,null);
    }

    public ANSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        /*
         * xml attributes
         */
//        int topBarHeightMode = 1;
//
//        if (attrs != null) {
//            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ANSwipeRefreshLayout, 0, 0);
//            try {
//                topBarHeightMode = ta.getInt(R.styleable.ANSwipeRefreshLayout_topBarHeightMode, 1);
//            } finally {
//                ta.recycle();
//            }
//        }
//        switch(topBarHeightMode){
//            case 1:
//                topBarHeight = (int)getResources().getDimension(R.dimen.space_top_only_action_bar);
//                break;
//            case 2:
//                topBarHeight = (int)getResources().getDimension(R.dimen.space_top_action_bar_with_tabs);
//                break;
//            case 3:
//            default:
//                topBarHeight = 0;
//                break;
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            translucentNavigationThemeEnabled = ANHelper.isTranslucentNavigationThemeEnabled(context);
            translucentStatusThemeEnabled = ANHelper.isTranslucentStatusThemeEnabled(context);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            calcInsets();
        }
        if(!isInsetsSet){
            isInsetsSet = true;
            setPadding(
                    insetLeft,
                    0,
                    insetRight,
                    0);

//            Log.d(App.DEV_TAG, "\nInsetTop="+insetTop
//                    + "\ntopBarHeight="+topBarHeight
//                    +"\nProgressViewStart="+getProgressViewStartOffset()
//                    + "\nProgressViewEnd="+getProgressViewEndOffset());

            setProgressViewOffset(false, getProgressViewStartOffset() + insetTop, getProgressViewEndOffset() + insetTop);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void calcInsets(){
        int insetLeft = 0;
        int insetRight = 0;
        int insetTop = 0;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            /*
             * P(and later) insets with cutout support
             */
            WindowInsets insets = getRootView().getRootWindowInsets();

            if(translucentStatusThemeEnabled) insetTop = insets.getSystemWindowInsetTop();

            DisplayCutout notch = insets.getDisplayCutout();

            if(ANHelper.isInMultiWindow(getContext())){
                /*
                 *
                 * Set inset when in multi window mode
                 * Only for side with cutout but without navigation
                 *
                 */
                if(notch != null){
                    insetLeft = insets.getSystemWindowInsetLeft();
                    insetRight = insets.getSystemWindowInsetRight();
                    /*
                     * stable insets -insets without notch
                     */
                    if(insets.getStableInsetLeft() != 0) insetLeft = 0;
                    if(insets.getStableInsetRight() != 0) insetRight = 0;
                }
            }else{
                if(translucentNavigationThemeEnabled){
                    insetLeft = insets.getSystemWindowInsetLeft();
                    insetRight = insets.getSystemWindowInsetRight();
                }else{
                    if(notch != null){
                        insetLeft = notch.getSafeInsetLeft();
                        insetRight = notch.getSafeInsetRight();
                        /*
                         * stable insets -insets without notch
                         */
                        if(insets.getStableInsetLeft() != 0) insetLeft = 0;
                        if(insets.getStableInsetRight() != 0) insetRight = 0;
                    }
                }
            }
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            /*
             * Nougat and Oreo insets
             */
            WindowInsets insets = getRootView().getRootWindowInsets();
            if(translucentStatusThemeEnabled) insetTop = insets.getSystemWindowInsetTop();
            /*
             * No insets in multi window mode
             */
            if(translucentNavigationThemeEnabled && !ANHelper.isInMultiWindow(getContext())){
                insetLeft = insets.getSystemWindowInsetLeft();
                insetRight = insets.getSystemWindowInsetRight();
            }
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            /*
             * Marshmallow insets
             */
            WindowInsets insets = getRootView().getRootWindowInsets();
            if(translucentStatusThemeEnabled) insetTop = insets.getSystemWindowInsetTop();

            if(translucentNavigationThemeEnabled){
                insetLeft = insets.getSystemWindowInsetLeft();
                insetRight = insets.getSystemWindowInsetRight();
            }
        }else{
            /*
             * Lollipop insets
             */
            if(translucentStatusThemeEnabled) insetTop = getResources().getDimensionPixelOffset(R.dimen.android_status_bar_height);

            if(translucentNavigationThemeEnabled
                    && ANHelper.hasNavigationBar(getContext())
                    && (!getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)
                    && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    ){
                insetRight = getResources().getDimensionPixelOffset(R.dimen.navigation_bar_height);
            }
        }

        /*
         * Check if these values are already set
         */
        if(insetLeft != this.insetLeft || insetRight != this.insetRight || insetTop != this.insetTop){
            this.insetLeft = insetLeft;
            this.insetRight = insetRight;
            this.insetTop = insetTop;

            isInsetsSet = false;
        }
    }

}
