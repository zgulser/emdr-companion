package com.zeki.emdr.session

import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.SetLength

enum class SessionPhase {
    Ready,
    Running,
    Paused,
    Resting,
    BetweenSets,
    Complete,
}

data class SessionRuntimeState(
    val phase: SessionPhase,
    val passesInSet: Int = 0,
    val setsCompleted: Int = 0,
) {
    val isReady: Boolean get() = phase == SessionPhase.Ready
    val isRunning: Boolean get() = phase == SessionPhase.Running
    val isPaused: Boolean get() = phase == SessionPhase.Paused
    val isResting: Boolean get() = phase == SessionPhase.Resting
    val isBetweenSets: Boolean get() = phase == SessionPhase.BetweenSets
    val isComplete: Boolean get() = phase == SessionPhase.Complete
    val holdChrome: Boolean get() = when (phase) {
        SessionPhase.Running, SessionPhase.Paused -> false
        else -> true
    }
    val motionActive: Boolean get() = phase == SessionPhase.Running
    val audioArmed: Boolean get() = phase == SessionPhase.Running
    val showBreathRing: Boolean get() = phase == SessionPhase.Paused || phase == SessionPhase.Resting
}

/**
 * Clinical session phases. One pass is one edge arrival (one saccade).
 * Sets stop on the last pass, rest for [REST_SECONDS], then wait for the next set.
 */
class SessionRuntime(startInReady: Boolean = true) {
    var state: SessionRuntimeState =
        if (startInReady) SessionRuntimeState(SessionPhase.Ready)
        else SessionRuntimeState(SessionPhase.Running)
        private set

    fun beginSet() {
        if (state.phase == SessionPhase.Complete) return
        when (state.phase) {
            SessionPhase.Ready, SessionPhase.BetweenSets ->
                state = state.copy(phase = SessionPhase.Running, passesInSet = 0)
            SessionPhase.Paused ->
                state = state.copy(phase = SessionPhase.Running)
            else -> Unit
        }
    }

    fun togglePause() {
        when (state.phase) {
            SessionPhase.Ready, SessionPhase.BetweenSets -> beginSet()
            SessionPhase.Running -> state = state.copy(phase = SessionPhase.Paused)
            SessionPhase.Paused -> beginSet()
            SessionPhase.Resting, SessionPhase.Complete -> Unit
        }
    }

    /**
     * Records [count] newly crossed edges. Returns true when this call
     * ended the set and the UI should fade audio / settle motion.
     */
    fun notePasses(count: Int, type: SessionType, length: SetLength): Boolean {
        if (state.phase != SessionPhase.Running || type != SessionType.SETS || count <= 0) {
            return false
        }
        val next = state.passesInSet + count
        return if (next >= length.passes) {
            state = state.copy(
                phase = SessionPhase.Resting,
                passesInSet = length.passes,
                setsCompleted = state.setsCompleted + 1,
            )
            true
        } else {
            state = state.copy(passesInSet = next)
            false
        }
    }

    fun finishRest() {
        if (state.phase == SessionPhase.Resting) {
            state = state.copy(phase = SessionPhase.BetweenSets)
        }
    }

    fun pauseIfRunning() {
        if (state.phase == SessionPhase.Running) {
            state = state.copy(phase = SessionPhase.Paused)
        }
    }

    fun markComplete() {
        state = state.copy(phase = SessionPhase.Complete)
    }

    fun returnToReady() {
        state = SessionRuntimeState(SessionPhase.Ready)
    }

    companion object {
        const val REST_SECONDS = 10
        const val REST_MS = SomaticBreath.CYCLE_MS.toLong()
        const val SETTLE_MS = 1_200
    }
}
