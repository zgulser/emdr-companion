package com.zeki.emdr.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import kotlin.concurrent.thread

actual fun createBilateralAudioPlayer(): BilateralAudioPlayer = AndroidBilateralAudioPlayer()

private class AndroidBilateralAudioPlayer : BilateralAudioPlayer {
    private val mixer = EdgePlaybackMixer()
    @Volatile private var armed: Boolean = false
    @Volatile private var running: Boolean = false

    private val lock = Any()
    private var worker: Thread? = null
    private var track: AudioTrack? = null

    override fun setSound(sound: AudioSound) {
        mixer.sound = sound
    }

    override fun setArmed(armed: Boolean) {
        val alreadyArmed = this.armed
        this.armed = armed
        if (armed && !alreadyArmed) {
            mixer.reset()
            startEngine()
        }
    }

    override fun triggerEdge(edge: TrajectoryEdge) {
        if (armed) mixer.trigger(edge)
    }

    override fun fadeOut() {
        armed = false
        mixer.fadeOut()
    }

    override fun release() {
        stopEngine()
    }

    private fun startEngine() {
        synchronized(lock) {
            if (worker != null) return
            running = true
            mixer.reset()
            val minBuffer = AudioTrack.getMinBufferSize(
                AudioSynthesis.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(128 * 4)
            val created = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(AudioSynthesis.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBuffer)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .apply {
                    if (Build.VERSION.SDK_INT >= 29) {
                        setOffloadedPlayback(false)
                    }
                }
                .build()
            track = created
            created.play()
            worker = thread(name = "emdr-audio", isDaemon = true) { stream(created) }
        }
    }

    private fun stream(output: AudioTrack) {
        val frames = 128
        val left = FloatArray(frames)
        val right = FloatArray(frames)
        val pcm = ByteArray(frames * 4)
        while (running) {
            mixer.render(frames, left, right)
            AudioSynthesis.mixToPcm16(left, right, frames, pcm)
            output.write(pcm, 0, pcm.size)
        }
    }

    private fun stopEngine() {
        synchronized(lock) {
            armed = false
            running = false
            mixer.reset()
            worker?.join(250)
            worker = null
            track?.pause()
            track?.flush()
            track?.release()
            track = null
        }
    }
}
