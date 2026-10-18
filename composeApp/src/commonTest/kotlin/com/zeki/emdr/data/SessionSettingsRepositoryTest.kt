package com.zeki.emdr.data

import com.russhwolf.settings.MapSettings
import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.model.OrbPalette
import com.zeki.emdr.model.SessionDuration
import com.zeki.emdr.model.SessionMode
import com.zeki.emdr.model.SessionSettings
import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.SetLength
import com.zeki.emdr.model.SupportMilestone
import com.zeki.emdr.model.ThemePreference
import com.zeki.emdr.model.VisualPattern
import com.zeki.emdr.theme.OrbSage
import com.zeki.emdr.theme.resolvesToDark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionSettingsRepositoryTest {
    @Test
    fun freshInstallSeedsClinicalBeginnerDefaults() {
        val settings = MapSettings()
        val seeded = SessionSettingsRepository(settings).state.value
        assertEquals(SessionMode.VISUAL_AND_AUDIO, seeded.mode)
        assertEquals(32, seeded.bpm)
        assertEquals(SessionType.SETS, seeded.sessionType)
        assertEquals(SetLength.PASSES_24, seeded.setLength)
        assertEquals(24, seeded.setLength.passes)
        assertEquals(OrbPalette.Sage, seeded.orbColor)
        assertEquals(OrbSage, seeded.orbColor.color)
        assertEquals(32, seeded.orbSize.diameterDp)
        assertEquals(AudioSound.SOFT_TONE, seeded.audioSound)
        assertEquals(VisualPattern.HORIZONTAL, seeded.pattern)
        assertTrue(seeded.allowBackgroundPlayback)
        assertTrue(seeded.hapticsEnabled)

        assertEquals("both", settings.getString("session.mode", ""))
        assertEquals(32, settings.getInt("session.bpm", -1))
        assertEquals("sets", settings.getString("session.type", ""))
        assertEquals("24", settings.getString("session.set_length", ""))
        assertEquals("sage", settings.getString("session.orb_color", ""))
        assertEquals("medium", settings.getString("session.orb_size", ""))
        assertEquals("soft_tone", settings.getString("session.audio", ""))
        assertEquals("horizontal", settings.getString("session.pattern", ""))
        assertTrue(settings.getBoolean("session.allow_background_playback", false))
        assertTrue(settings.getBoolean("session.haptics_enabled", false))
        assertFalse(settings.getBoolean("has_supported_app", true))
    }

    @Test
    fun persistsBackgroundPlaybackPreference() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)
        assertTrue(repository.state.value.allowBackgroundPlayback)
        repository.setAllowBackgroundPlayback(false)
        assertFalse(SessionSettingsRepository(settings).state.value.allowBackgroundPlayback)
    }

    @Test
    fun persistsLocalSupportFlagWithoutIdentifiers() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)
        assertFalse(repository.state.value.hasSupportedApp)
        repository.markSupported()
        assertTrue(repository.state.value.hasSupportedApp)
        assertTrue(settings.getBoolean("has_supported_app", false))
        assertTrue(SessionSettingsRepository(settings).state.value.hasSupportedApp)
    }

    @Test
    fun persistsHapticsPreference() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)
        assertTrue(repository.state.value.hapticsEnabled)
        repository.setHapticsEnabled(false)
        assertFalse(SessionSettingsRepository(settings).state.value.hapticsEnabled)
    }

    @Test
    fun existingStoreIsNotOverwrittenByNewDefaults() {
        val settings = MapSettings()
        settings.putInt("session.bpm", 72)
        settings.putString("session.mode", "audio")
        settings.putString("session.type", "continuous")
        val loaded = SessionSettingsRepository(settings).state.value
        assertEquals(72, loaded.bpm)
        assertEquals(SessionMode.Audio, loaded.mode)
        assertEquals(SessionType.CONTINUOUS, loaded.sessionType)
    }

    @Test
    fun writesEveryMutationImmediately() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)

        repository.setMode(SessionMode.Audio)
        repository.setOrbColor(OrbPalette.Teal)
        repository.setPattern(VisualPattern.Infinity)
        repository.setAudioSound(AudioSound.WOODEN_METRONOME)
        repository.setBpm(72)
        repository.setDuration(SessionDuration.Minutes5)

        val reloaded = SessionSettingsRepository(settings).state.value
        assertEquals(SessionMode.Audio, reloaded.mode)
        assertEquals(OrbPalette.Teal, reloaded.orbColor)
        assertEquals(VisualPattern.Infinity, reloaded.pattern)
        assertEquals(AudioSound.WoodenMetronome, reloaded.audioSound)
        assertEquals(72, reloaded.bpm)
        assertEquals(SessionDuration.Minutes5, reloaded.duration)
    }

    @Test
    fun clampsBpmToClinicalRange() {
        val repository = SessionSettingsRepository(MapSettings())
        repository.setBpm(4)
        assertEquals(SessionSettings.MIN_BPM, repository.state.value.bpm)
        repository.setBpm(400)
        assertEquals(SessionSettings.MAX_BPM, repository.state.value.bpm)
    }

    @Test
    fun firstLaunchIsIncompleteUntilMarked() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)
        assertFalse(repository.state.value.hasCompletedFirstLaunch)
        repository.markFirstLaunchComplete()
        assertTrue(SessionSettingsRepository(settings).state.value.hasCompletedFirstLaunch)
    }

    @Test
    fun persistsSetBasedSessionType() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)
        repository.setSessionType(SessionType.SETS)
        repository.setSetLength(SetLength.PASSES_36)
        val reloaded = SessionSettingsRepository(settings).state.value
        assertEquals(SessionType.SETS, reloaded.sessionType)
        assertEquals(SetLength.PASSES_36, reloaded.setLength)
        assertEquals(36, reloaded.setLength.passes)
    }

    @Test
    fun recordsLaunchCountAndShowsEachMilestoneOnce() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)
        assertEquals(0, repository.state.value.appLaunchCount)
        assertEquals(null, repository.consumePendingMilestone())

        repeat(5) { repository.recordAppLaunch() }
        assertEquals(5, settings.getInt("app.launch_count", -1))
        assertEquals(SupportMilestone.Five, repository.consumePendingMilestone())
        assertTrue(settings.getBoolean("shownMilestone5", false))
        assertEquals(null, repository.consumePendingMilestone())

        repeat(15) { repository.recordAppLaunch() }
        assertEquals(20, repository.state.value.appLaunchCount)
        assertEquals(SupportMilestone.Twenty, repository.consumePendingMilestone())
        assertTrue(repository.state.value.shownMilestone20)
        assertEquals(null, repository.consumePendingMilestone())

        repeat(30) { repository.recordAppLaunch() }
        assertEquals(50, repository.state.value.appLaunchCount)
        assertEquals(SupportMilestone.Fifty, repository.consumePendingMilestone())
        assertTrue(repository.state.value.shownMilestone50)
        assertEquals(null, repository.consumePendingMilestone())
    }

    @Test
    fun persistsThemePreference() {
        val settings = MapSettings()
        val repository = SessionSettingsRepository(settings)
        repository.setThemePreference(ThemePreference.Light)
        assertEquals(ThemePreference.Light, SessionSettingsRepository(settings).state.value.themePreference)
    }

    @Test
    fun themePreferenceResolvesAgainstSystem() {
        assertTrue(ThemePreference.System.resolvesToDark(systemDark = true))
        assertFalse(ThemePreference.System.resolvesToDark(systemDark = false))
        assertFalse(ThemePreference.Light.resolvesToDark(systemDark = true))
        assertTrue(ThemePreference.Dark.resolvesToDark(systemDark = false))
    }
}
