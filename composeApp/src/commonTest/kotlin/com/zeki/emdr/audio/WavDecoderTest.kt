package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WavDecoderTest {
    @Test
    fun decodesWrappedStereoAsMono() {
        val left = FloatArray(8) { 0.5f }
        val right = FloatArray(8) { 0.25f }
        val pcm = ByteArray(32)
        AudioSynthesis.mixToPcm16(left, right, 8, pcm)
        val wav = AudioSynthesis.wrapWav(pcm)
        val decoded = WavDecoder.decodeMono16(wav)
        assertEquals(8, decoded.size)
        decoded.forEach { sample ->
            assertEquals(0.375f, sample, 0.01f)
        }
    }

    @Test
    fun woodenMetronomeStorageRoundTrips() {
        assertEquals(AudioSound.WoodenMetronome, AudioSound.fromStorage("wooden_metronome"))
        assertEquals(AudioSound.WOODEN_METRONOME, AudioSound.fromStorage("wooden_metronome"))
        assertEquals(AudioSound.SoftTone, AudioSound.fromStorage("click"))
    }

    @Test
    fun woodenTickHasASharpAttack() {
        val left = FloatArray(256)
        val right = FloatArray(256)
        AudioSamples.woodenMetronome = FloatArray(64) { index ->
            if (index == 0) 0.9f else 0.9f * (1f - index / 64f)
        }
        val mixer = EdgePlaybackMixer()
        mixer.sound = AudioSound.WoodenMetronome
        mixer.trigger(TrajectoryEdge.Right)
        mixer.render(256, left, right)
        assertTrue(right.first() > 0.4f)
        assertTrue(left.first() < 0.02f)
        assertTrue(right[64] == 0f)
    }
}
