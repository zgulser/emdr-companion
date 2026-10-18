package com.zeki.emdr.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SupportMilestoneTest {
    @Test
    fun pendingOnlyAtExactLaunchMilestones() {
        assertNull(SupportMilestone.pending(4, false, false, false))
        assertEquals(SupportMilestone.Five, SupportMilestone.pending(5, false, false, false))
        assertNull(SupportMilestone.pending(5, true, false, false))
        assertNull(SupportMilestone.pending(6, false, false, false))
        assertEquals(SupportMilestone.Twenty, SupportMilestone.pending(20, true, false, false))
        assertNull(SupportMilestone.pending(20, true, true, false))
        assertEquals(SupportMilestone.Fifty, SupportMilestone.pending(50, true, true, false))
        assertNull(SupportMilestone.pending(50, true, true, true))
    }
}
