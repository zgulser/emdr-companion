package com.zeki.emdr.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SomaticBreathTest {
    @Test
    fun inhaleOccupiesTheFirstFourSeconds() {
        assertTrue(SomaticBreath.isInhale(0f))
        assertTrue(SomaticBreath.isInhale(0.39f))
        assertFalse(SomaticBreath.isInhale(0.40f))
        assertFalse(SomaticBreath.isInhale(0.99f))
    }

    @Test
    fun vagalCurveExpandsThenContractsWithoutSnapping() {
        assertEquals(SomaticBreath.MIN_SCALE, SomaticBreath.scale(0f), 1e-5f)
        assertEquals(SomaticBreath.MAX_SCALE, SomaticBreath.scale(0.4f), 1e-5f)
        assertEquals(SomaticBreath.MIN_SCALE, SomaticBreath.scale(1f), 1e-5f)
        val rising = SomaticBreath.scale(0.2f)
        assertTrue(rising > SomaticBreath.MIN_SCALE && rising < SomaticBreath.MAX_SCALE)
        val falling = SomaticBreath.scale(0.7f)
        assertTrue(falling > SomaticBreath.MIN_SCALE && falling < SomaticBreath.MAX_SCALE)
    }
}
