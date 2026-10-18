package com.zeki.emdr.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.zeki.emdr.MainActivity

class EmdrSessionService : Service() {
    private var mediaSession: MediaSession? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var playing: Boolean = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        ensureSession()
        publishPlayback(playing = true)
        startInForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                playing = false
                SessionRemoteControls.pause?.invoke()
            }
            ACTION_PLAY -> {
                playing = true
                SessionRemoteControls.resume?.invoke()
            }
            ACTION_STOP -> {
                SessionRemoteControls.stop?.invoke() ?: stopSelf()
                return START_NOT_STICKY
            }
        }
        if (intent?.hasExtra(EXTRA_PLAYING) == true) {
            playing = intent.getBooleanExtra(EXTRA_PLAYING, true)
        }
        ensureSession()
        publishPlayback(playing)
        startInForeground()
        return START_STICKY
    }

    override fun onDestroy() {
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    private fun startInForeground() {
        ensureChannel()
        val launch = activityIntent()
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("EMDR Companion")
            .setContentText(if (playing) "Bilateral session is running" else "Session paused")
            .setSmallIcon(if (playing) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
            .setContentIntent(launch)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        if (playing) {
            builder.addAction(action(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE, 1))
        } else {
            builder.addAction(action(android.R.drawable.ic_media_play, "Resume", ACTION_PLAY, 2))
        }
        builder.addAction(action(android.R.drawable.ic_menu_close_clear_cancel, "Stop", ACTION_STOP, 3))
        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureSession() {
        if (mediaSession != null) return
        val session = MediaSession(this, SESSION_TAG).apply {
            setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "EMDR Companion")
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "Bilateral session")
                    .build(),
            )
            setCallback(object : MediaSession.Callback() {
                // Lock-screen and OEM media controllers send onPause() when
                // the display turns off. Only the notification actions may
                // pause or stop a clinical set.
            })
            isActive = true
        }
        mediaSession = session
    }

    private fun publishPlayback(playing: Boolean) {
        val state = if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY)
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, if (playing) 1f else 0f)
                .build(),
        )
    }

    private fun action(icon: Int, title: String, action: String, requestCode: Int): Notification.Action =
        Notification.Action.Builder(icon, title, serviceIntent(action, requestCode)).build()

    private fun activityIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun serviceIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, EmdrSessionService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun acquireWakeLock() {
        if (wakeLock != null) return
        val lock = getSystemService(PowerManager::class.java)
            ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_TAG)
            ?: return
        lock.setReferenceCounted(false)
        lock.acquire(60 * 60 * 1000L)
        wakeLock = lock
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Session playback",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps bilateral audio running while the screen is locked."
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_STOP = "com.zeki.emdr.session.STOP"
        const val ACTION_PAUSE = "com.zeki.emdr.session.PAUSE"
        const val ACTION_PLAY = "com.zeki.emdr.session.PLAY"
        const val EXTRA_PLAYING = "playing"
        private const val CHANNEL_ID = "emdr_session_playback"
        private const val NOTIFICATION_ID = 41
        private const val SESSION_TAG = "emdr-session"
        private const val WAKE_TAG = "emdr:session"
    }
}
