package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread

actual fun createBilateralAudioPlayer(): BilateralAudioPlayer = DesktopBilateralAudioPlayer()

private class DesktopBilateralAudioPlayer : BilateralAudioPlayer {
    private val mixer = EdgePlaybackMixer()
    @Volatile private var armed: Boolean = false
    @Volatile private var running: Boolean = false

    private val lock = Any()
    private var worker: Thread? = null
    private var line: SourceDataLine? = null

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
            val format = AudioFormat(AudioSynthesis.SAMPLE_RATE.toFloat(), 16, 2, true, false)
            val info = DataLine.Info(SourceDataLine::class.java, format)
            val created = AudioSystem.getLine(info) as SourceDataLine
            created.open(format, 128 * 4 * 4)
            created.start()
            line = created
            worker = thread(name = "emdr-audio", isDaemon = true) { stream(created) }
        }
    }

    private fun stream(output: SourceDataLine) {
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
            line?.stop()
            line?.flush()
            line?.close()
            line = null
        }
    }
}
