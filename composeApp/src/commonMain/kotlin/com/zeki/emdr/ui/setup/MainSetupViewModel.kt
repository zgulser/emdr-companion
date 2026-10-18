package com.zeki.emdr.ui.setup

import androidx.lifecycle.ViewModel
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.model.OrbPalette
import com.zeki.emdr.model.OrbSize
import com.zeki.emdr.model.SessionDuration
import com.zeki.emdr.model.SessionMode
import com.zeki.emdr.model.SessionSettings
import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.SetLength
import com.zeki.emdr.model.ThemePreference
import com.zeki.emdr.model.VisualPattern
import kotlinx.coroutines.flow.StateFlow

class MainSetupViewModel(
    private val repository: SessionSettingsRepository,
) : ViewModel() {
    val settings: StateFlow<SessionSettings> = repository.state

    fun setMode(mode: SessionMode) = repository.setMode(mode)
    fun setOrbColor(color: OrbPalette) = repository.setOrbColor(color)
    fun setPattern(pattern: VisualPattern) = repository.setPattern(pattern)
    fun setOrbSize(size: OrbSize) = repository.setOrbSize(size)
    fun setAudioSound(sound: AudioSound) = repository.setAudioSound(sound)
    fun setBpm(bpm: Int) = repository.setBpm(bpm)
    fun setDuration(duration: SessionDuration) = repository.setDuration(duration)
    fun setSessionType(type: SessionType) = repository.setSessionType(type)
    fun setSetLength(length: SetLength) = repository.setSetLength(length)
    fun setThemePreference(preference: ThemePreference) = repository.setThemePreference(preference)
    fun setAllowBackgroundPlayback(allowed: Boolean) = repository.setAllowBackgroundPlayback(allowed)
    fun setHapticsEnabled(enabled: Boolean) = repository.setHapticsEnabled(enabled)
    fun markFirstLaunchComplete() = repository.markFirstLaunchComplete()
}
