package com.zeki.emdr.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.zeki.emdr.MainActivity
import com.zekinthos.emdrcompanion.R

class EmdrSessionService : Service() {
    private var mediaSession: MediaSessionCompat? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var playing: Boolean = true
    private val main = Handler(Looper.getMainLooper())

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
        val session = ensureSession()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EMDR Companion Session")
            .setContentText(
                if (playing) {
                    "Bilateral stimulation in progress..."
                } else {
                    "Session paused"
                },
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(activityIntent())
            .setStyle(
                MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(serviceIntent(ACTION_STOP, 3)),
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (playing) {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", serviceIntent(ACTION_PAUSE, 1))
        } else {
            builder.addAction(android.R.drawable.ic_media_play, "Resume", serviceIntent(ACTION_PLAY, 2))
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", serviceIntent(ACTION_STOP, 3))
        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureSession(): MediaSessionCompat {
        mediaSession?.let { return it }
        val session = MediaSessionCompat(this, SESSION_TAG).apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS,
            )
            setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "EMDR Companion Session")
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Bilateral stimulation")
                    .build(),
            )
            setCallback(
                object : MediaSessionCompat.Callback() {
                    override fun onPlay() = runOnMain { applyRemotePlay() }
                    override fun onPause() = runOnMain {
                        if (screenIsInteractive()) applyRemotePause()
                    }
                    override fun onStop() = runOnMain { applyRemoteStop() }
                    override fun onCustomAction(action: String?, extras: android.os.Bundle?) {
                        if (action == ACTION_STOP) runOnMain { applyRemoteStop() }
                    }
                },
                main,
            )
            isActive = true
        }
        mediaSession = session
        return session
    }

    private fun publishPlayback(playing: Boolean) {
        val state = if (playing) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }
        val actions = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_STOP
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .addCustomAction(
                    PlaybackStateCompat.CustomAction.Builder(
                        ACTION_STOP,
                        "Stop",
                        android.R.drawable.ic_menu_close_clear_cancel,
                    ).build(),
                )
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, if (playing) 1f else 0f)
                .build(),
        )
    }

    private fun applyRemotePlay() {
        playing = true
        SessionRemoteControls.resume?.invoke()
        publishPlayback(true)
        startInForeground()
    }

    private fun applyRemotePause() {
        playing = false
        SessionRemoteControls.pause?.invoke()
        publishPlayback(false)
        startInForeground()
    }

    private fun applyRemoteStop() {
        SessionRemoteControls.stop?.invoke() ?: stopSelf()
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }

    private fun screenIsInteractive(): Boolean =
        getSystemService(PowerManager::class.java)?.isInteractive != false

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
        private const val SESSION_TAG = "EmdrSession"
        private const val WAKE_TAG = "emdr:session"
    }
}
