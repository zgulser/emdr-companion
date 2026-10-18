package com.zeki.emdr.session

import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.SetLength
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionRuntimeTest {
    @Test
    fun twentyFourPassesEndTheSetAndEnterRest() {
        val runtime = SessionRuntime()
        runtime.beginSet()
        assertFalse(runtime.notePasses(23, SessionType.SETS, SetLength.PASSES_24))
        assertEquals(23, runtime.state.passesInSet)
        assertTrue(runtime.notePasses(1, SessionType.SETS, SetLength.PASSES_24))
        assertEquals(SessionPhase.Resting, runtime.state.phase)
        assertTrue(runtime.state.showBreathRing)
        assertEquals(24, runtime.state.passesInSet)
        assertEquals(1, runtime.state.setsCompleted)
    }

    @Test
    fun continuousIgnoresPassCounts() {
        val runtime = SessionRuntime(startInReady = false)
        assertFalse(runtime.notePasses(48, SessionType.CONTINUOUS, SetLength.PASSES_24))
        assertEquals(SessionPhase.Running, runtime.state.phase)
        assertEquals(0, runtime.state.passesInSet)
    }

    @Test
    fun restThenNextSetClearsPasses() {
        val runtime = SessionRuntime()
        runtime.beginSet()
        runtime.notePasses(24, SessionType.SETS, SetLength.PASSES_24)
        runtime.finishRest()
        assertEquals(SessionPhase.BetweenSets, runtime.state.phase)
        runtime.beginSet()
        assertEquals(SessionPhase.Running, runtime.state.phase)
        assertEquals(0, runtime.state.passesInSet)
        assertEquals(1, runtime.state.setsCompleted)
    }

    @Test
    fun pauseDoesNotResetPasses() {
        val runtime = SessionRuntime()
        runtime.beginSet()
        runtime.notePasses(10, SessionType.SETS, SetLength.PASSES_36)
        runtime.togglePause()
        assertEquals(SessionPhase.Paused, runtime.state.phase)
        assertTrue(runtime.state.showBreathRing)
        assertFalse(runtime.state.holdChrome)
        runtime.togglePause()
        assertEquals(SessionPhase.Running, runtime.state.phase)
        assertEquals(10, runtime.state.passesInSet)
    }

    @Test
    fun pauseIfRunningLeavesReadyAndRestAlone() {
        val runtime = SessionRuntime()
        runtime.pauseIfRunning()
        assertEquals(SessionPhase.Ready, runtime.state.phase)
        runtime.beginSet()
        runtime.pauseIfRunning()
        assertEquals(SessionPhase.Paused, runtime.state.phase)
        assertFalse(runtime.state.holdChrome)
    }

    @Test
    fun readyAndRestHoldChromeWhileRunningDoesNot() {
        val runtime = SessionRuntime()
        assertTrue(runtime.state.holdChrome)
        runtime.beginSet()
        assertFalse(runtime.state.holdChrome)
        runtime.notePasses(24, SessionType.SETS, SetLength.PASSES_24)
        assertTrue(runtime.state.holdChrome)
    }
}
