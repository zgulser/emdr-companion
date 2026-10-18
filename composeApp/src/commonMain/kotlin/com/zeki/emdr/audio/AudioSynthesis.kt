package com.zeki.emdr.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AudioSynthesis {
    const val SAMPLE_RATE = 44_100

    fun equalPowerGains(pan: Float): Pair<Float, Float> {
        val clamped = pan.coerceIn(-1f, 1f)
        val angle = ((clamped + 1f) * (PI.toFloat() / 4f))
        return cos(angle) to sin(angle)
    }

    /**
     * Writes [frames] of a one-shot starting at [playhead]. Returns the new playhead.
     * Past the sample, the output is silence — never a looping tick file.
     */
    fun mixOneShot(
        sample: FloatArray,
        playhead: Int,
        pan: Float,
        frames: Int,
        left: FloatArray,
        right: FloatArray,
    ): Int {
        val (gainL, gainR) = equalPowerGains(pan)
        var head = playhead
        for (i in 0 until frames) {
            val value = if (head in sample.indices) sample[head++] else 0f
            left[i] = value * gainL
            right[i] = value * gainR
        }
        return head
    }

    fun mixToPcm16(left: FloatArray, right: FloatArray, frames: Int, dest: ByteArray) {
        var o = 0
        for (i in 0 until frames) {
            writeSample(dest, o, left[i]); o += 2
            writeSample(dest, o, right[i]); o += 2
        }
    }

    fun monoToStereoPcm16(mono: FloatArray): ByteArray {
        val pcm = ByteArray(mono.size * 4)
        var o = 0
        for (sample in mono) {
            writeSample(pcm, o, sample); o += 2
            writeSample(pcm, o, sample); o += 2
        }
        return pcm
    }

    private fun writeSample(dest: ByteArray, offset: Int, sample: Float) {
        val clamped = sample.coerceIn(-1f, 1f)
        val intSample = (clamped * Short.MAX_VALUE).toInt()
        dest[offset] = (intSample and 0xFF).toByte()
        dest[offset + 1] = (intSample shr 8).toByte()
    }

    fun wrapWav(pcm16Stereo: ByteArray, sampleRate: Int = SAMPLE_RATE): ByteArray {
        val header = ByteArray(44)
        val dataSize = pcm16Stereo.size
        val fileSize = 36 + dataSize
        header.writeAscii(0, "RIFF")
        header.writeInt32(4, fileSize)
        header.writeAscii(8, "WAVE")
        header.writeAscii(12, "fmt ")
        header.writeInt32(16, 16)
        header.writeInt16(20, 1)
        header.writeInt16(22, 2)
        header.writeInt32(24, sampleRate)
        header.writeInt32(28, sampleRate * 4)
        header.writeInt16(32, 4)
        header.writeInt16(34, 16)
        header.writeAscii(36, "data")
        header.writeInt32(40, dataSize)
        return header + pcm16Stereo
    }

    private fun ByteArray.writeAscii(offset: Int, value: String) {
        value.forEachIndexed { index, char -> this[offset + index] = char.code.toByte() }
    }

    private fun ByteArray.writeInt16(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = (value shr 8 and 0xFF).toByte()
    }

    private fun ByteArray.writeInt32(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = (value shr 8 and 0xFF).toByte()
        this[offset + 2] = (value shr 16 and 0xFF).toByte()
        this[offset + 3] = (value shr 24 and 0xFF).toByte()
    }
}
