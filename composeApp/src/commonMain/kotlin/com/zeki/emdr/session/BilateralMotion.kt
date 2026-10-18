package com.zeki.emdr.session

import com.zeki.emdr.model.VisualPattern
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

enum class TrajectoryEdge(val pan: Float, val axis: Float) {
    /** Screen-space left: rendered x = 0 of the travel field. */
    Left(pan = -1f, axis = -1f),
    /** Screen-space right: rendered x = max of the travel field. */
    Right(pan = 1f, axis = 1f),
}

data class FrameInversion(
    val edge: TrajectoryEdge,
    /** Exact travel-field pose to draw on the same frame as the tick. */
    val snappedOffset: BilateralOffset,
)

data class BilateralOffset(
    /** Normalized horizontal position in `-1.0` (left) … `+1.0` (right). */
    val x: Float,
    /** Normalized vertical position in `-1.0` (top) … `+1.0` (bottom). */
    val y: Float,
)

/**
 * Sine-wave bilateral kinematics. A full left–right–left cycle has two edge arrivals,
 * so frequency is `bpm / 120` Hz. `sin` supplies the required accel/decel at each end.
 */
object BilateralMotion {
    fun frequencyHz(bpm: Int): Double = bpm / 120.0

    fun phaseStepRadians(deltaSeconds: Double, bpm: Int): Double =
        2.0 * PI * frequencyHz(bpm) * deltaSeconds

    fun signedAxis(phaseRadians: Double): Float = sin(phaseRadians).toFloat()

    fun offset(phaseRadians: Double, pattern: VisualPattern): BilateralOffset {
        val axis = signedAxis(phaseRadians)
        return when (pattern) {
            VisualPattern.Horizontal -> BilateralOffset(x = axis, y = 0f)
            VisualPattern.Vertical -> BilateralOffset(x = 0f, y = axis)
            VisualPattern.Diagonal -> BilateralOffset(x = axis, y = axis)
            VisualPattern.Infinity -> BilateralOffset(
                x = axis,
                y = sin(2.0 * phaseRadians).toFloat() * 0.45f,
            )
        }
    }

    /** Stereo pan follows the orb's horizontal coordinate only. */
    fun panFromHorizontal(x: Float): Float = x.coerceIn(-1f, 1f)

    /**
     * Inflection points of `sin(phase)`: `π/2 + nπ`.
     * Even `n` is the right edge (`+1`), odd `n` is the left edge (`-1`).
     * Uses a half-open interval `(from, to]` so a 60 Hz and 120 Hz clock
     * report the same edges with no double-fire or drift.
     */
    fun edgesCrossed(fromPhaseExclusive: Double, toPhaseInclusive: Double): List<TrajectoryEdge> {
        if (toPhaseInclusive <= fromPhaseExclusive) return emptyList()
        val startN = floor((fromPhaseExclusive - HALF_PI) / PI) + 1.0
        val endN = floor((toPhaseInclusive - HALF_PI) / PI)
        if (endN < startN) return emptyList()
        val edges = ArrayList<TrajectoryEdge>((endN - startN).toInt() + 1)
        var n = startN
        while (n <= endN + 1e-9) {
            val index = n.toLong()
            edges += if (index and 1L == 0L) TrajectoryEdge.Right else TrajectoryEdge.Left
            n += 1.0
        }
        return edges
    }

    /**
     * Frame-bound inversions for the interval just rendered. The snapped
     * offset puts the orb on x = 0 or x = max in the same frame as the tick,
     * so 60 / 90 / 120 Hz clocks stay visually locked.
     */
    fun inversionsOnFrame(
        fromPhaseExclusive: Double,
        toPhaseInclusive: Double,
        pattern: VisualPattern,
    ): List<FrameInversion> =
        edgesCrossed(fromPhaseExclusive, toPhaseInclusive).map { edge ->
            FrameInversion(
                edge = edge,
                snappedOffset = offsetAtAxis(edge.axis, toPhaseInclusive, pattern),
            )
        }

    fun offsetAtAxis(axis: Float, phaseRadians: Double, pattern: VisualPattern): BilateralOffset {
        val clamped = axis.coerceIn(-1f, 1f)
        return when (pattern) {
            VisualPattern.Horizontal -> BilateralOffset(x = clamped, y = 0f)
            VisualPattern.Vertical -> BilateralOffset(x = 0f, y = clamped)
            VisualPattern.Diagonal -> BilateralOffset(x = clamped, y = clamped)
            VisualPattern.Infinity -> BilateralOffset(
                x = clamped,
                y = sin(2.0 * phaseRadians).toFloat() * 0.45f,
            )
        }
    }

    private val HALF_PI = PI / 2.0
}

class BilateralClock {
    var phaseRadians: Double = 0.0
        private set

    fun advance(deltaSeconds: Double, bpm: Int): Double {
        val from = phaseRadians
        if (deltaSeconds <= 0.0) return from
        phaseRadians += BilateralMotion.phaseStepRadians(deltaSeconds, bpm)
        return from
    }

    fun reset() {
        phaseRadians = 0.0
    }
}
