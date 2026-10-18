package com.zeki.emdr.session

import com.zeki.emdr.model.VisualPattern
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BilateralMotionTest {
    @Test
    fun frequencyIsHalfTheBeatRate() {
        assertEquals(0.5, BilateralMotion.frequencyHz(60), 1e-9)
        assertEquals(0.25, BilateralMotion.frequencyHz(30), 1e-9)
    }

    @Test
    fun sineAxisRestsAtTheEdges() {
        assertEquals(0f, BilateralMotion.signedAxis(0.0), 1e-5f)
        assertEquals(1f, BilateralMotion.signedAxis(PI / 2.0), 1e-5f)
        assertEquals(0f, BilateralMotion.signedAxis(PI), 1e-5f)
        assertEquals(-1f, BilateralMotion.signedAxis(3.0 * PI / 2.0), 1e-5f)
    }

    @Test
    fun panTracksHorizontalOnly() {
        val vertical = BilateralMotion.offset(PI / 2.0, VisualPattern.Vertical)
        assertEquals(0f, BilateralMotion.panFromHorizontal(vertical.x), 1e-5f)
        assertEquals(1f, vertical.y, 1e-5f)

        val horizontal = BilateralMotion.offset(PI / 2.0, VisualPattern.Horizontal)
        assertEquals(1f, BilateralMotion.panFromHorizontal(horizontal.x), 1e-5f)
    }

    @Test
    fun clockDoesNotJumpWhenBpmChanges() {
        val clock = BilateralClock()
        clock.advance(1.0, 60)
        val before = clock.phaseRadians
        clock.advance(0.0, 90)
        assertEquals(before, clock.phaseRadians, 1e-9)
        clock.advance(0.1, 90)
        assertTrue(clock.phaseRadians > before)
    }

    @Test
    fun infinityHasVerticalComponent() {
        val mid = BilateralMotion.offset(PI / 4.0, VisualPattern.Infinity)
        assertTrue(abs(mid.y) > 0.1f)
    }

    @Test
    fun firstEdgeIsRightAndDoesNotDoubleFire() {
        assertEquals(
            listOf(TrajectoryEdge.Right),
            BilateralMotion.edgesCrossed(0.0, PI / 2.0),
        )
        assertEquals(emptyList(), BilateralMotion.edgesCrossed(PI / 2.0, PI / 2.0 + 0.01))
        assertEquals(
            listOf(TrajectoryEdge.Left),
            BilateralMotion.edgesCrossed(PI / 2.0, 3.0 * PI / 2.0),
        )
    }

    @Test
    fun sixtyAndOneTwentyFpsReportTheSameEdges() {
        val bpm = 60
        val seconds = 2.0
        fun collect(fps: Int): List<TrajectoryEdge> {
            val clock = BilateralClock()
            val edges = ArrayList<TrajectoryEdge>()
            val dt = 1.0 / fps
            var t = 0.0
            while (t < seconds - 1e-12) {
                val from = clock.advance(dt, bpm)
                edges += BilateralMotion.edgesCrossed(from, clock.phaseRadians)
                t += dt
            }
            return edges
        }
        assertEquals(
            listOf(TrajectoryEdge.Right, TrajectoryEdge.Left),
            collect(60),
        )
        assertEquals(collect(60), collect(120))
        assertEquals(collect(60), collect(90))
    }

    @Test
    fun frameInversionSnapsToTravelFieldEdges() {
        val right = BilateralMotion.inversionsOnFrame(0.0, PI / 2.0, VisualPattern.Horizontal)
        assertEquals(1, right.size)
        assertEquals(TrajectoryEdge.Right, right.single().edge)
        assertEquals(1f, right.single().snappedOffset.x, 1e-5f)

        val left = BilateralMotion.inversionsOnFrame(PI / 2.0, 3.0 * PI / 2.0, VisualPattern.Horizontal)
        assertEquals(TrajectoryEdge.Left, left.single().edge)
        assertEquals(-1f, left.single().snappedOffset.x, 1e-5f)
    }

    @Test
    fun sixtyNinetyAndOneTwentyFpsShareTheSameInversions() {
        val bpm = 60
        val seconds = 2.0
        fun collect(fps: Int): List<TrajectoryEdge> {
            val clock = BilateralClock()
            val edges = ArrayList<TrajectoryEdge>()
            val dt = 1.0 / fps
            var t = 0.0
            while (t < seconds - 1e-12) {
                val from = clock.advance(dt, bpm)
                edges += BilateralMotion.inversionsOnFrame(
                    from,
                    clock.phaseRadians,
                    VisualPattern.Horizontal,
                ).map { it.edge }
                t += dt
            }
            return edges
        }
        assertEquals(collect(60), collect(90))
        assertEquals(collect(60), collect(120))
    }
}
