package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Single-producer / single-consumer mailbox. The UI thread only [offer]s;
 * the audio thread only [poll]s. CAS keeps Native and JVM aligned.
 */
@OptIn(ExperimentalAtomicApi::class)
class EdgeTriggerQueue {
    private val pending = AtomicReference<List<TrajectoryEdge>>(emptyList())

    fun offer(edge: TrajectoryEdge) {
        while (true) {
            val current = pending.load()
            if (pending.compareAndSet(current, current + edge)) return
        }
    }

    fun poll(): TrajectoryEdge? {
        while (true) {
            val current = pending.load()
            if (current.isEmpty()) return null
            if (pending.compareAndSet(current, current.drop(1))) return current.first()
        }
    }

    fun clear() {
        pending.store(emptyList())
    }
}

/**
 * Mixes discrete one-shots onto a warm audio thread. Silence is written
 * between edges so the device stays open without looping a tick file.
 */
class EdgePlaybackMixer {
    val queue = EdgeTriggerQueue()
    var sound: AudioSound = AudioSound.SoftTone

    private var playhead: Int = Int.MAX_VALUE
    private var activePan: Float = 0f
    private var activeSample: FloatArray = FloatArray(0)
    private var outputGain: Float = 1f
    private var fadePerSample: Float = 0f

    fun trigger(edge: TrajectoryEdge) {
        if (fadePerSample > 0f) return
        queue.offer(edge)
    }

    fun fadeOut(seconds: Float = 1.2f) {
        queue.clear()
        val samples = (AudioSynthesis.SAMPLE_RATE * seconds).coerceAtLeast(1f)
        fadePerSample = outputGain / samples
    }

    fun reset() {
        queue.clear()
        playhead = Int.MAX_VALUE
        activeSample = FloatArray(0)
        outputGain = 1f
        fadePerSample = 0f
    }

    fun render(frames: Int, left: FloatArray, right: FloatArray) {
        if (playhead >= activeSample.size && fadePerSample == 0f) {
            val edge = queue.poll()
            if (edge != null) {
                activeSample = AudioSamples.oneShot(sound)
                playhead = 0
                // TrajectoryEdge.Left = -1, Right = +1 — never mix to center.
                activePan = edge.pan
            }
        }
        playhead = AudioSynthesis.mixOneShot(
            sample = activeSample,
            playhead = playhead,
            pan = activePan,
            frames = frames,
            left = left,
            right = right,
        )
        if (fadePerSample > 0f || outputGain < 1f) {
            for (i in 0 until frames) {
                if (fadePerSample > 0f) {
                    outputGain = (outputGain - fadePerSample).coerceAtLeast(0f)
                }
                left[i] *= outputGain
                right[i] *= outputGain
            }
        }
    }
}
