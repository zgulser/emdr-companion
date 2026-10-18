package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create

actual fun createBilateralAudioPlayer(): BilateralAudioPlayer = IosBilateralAudioPlayer()

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
private class IosBilateralAudioPlayer : BilateralAudioPlayer {
    private var leftPlayer: AVAudioPlayer? = null
    private var rightPlayer: AVAudioPlayer? = null
    private var sound: AudioSound = AudioSound.SoftTone
    private var armed: Boolean = false

    override fun setSound(sound: AudioSound) {
        this.sound = sound
        rebuild()
    }

    override fun setArmed(armed: Boolean) {
        this.armed = armed
        if (armed) {
            ensureSession()
            if (leftPlayer == null) rebuild()
            leftPlayer?.setVolume(1f)
            rightPlayer?.setVolume(1f)
        } else {
            leftPlayer?.stop()
            rightPlayer?.stop()
        }
    }

    override fun triggerEdge(edge: TrajectoryEdge) {
        if (!armed) return
        val player = if (edge == TrajectoryEdge.Left) leftPlayer else rightPlayer
        player?.setCurrentTime(0.0)
        player?.play()
    }

    override fun fadeOut() {
        armed = false
        leftPlayer?.setVolume(0f)
        rightPlayer?.setVolume(0f)
    }

    override fun release() {
        armed = false
        leftPlayer?.stop()
        rightPlayer?.stop()
        leftPlayer = null
        rightPlayer = null
    }

    private fun rebuild() {
        leftPlayer?.stop()
        rightPlayer?.stop()
        val wav = AudioSynthesis.wrapWav(
            AudioSynthesis.monoToStereoPcm16(AudioSamples.oneShot(sound)),
        )
        val data = wav.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = wav.size.toULong())
        }
        leftPlayer = oneShotPlayer(data, pan = -1f)
        rightPlayer = oneShotPlayer(data, pan = 1f)
    }

    private fun oneShotPlayer(data: NSData, pan: Float): AVAudioPlayer {
        val created = AVAudioPlayer(data = data, error = null)
        created.setNumberOfLoops(0)
        created.setPan(pan)
        created.prepareToPlay()
        return created
    }

    private fun ensureSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }
}
