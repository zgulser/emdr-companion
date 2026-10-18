package com.zeki.emdr

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zeki.emdr.billing.SupportForeground
import com.zeki.emdr.haptics.AndroidHapticsHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        AndroidHapticsHost.context = applicationContext
        AndroidHapticsHost.activity = this
        setContent { EmdrApp() }
    }

    override fun onResume() {
        super.onResume()
        AndroidHapticsHost.activity = this
        SupportForeground.onResume?.invoke()
    }

    override fun onDestroy() {
        if (AndroidHapticsHost.activity === this) {
            AndroidHapticsHost.activity = null
        }
        super.onDestroy()
    }
}
