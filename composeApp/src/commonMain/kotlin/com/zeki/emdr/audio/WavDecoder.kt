package com.zeki.emdr.audio

object WavDecoder {
    fun decodeMono16(bytes: ByteArray): FloatArray {
        require(bytes.size >= 44) { "WAV too small" }
        require(bytes.ascii(0, 4) == "RIFF" && bytes.ascii(8, 4) == "WAVE") { "Not a RIFF WAVE" }
        var offset = 12
        var channels = 1
        var bits = 16
        var dataOffset = -1
        var dataSize = 0
        while (offset + 8 <= bytes.size) {
            val id = bytes.ascii(offset, 4)
            val size = bytes.int32(offset + 4)
            val payload = offset + 8
            when (id) {
                "fmt " -> {
                    channels = bytes.int16(payload + 2)
                    bits = bytes.int16(payload + 14)
                }
                "data" -> {
                    dataOffset = payload
                    dataSize = size
                }
            }
            offset = payload + size + (size and 1)
        }
        require(dataOffset >= 0 && bits == 16) { "Expected 16-bit PCM WAV" }
        val frameBytes = 2 * channels
        val frames = dataSize / frameBytes
        val samples = FloatArray(frames)
        var i = 0
        var cursor = dataOffset
        while (i < frames) {
            var mixed = 0f
            repeat(channels) {
                mixed += bytes.int16(cursor).toFloat() / Short.MAX_VALUE
                cursor += 2
            }
            samples[i] = mixed / channels
            i += 1
        }
        return samples
    }

    private fun ByteArray.ascii(offset: Int, length: Int): String =
        decodeToString(offset, offset + length)

    private fun ByteArray.int16(offset: Int): Int {
        val lo = this[offset].toInt() and 0xFF
        val hi = this[offset + 1].toInt()
        return (hi shl 8) or lo
    }

    private fun ByteArray.int32(offset: Int): Int {
        val b0 = this[offset].toInt() and 0xFF
        val b1 = this[offset + 1].toInt() and 0xFF
        val b2 = this[offset + 2].toInt() and 0xFF
        val b3 = this[offset + 3].toInt()
        return (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
    }
}
