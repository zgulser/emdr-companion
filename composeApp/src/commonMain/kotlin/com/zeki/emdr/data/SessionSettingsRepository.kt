package com.zeki.emdr.data

import com.russhwolf.settings.Settings
import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.model.OrbPalette
import com.zeki.emdr.model.OrbSize
import com.zeki.emdr.model.SessionDuration
import com.zeki.emdr.model.SessionMode
import com.zeki.emdr.model.SessionSettings
import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.SetLength
import com.zeki.emdr.model.SupportMilestone
import com.zeki.emdr.model.ThemePreference
import com.zeki.emdr.model.VisualPattern
import com.zeki.emdr.model.pendingSupportMilestone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * On-device source of truth for setup choices. Every mutation writes immediately
 * through Multiplatform Settings — no network, no debounce.
 */
class SessionSettingsRepository(
    private val settings: Settings = Settings(),
) {
    private val _state = MutableStateFlow(loadOrSeed())
    val state: StateFlow<SessionSettings> = _state.asStateFlow()

    fun update(transform: (SessionSettings) -> SessionSettings) {
        val next = transform(_state.value)
        write(next)
        _state.value = next
    }

    fun setMode(mode: SessionMode) = update { it.copy(mode = mode) }

    fun setOrbColor(color: OrbPalette) = update { it.copy(orbColor = color) }

    fun setPattern(pattern: VisualPattern) = update { it.copy(pattern = pattern) }

    fun setOrbSize(size: OrbSize) = update { it.copy(orbSize = size) }

    fun setAudioSound(sound: AudioSound) = update { it.copy(audioSound = sound) }

    fun setBpm(bpm: Int) = update { it.copy(bpm = SessionSettings.clampBpm(bpm)) }

    fun setDuration(duration: SessionDuration) = update { it.copy(duration = duration) }

    fun setSessionType(type: SessionType) = update { it.copy(sessionType = type) }

    fun setSetLength(length: SetLength) = update { it.copy(setLength = length) }

    fun setThemePreference(preference: ThemePreference) =
        update { it.copy(themePreference = preference) }

    fun setAllowBackgroundPlayback(allowed: Boolean) =
        update { it.copy(allowBackgroundPlayback = allowed) }

    fun setHapticsEnabled(enabled: Boolean) =
        update { it.copy(hapticsEnabled = enabled) }

    fun markSupported() = update { it.copy(hasSupportedApp = true) }

    fun markFirstLaunchComplete() = update { it.copy(hasCompletedFirstLaunch = true) }

    fun recordAppLaunch() = update { it.copy(appLaunchCount = it.appLaunchCount + 1) }

    /**
     * Marks the matching launch milestone as shown and returns it.
     * Null when the current count is not 5 / 20 / 50, or that sheet already appeared.
     */
    fun consumePendingMilestone(): SupportMilestone? {
        val pending = _state.value.pendingSupportMilestone() ?: return null
        update { current ->
            when (pending) {
                SupportMilestone.Five -> current.copy(shownMilestone5 = true)
                SupportMilestone.Twenty -> current.copy(shownMilestone20 = true)
                SupportMilestone.Fifty -> current.copy(shownMilestone50 = true)
            }
        }
        return pending
    }

    private fun loadOrSeed(): SessionSettings {
        if (!hasPersistedSession()) {
            val defaults = SessionSettings()
            write(defaults)
            return defaults
        }
        return read()
    }

    private fun hasPersistedSession(): Boolean =
        settings.hasKey(KEY_BPM) ||
            settings.hasKey(KEY_MODE) ||
            settings.hasKey(KEY_SESSION_TYPE) ||
            settings.hasKey(KEY_FIRST_LAUNCH_DONE)

    private fun read(): SessionSettings {
        val defaults = SessionSettings()
        return SessionSettings(
            mode = SessionMode.fromStorage(settings.getString(KEY_MODE, defaults.mode.storageValue)),
            orbColor = OrbPalette.fromStorage(settings.getString(KEY_ORB_COLOR, defaults.orbColor.storageValue)),
            pattern = VisualPattern.fromStorage(settings.getString(KEY_PATTERN, defaults.pattern.storageValue)),
            orbSize = OrbSize.fromStorage(settings.getString(KEY_ORB_SIZE, defaults.orbSize.storageValue)),
            audioSound = AudioSound.fromStorage(settings.getString(KEY_AUDIO, defaults.audioSound.storageValue)),
            bpm = SessionSettings.clampBpm(settings.getInt(KEY_BPM, defaults.bpm)),
            duration = SessionDuration.fromStorage(
                settings.getString(KEY_DURATION, defaults.duration.storageValue),
            ),
            sessionType = SessionType.fromStorage(
                settings.getString(KEY_SESSION_TYPE, defaults.sessionType.storageValue),
            ),
            setLength = SetLength.fromStorage(
                settings.getString(KEY_SET_LENGTH, defaults.setLength.storageValue),
            ),
            themePreference = ThemePreference.fromStorage(
                settings.getString(KEY_THEME, defaults.themePreference.storageValue),
            ),
            allowBackgroundPlayback = settings.getBoolean(
                KEY_BACKGROUND_PLAYBACK,
                defaults.allowBackgroundPlayback,
            ),
            hapticsEnabled = settings.getBoolean(KEY_HAPTICS, defaults.hapticsEnabled),
            hasSupportedApp = settings.getBoolean(KEY_SUPPORTED, defaults.hasSupportedApp),
            hasCompletedFirstLaunch = settings.getBoolean(KEY_FIRST_LAUNCH_DONE, defaults.hasCompletedFirstLaunch),
            appLaunchCount = settings.getInt(KEY_LAUNCH_COUNT, defaults.appLaunchCount),
            shownMilestone5 = settings.getBoolean(KEY_MILESTONE_5, defaults.shownMilestone5),
            shownMilestone20 = settings.getBoolean(KEY_MILESTONE_20, defaults.shownMilestone20),
            shownMilestone50 = settings.getBoolean(KEY_MILESTONE_50, defaults.shownMilestone50),
        )
    }

    private fun write(value: SessionSettings) {
        settings.putString(KEY_MODE, value.mode.storageValue)
        settings.putString(KEY_ORB_COLOR, value.orbColor.storageValue)
        settings.putString(KEY_PATTERN, value.pattern.storageValue)
        settings.putString(KEY_ORB_SIZE, value.orbSize.storageValue)
        settings.putString(KEY_AUDIO, value.audioSound.storageValue)
        settings.putInt(KEY_BPM, value.bpm)
        settings.putString(KEY_DURATION, value.duration.storageValue)
        settings.putString(KEY_SESSION_TYPE, value.sessionType.storageValue)
        settings.putString(KEY_SET_LENGTH, value.setLength.storageValue)
        settings.putString(KEY_THEME, value.themePreference.storageValue)
        settings.putBoolean(KEY_BACKGROUND_PLAYBACK, value.allowBackgroundPlayback)
        settings.putBoolean(KEY_HAPTICS, value.hapticsEnabled)
        settings.putBoolean(KEY_SUPPORTED, value.hasSupportedApp)
        settings.putBoolean(KEY_FIRST_LAUNCH_DONE, value.hasCompletedFirstLaunch)
        settings.putInt(KEY_LAUNCH_COUNT, value.appLaunchCount)
        settings.putBoolean(KEY_MILESTONE_5, value.shownMilestone5)
        settings.putBoolean(KEY_MILESTONE_20, value.shownMilestone20)
        settings.putBoolean(KEY_MILESTONE_50, value.shownMilestone50)
    }

    private companion object {
        const val KEY_MODE = "session.mode"
        const val KEY_ORB_COLOR = "session.orb_color"
        const val KEY_PATTERN = "session.pattern"
        const val KEY_ORB_SIZE = "session.orb_size"
        const val KEY_AUDIO = "session.audio"
        const val KEY_BPM = "session.bpm"
        const val KEY_DURATION = "session.duration"
        const val KEY_SESSION_TYPE = "session.type"
        const val KEY_SET_LENGTH = "session.set_length"
        const val KEY_THEME = "app.theme_preference"
        const val KEY_BACKGROUND_PLAYBACK = "session.allow_background_playback"
        const val KEY_HAPTICS = "session.haptics_enabled"
        const val KEY_SUPPORTED = "has_supported_app"
        const val KEY_FIRST_LAUNCH_DONE = "app.has_completed_first_launch"
        const val KEY_LAUNCH_COUNT = "app.launch_count"
        const val KEY_MILESTONE_5 = "shownMilestone5"
        const val KEY_MILESTONE_20 = "shownMilestone20"
        const val KEY_MILESTONE_50 = "shownMilestone50"
    }
}
