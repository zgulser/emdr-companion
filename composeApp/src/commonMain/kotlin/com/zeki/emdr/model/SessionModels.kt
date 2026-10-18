package com.zeki.emdr.model

import androidx.compose.ui.graphics.Color
import com.zeki.emdr.theme.OrbAmber
import com.zeki.emdr.theme.OrbCoral
import com.zeki.emdr.theme.OrbLavender
import com.zeki.emdr.theme.OrbSage
import com.zeki.emdr.theme.OrbSoftWhite
import com.zeki.emdr.theme.OrbTeal

enum class SessionMode(val storageValue: String, val label: String) {
    Visual("visual", "Visual"),
    Audio("audio", "Audio"),
    Both("both", "Both"),
    ;

    val usesVisual: Boolean get() = this != Audio
    val usesAudio: Boolean get() = this != Visual

    companion object {
        val VISUAL_AND_AUDIO: SessionMode get() = Both

        fun fromStorage(value: String): SessionMode =
            entries.firstOrNull { it.storageValue == value } ?: VISUAL_AND_AUDIO
    }
}

enum class VisualPattern(val storageValue: String, val label: String) {
    Horizontal("horizontal", "Horizontal"),
    Infinity("infinity", "Infinity"),
    Diagonal("diagonal", "Diagonal"),
    Vertical("vertical", "Vertical"),
    ;

    companion object {
        val HORIZONTAL: VisualPattern get() = Horizontal

        fun fromStorage(value: String): VisualPattern =
            entries.firstOrNull { it.storageValue == value } ?: HORIZONTAL
    }
}

enum class OrbSize(val storageValue: String, val label: String, val diameterDp: Int) {
    Small("small", "S", 18),
    Medium("medium", "M", 32),
    Large("large", "L", 40),
    ;

    companion object {
        fun fromStorage(value: String): OrbSize =
            entries.firstOrNull { it.storageValue == value } ?: Medium
    }
}

enum class OrbPalette(val storageValue: String, val label: String, val color: Color) {
    Sage("sage", "Sage", OrbSage),
    SoftWhite("soft_white", "Soft White", OrbSoftWhite),
    Amber("amber", "Amber", OrbAmber),
    Teal("teal", "Teal", OrbTeal),
    Lavender("lavender", "Lavender", OrbLavender),
    Coral("coral", "Coral", OrbCoral),
    ;

    companion object {
        fun fromStorage(value: String): OrbPalette =
            entries.firstOrNull { it.storageValue == value } ?: Sage
    }
}

enum class AudioSound(val storageValue: String, val label: String) {
    SoftTone("soft_tone", "Soft Sine Tones"),
    WoodenMetronome("wooden_metronome", "Wooden Metronome"),
    ;

    companion object {
        val WOODEN_METRONOME: AudioSound get() = WoodenMetronome
        val SOFT_TONE: AudioSound get() = SoftTone

        fun fromStorage(value: String): AudioSound =
            when (value) {
                WoodenMetronome.storageValue -> WoodenMetronome
                else -> SOFT_TONE
            }
    }
}

enum class SessionType(val storageValue: String, val label: String) {
    CONTINUOUS("continuous", "Continuous"),
    SETS("sets", "Sets"),
    ;

    companion object {
        fun fromStorage(value: String): SessionType =
            entries.firstOrNull { it.storageValue == value } ?: SETS
    }
}

enum class SetLength(val storageValue: String, val label: String, val passes: Int) {
    PASSES_24("24", "24", 24),
    PASSES_36("36", "36", 36),
    PASSES_48("48", "48", 48),
    ;

    companion object {
        fun fromStorage(value: String): SetLength =
            entries.firstOrNull { it.storageValue == value } ?: PASSES_24
    }
}

enum class SessionDuration(val storageValue: String, val label: String, val seconds: Int?) {
    Seconds30("30s", "30s", 30),
    Minutes1("1m", "1 min", 60),
    Minutes2("2m", "2 min", 120),
    Minutes5("5m", "5 min", 300),
    Open("open", "Open", null),
    ;

    companion object {
        fun fromStorage(value: String): SessionDuration =
            entries.firstOrNull { it.storageValue == value } ?: Minutes2
    }
}

enum class ThemePreference(val storageValue: String, val label: String) {
    System("system", "System"),
    Light("light", "Light"),
    Dark("dark", "Dark"),
    ;

    companion object {
        fun fromStorage(value: String): ThemePreference =
            entries.firstOrNull { it.storageValue == value } ?: System
    }
}

/** Spec / product name for the persisted session configuration. */
typealias EmdrSessionConfig = SessionSettings
typealias StimulationMode = SessionMode
typealias MotionPattern = VisualPattern

data class SessionSettings(
    val mode: SessionMode = SessionMode.VISUAL_AND_AUDIO,
    val orbColor: OrbPalette = OrbPalette.Sage,
    val pattern: MotionPattern = MotionPattern.HORIZONTAL,
    val orbSize: OrbSize = OrbSize.Medium,
    val audioSound: AudioSound = AudioSound.SOFT_TONE,
    val bpm: Int = DEFAULT_BPM,
    val duration: SessionDuration = SessionDuration.Minutes2,
    val sessionType: SessionType = SessionType.SETS,
    val setLength: SetLength = SetLength.PASSES_24,
    val themePreference: ThemePreference = ThemePreference.System,
    val allowBackgroundPlayback: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val hasSupportedApp: Boolean = false,
    val hasCompletedFirstLaunch: Boolean = false,
    val appLaunchCount: Int = 0,
    val shownMilestone5: Boolean = false,
    val shownMilestone20: Boolean = false,
    val shownMilestone50: Boolean = false,
) {
    companion object {
        const val MIN_BPM = 15
        const val MAX_BPM = 90
        const val DEFAULT_BPM = 32

        fun clampBpm(value: Int): Int = value.coerceIn(MIN_BPM, MAX_BPM)
    }
}
