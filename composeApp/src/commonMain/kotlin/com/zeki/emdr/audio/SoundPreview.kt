package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import kotlinx.coroutines.delay

const val SOUND_PREVIEW_GAP_MS = 600L

/**
 * One left tick, a 600ms gap, one right tick. Never starts the session clock.
 * Disarms after the right one-shot so the engine does not keep firing.
 */
suspend fun playBilateralSoundPreview(
    player: BilateralAudioPlayer,
    sound: AudioSound,
) {
    player.setSound(sound)
    player.setArmed(true)
    try {
        player.triggerEdge(TrajectoryEdge.Left)
        delay(SOUND_PREVIEW_GAP_MS)
        player.triggerEdge(TrajectoryEdge.Right)
        delay(previewTailMs(sound))
    } finally {
        player.setArmed(false)
    }
}

internal fun previewTailMs(sound: AudioSound): Long {
    val frames = AudioSamples.oneShot(sound).size
    if (frames == 0) return 150L
    return (frames * 1000L) / AudioSynthesis.SAMPLE_RATE + 40L
}
