package com.zeki.emdr.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeki.emdr.audio.AudioSamples
import com.zeki.emdr.audio.createBilateralAudioPlayer
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.haptics.createEdgeHaptics
import com.zeki.emdr.model.SessionSettings
import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.SupportMilestone
import com.zeki.emdr.session.BackgroundSessionController
import com.zeki.emdr.session.BilateralClock
import com.zeki.emdr.session.BilateralMotion
import com.zeki.emdr.session.BilateralOffset
import com.zeki.emdr.session.SessionRemoteControls
import com.zeki.emdr.session.SessionRuntime
import com.zeki.emdr.session.SessionRuntimeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

class ActiveSessionViewModel(
    private val repository: SessionSettingsRepository,
    startInReady: Boolean,
) : ViewModel() {
    val settings: StateFlow<SessionSettings> = repository.state

    private val runtime = SessionRuntime(startInReady)
    private val _runtimeState = MutableStateFlow(runtime.state)
    val runtimeState: StateFlow<SessionRuntimeState> = _runtimeState.asStateFlow()

    private val _resetEpoch = MutableStateFlow(0)
    val resetEpoch: StateFlow<Int> = _resetEpoch.asStateFlow()

    private val _offset = MutableStateFlow(BilateralOffset(0f, 0f))
    val offset: StateFlow<BilateralOffset> = _offset.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(settings.value.duration.seconds ?: 0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _elapsedActiveSeconds = MutableStateFlow(0f)
    val elapsedActiveSeconds: StateFlow<Float> = _elapsedActiveSeconds.asStateFlow()

    private val _milestoneSheet = MutableStateFlow<SupportMilestone?>(null)
    val milestoneSheet: StateFlow<SupportMilestone?> = _milestoneSheet.asStateFlow()

    private val audioPlayer = createBilateralAudioPlayer()
    private val haptics = createEdgeHaptics()
    private val clock = BilateralClock()
    private var restJob: Job? = null
    private var sessionWasActive = !startInReady

    init {
        SessionRemoteControls.pause = { pauseIfRunning() }
        SessionRemoteControls.resume = { beginSet() }
        SessionRemoteControls.stop = { returnToReady() }
        viewModelScope.launch {
            AudioSamples.ensureLoaded()
            audioPlayer.setSound(settings.value.audioSound)
        }
        viewModelScope.launch {
            settings.collect { next ->
                audioPlayer.setSound(next.audioSound)
                syncBackgroundService()
            }
        }
        viewModelScope.launch { tickLoop() }
        applyAudioForPhase()
        syncBackgroundService()
    }

    override fun onCleared() {
        restJob?.cancel()
        if (SessionRemoteControls.pause != null) {
            SessionRemoteControls.pause = null
            SessionRemoteControls.resume = null
            SessionRemoteControls.stop = null
        }
        BackgroundSessionController.stop()
        audioPlayer.release()
        haptics.release()
        super.onCleared()
    }

    fun togglePause() {
        runtime.togglePause()
        publish()
    }

    fun beginSet() {
        val restartClock = runtime.state.isReady || runtime.state.isBetweenSets
        runtime.beginSet()
        sessionWasActive = true
        if (restartClock) resetMotion()
        publish()
    }

    fun finishRest() {
        runtime.finishRest()
        publish()
    }

    fun pauseIfRunning() {
        runtime.pauseIfRunning()
        publish()
    }

    fun returnToReady() {
        val shouldOffer = sessionWasActive || runtime.state.isComplete
        runtime.returnToReady()
        resetMotion()
        _resetEpoch.value += 1
        publish()
        sessionWasActive = false
        if (shouldOffer) onSessionFinished()
    }

    fun setBpm(bpm: Int) = repository.setBpm(bpm)

    fun markComplete() {
        runtime.markComplete()
        publish()
        if (sessionWasActive) {
            sessionWasActive = false
            onSessionFinished()
        }
    }

    fun dismissMilestoneSheet() {
        _milestoneSheet.value = null
    }

    private fun onSessionFinished() {
        if (_milestoneSheet.value != null) return
        _milestoneSheet.value = repository.consumePendingMilestone()
    }

    private fun publish() {
        _runtimeState.value = runtime.state
        applyAudioForPhase()
        syncBackgroundService()
        scheduleRestIfNeeded()
    }

    private fun applyAudioForPhase() {
        val current = settings.value
        when {
            !current.mode.usesAudio -> audioPlayer.setArmed(false)
            runtime.state.audioArmed -> audioPlayer.setArmed(true)
            runtime.state.showBreathRing -> audioPlayer.fadeOut()
            else -> audioPlayer.setArmed(false)
        }
    }

    private fun syncBackgroundService() {
        val current = settings.value
        val state = runtime.state
        val keep = current.allowBackgroundPlayback && (state.isRunning || state.isPaused)
        if (keep) {
            BackgroundSessionController.start(playing = state.isRunning)
        } else {
            BackgroundSessionController.stop()
        }
    }

    private fun scheduleRestIfNeeded() {
        if (runtime.state.isResting) {
            if (restJob?.isActive != true) {
                restJob = viewModelScope.launch {
                    delay(SessionRuntime.REST_MS)
                    if (runtime.state.isResting) finishRest()
                }
            }
        } else {
            restJob?.cancel()
            restJob = null
        }
    }

    private fun resetMotion() {
        clock.reset()
        _offset.value = BilateralOffset(0f, 0f)
        _elapsedActiveSeconds.value = 0f
        _remainingSeconds.value = settings.value.duration.seconds ?: 0
    }

    private suspend fun tickLoop() {
        var lastMark = TimeSource.Monotonic.markNow()
        while (viewModelScope.isActive) {
            delay(8)
            val state = runtime.state
            if (!state.motionActive) {
                lastMark = TimeSource.Monotonic.markNow()
                continue
            }
            val now = TimeSource.Monotonic.markNow()
            val dt = (now - lastMark).toDouble(DurationUnit.SECONDS)
            lastMark = now
            if (dt <= 0.0 || dt >= 0.25) continue
            val current = settings.value
            val fromPhase = clock.advance(dt, current.bpm)
            val inversions = BilateralMotion.inversionsOnFrame(
                fromPhaseExclusive = fromPhase,
                toPhaseInclusive = clock.phaseRadians,
                pattern = current.pattern,
            )
            _offset.value = if (inversions.isNotEmpty()) {
                inversions.last().snappedOffset
            } else {
                BilateralMotion.offset(clock.phaseRadians, current.pattern)
            }
            _elapsedActiveSeconds.value += dt.toFloat()
            inversions.forEach { inversion ->
                if (current.mode.usesAudio) {
                    audioPlayer.triggerEdge(inversion.edge)
                }
                if (current.hapticsEnabled) {
                    haptics.lightImpact()
                }
            }
            if (current.sessionType == SessionType.SETS) {
                if (inversions.isNotEmpty()) {
                    runtime.notePasses(inversions.size, current.sessionType, current.setLength)
                    publish()
                }
            } else {
                val limit = current.duration.seconds
                if (limit != null) {
                    _remainingSeconds.value = (limit - _elapsedActiveSeconds.value).toInt().coerceAtLeast(0)
                    if (_elapsedActiveSeconds.value >= limit) {
                        markComplete()
                    }
                }
            }
        }
    }
}
