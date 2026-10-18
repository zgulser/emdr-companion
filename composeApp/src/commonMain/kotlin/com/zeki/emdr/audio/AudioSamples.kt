package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.resources.Res
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import org.jetbrains.compose.resources.ExperimentalResourceApi

object AudioSamples {
    var softSineTone: FloatArray = FloatArray(0)
        internal set

    var woodenMetronome: FloatArray = FloatArray(0)
        internal set

    @OptIn(ExperimentalResourceApi::class)
    suspend fun ensureLoaded() {
        if (softSineTone.isNotEmpty() && woodenMetronome.isNotEmpty()) return
        softSineTone = WavDecoder.decodeMono16(Res.readBytes("files/soft_sine_tone.wav"))
        woodenMetronome = WavDecoder.decodeMono16(Res.readBytes("files/wooden_metronome.wav"))
    }

    fun oneShot(sound: AudioSound): FloatArray =
        when (sound) {
            AudioSound.SoftTone -> sinePing()
            AudioSound.WoodenMetronome ->
                woodenMetronome.takeIf { it.isNotEmpty() } ?: sinePing()
        }

    private fun sinePing(): FloatArray {
        val n = (AudioSynthesis.SAMPLE_RATE * 0.11).toInt()
        val source = softSineTone
        val length = if (source.isEmpty()) n else min(source.size, n)
        return FloatArray(length) { index ->
            val env = exp(-index * 28.0 / AudioSynthesis.SAMPLE_RATE).toFloat()
            val raw = if (source.isEmpty()) {
                sin(2.0 * PI * 220.0 * index / AudioSynthesis.SAMPLE_RATE).toFloat() * 0.22f
            } else {
                source[index] * 1.35f
            }
            raw * env
        }
    }
}
