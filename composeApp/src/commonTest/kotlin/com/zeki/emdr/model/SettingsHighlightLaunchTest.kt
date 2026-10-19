package com.zeki.emdr.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsHighlightLaunchTest {
    @Test
    fun onlyExactLaunchMilestones() {
        assertFalse(isSettingsHighlightLaunch(0))
        assertTrue(isSettingsHighlightLaunch(1))
        assertFalse(isSettingsHighlightLaunch(2))
        assertFalse(isSettingsHighlightLaunch(5))
        assertFalse(isSettingsHighlightLaunch(49))
        assertTrue(isSettingsHighlightLaunch(50))
        assertFalse(isSettingsHighlightLaunch(51))
        assertTrue(isSettingsHighlightLaunch(100))
        assertTrue(isSettingsHighlightLaunch(250))
        assertFalse(isSettingsHighlightLaunch(251))
    }
}
