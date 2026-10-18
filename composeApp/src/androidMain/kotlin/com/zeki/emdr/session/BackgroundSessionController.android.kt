package com.zeki.emdr.session

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.zeki.emdr.haptics.AndroidHapticsHost

actual object BackgroundSessionController {
    actual fun start(playing: Boolean) {
        val context = AndroidHapticsHost.context ?: return
        requestNotificationPermission()
        val intent = Intent(context, EmdrSessionService::class.java)
            .putExtra(EmdrSessionService.EXTRA_PLAYING, playing)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    actual fun stop() {
        val context = AndroidHapticsHost.context ?: return
        context.stopService(Intent(context, EmdrSessionService::class.java))
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val activity = AndroidHapticsHost.activity ?: return
        if (activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
    }
}
