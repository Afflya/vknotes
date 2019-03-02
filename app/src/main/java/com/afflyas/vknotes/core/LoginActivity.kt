package com.afflyas.vknotes.core

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.afflyas.vknotes.R
import com.google.android.material.snackbar.Snackbar
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Activity with button to log in to VK
 * shows when user does not logged in or access token is invalid
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Always show notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (window.attributes as WindowManager.LayoutParams).layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        loginButton.setOnClickListener {
            VKSdk.login(this, "messages")
        }

    }

    /**
     *
     * Handle result of logging with VK SDK
     *
     * Open MainActivity when its success and show error message when its not
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {

                    override fun onResult(res: VKAccessToken) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    override fun onError(error: VKError) {
                        Snackbar.make(rootView,R.string.error_authentication, Snackbar.LENGTH_SHORT).show()
                    }
                })) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
