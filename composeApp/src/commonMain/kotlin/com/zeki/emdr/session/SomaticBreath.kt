package com.zeki.emdr.session

import kotlin.math.PI
import kotlin.math.cos

/**
 * 10-second vagal breath: 4s inhale (expand) and 6s exhale (contract).
 * Cosine ease keeps the turns quiet — no linear snaps at the peaks.
 */
object SomaticBreath {
    const val INHALE_MS = 4_000
    const val EXHALE_MS = 6_000
    const val CYCLE_MS = INHALE_MS + EXHALE_MS
    const val INHALE_FRACTION = INHALE_MS.toFloat() / CYCLE_MS
    const val MIN_SCALE = 0.78f
    const val MAX_SCALE = 1.18f
    const val PROMPT = "Breathe in... Breathe out..."

    fun isInhale(cycleProgress: Float): Boolean {
        val progress = wrap(cycleProgress)
        return progress < INHALE_FRACTION
    }

    fun scale(cycleProgress: Float): Float {
        val progress = wrap(cycleProgress)
        val eased = if (progress < INHALE_FRACTION) {
            cosineEase(progress / INHALE_FRACTION)
        } else {
            1f - cosineEase((progress - INHALE_FRACTION) / (1f - INHALE_FRACTION))
        }
        return MIN_SCALE + (MAX_SCALE - MIN_SCALE) * eased
    }

    fun prompt(cycleProgress: Float): String =
        if (isInhale(cycleProgress)) "Breathe in..." else "Breathe out..."

    private fun wrap(progress: Float): Float {
        if (progress.isNaN()) return 0f
        val wrapped = progress % 1f
        return if (wrapped < 0f) wrapped + 1f else wrapped
    }

    private fun cosineEase(t: Float): Float {
        val clamped = t.coerceIn(0f, 1f)
        return ((1.0 - cos(clamped * PI)) * 0.5).toFloat()
    }
}
