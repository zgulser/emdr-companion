package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class SoundPreviewTest {
    @Test
    fun playsHardLeftThenHardRightThenDisarms() = runTest {
        val player = RecordingBilateralAudioPlayer()
        playBilateralSoundPreview(player, AudioSound.WoodenMetronome)
        assertEquals(-1f, TrajectoryEdge.Left.pan)
        assertEquals(1f, TrajectoryEdge.Right.pan)
        assertEquals(
            listOf(
                "sound:wooden_metronome",
                "armed:true",
                "edge:Left",
                "edge:Right",
                "armed:false",
            ),
            player.events,
        )
    }

    @Test
    fun disarmsIfPreviewIsCancelledAfterTheLeftTick() = runTest {
        val player = RecordingBilateralAudioPlayer()
        val job = launch {
            playBilateralSoundPreview(player, AudioSound.SoftTone)
        }
        testScheduler.runCurrent()
        assertEquals(
            listOf("sound:soft_tone", "armed:true", "edge:Left"),
            player.events,
        )
        job.cancel()
        job.join()
        assertEquals("armed:false", player.events.last())
        assertEquals(1, player.events.count { it.startsWith("edge:") })
    }
}

private class RecordingBilateralAudioPlayer : BilateralAudioPlayer {
    val events = mutableListOf<String>()

    override fun setSound(sound: AudioSound) {
        events += "sound:${sound.storageValue}"
    }

    override fun setArmed(armed: Boolean) {
        events += "armed:$armed"
    }

    override fun triggerEdge(edge: TrajectoryEdge) {
        events += "edge:${edge.name}"
    }

    override fun fadeOut() {
        events += "fadeOut"
    }

    override fun release() {
        events += "release"
    }
}
