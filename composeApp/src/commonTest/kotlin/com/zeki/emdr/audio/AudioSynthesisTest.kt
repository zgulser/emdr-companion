package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioSynthesisTest {
    @Test
    fun equalPowerIsHardLeftAndRightAtTheEdges() {
        val left = AudioSynthesis.equalPowerGains(-1f)
        assertEquals(1f, left.first, 1e-5f)
        assertEquals(0f, left.second, 1e-5f)

        val right = AudioSynthesis.equalPowerGains(1f)
        assertEquals(0f, right.first, 1e-5f)
        assertEquals(1f, right.second, 1e-5f)
    }

    @Test
    fun centerPanSplitsEnergy() {
        val (l, r) = AudioSynthesis.equalPowerGains(0f)
        assertTrue(l in 0.70f..0.72f)
        assertEquals(l, r, 1e-5f)
    }

    @Test
    fun oneShotDoesNotLoopAfterTheSampleEnds() {
        val sample = floatArrayOf(0.8f, 0.4f)
        val left = FloatArray(4)
        val right = FloatArray(4)
        val playhead = AudioSynthesis.mixOneShot(sample, 0, -1f, 4, left, right)
        assertEquals(2, playhead)
        assertEquals(0.8f, left[0], 1e-5f)
        assertEquals(0.4f, left[1], 1e-5f)
        assertEquals(0f, left[2], 1e-5f)
        assertEquals(0f, left[3], 1e-5f)
        assertEquals(0f, right[0], 1e-5f)
    }

    @Test
    fun mixerFiresHardPannedTicksOnlyOnEdges() {
        AudioSamples.woodenMetronome = floatArrayOf(1f, 0.5f)
        val mixer = EdgePlaybackMixer()
        mixer.sound = AudioSound.WoodenMetronome
        val left = FloatArray(4)
        val right = FloatArray(4)
        mixer.render(4, left, right)
        assertTrue(left.all { it == 0f })
        assertTrue(right.all { it == 0f })

        mixer.trigger(TrajectoryEdge.Left)
        mixer.render(4, left, right)
        assertEquals(-1f, TrajectoryEdge.Left.pan)
        assertEquals(1f, left[0], 1e-5f)
        assertEquals(0f, right[0], 1e-5f)
        assertEquals(0f, left[2], 1e-5f)

        mixer.reset()
        mixer.trigger(TrajectoryEdge.Right)
        mixer.render(4, left, right)
        assertEquals(1f, TrajectoryEdge.Right.pan)
        assertEquals(0f, left[0], 1e-5f)
        assertEquals(1f, right[0], 1e-5f)
    }

    @Test
    fun hardPannedPcmKeepsSilentOppositeChannel() {
        val left = AudioSynthesis.monoToHardPannedPcm16(floatArrayOf(1f), pan = -1f)
        // 1f * Short.MAX_VALUE = 0x7FFF little-endian
        assertEquals(0xFF.toByte(), left[0])
        assertEquals(0x7F.toByte(), left[1])
        assertEquals(0, left[2].toInt())
        assertEquals(0, left[3].toInt())

        val right = AudioSynthesis.monoToHardPannedPcm16(floatArrayOf(1f), pan = 1f)
        assertEquals(0, right[0].toInt())
        assertEquals(0, right[1].toInt())
        assertEquals(0xFF.toByte(), right[2])
        assertEquals(0x7F.toByte(), right[3])
    }

    @Test
    fun mixerFadeOutSilencesAndRejectsNewEdges() {
        AudioSamples.woodenMetronome = floatArrayOf(1f, 1f)
        val mixer = EdgePlaybackMixer()
        mixer.sound = AudioSound.WoodenMetronome
        mixer.trigger(TrajectoryEdge.Left)
        val left = FloatArray(8)
        val right = FloatArray(8)
        mixer.fadeOut(seconds = 1f / AudioSynthesis.SAMPLE_RATE)
        mixer.render(8, left, right)
        assertTrue(left.all { it == 0f })
        mixer.trigger(TrajectoryEdge.Right)
        mixer.render(8, left, right)
        assertTrue(right.all { it == 0f })
    }
}
