package com.zeki.emdr.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val EmdrPrimary = Color(0xFF7CA982)
val EmdrPrimaryDim = Color(0xFF5E8A66)
val EmdrOnPrimary = Color(0xFF121417)
val EmdrStop = Color(0xFFD9897B)

val OrbSage = Color(0xFF7CA982)
val OrbSoftWhite = Color(0xFFE6E8EC)
val OrbAmber = Color(0xFFD4A574)
val OrbTeal = Color(0xFF6BB3B0)
val OrbLavender = Color(0xFFA89CC8)
val OrbCoral = Color(0xFFD9897B)

data class EmdrPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val primary: Color,
    val primaryDim: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val text: Color,
    val textSecondary: Color,
    val textHint: Color,
    val textMuted: Color,
    val outline: Color,
    val track: Color,
    val stop: Color,
    val supporter: Color,
    val isDark: Boolean,
)

val EmdrDarkPalette = EmdrPalette(
    background = Color(0xFF121417),
    surface = Color(0xFF1A1D22),
    surfaceElevated = Color(0xFF22262C),
    primary = EmdrPrimary,
    primaryDim = EmdrPrimaryDim,
    primaryContainer = Color(0xFF243028),
    onPrimary = EmdrOnPrimary,
    text = Color(0xFFE6E8EC),
    textSecondary = Color(0xFFA8ADB6),
    textHint = Color(0xFF9BA1B0),
    textMuted = Color(0xFF6B717C),
    outline = Color(0xFF2A2F36),
    track = Color(0xFF2E333B),
    stop = EmdrStop,
    supporter = Color(0xFF1C2026),
    isDark = true,
)

val EmdrLightPalette = EmdrPalette(
    background = Color(0xFFF4F3EF),
    surface = Color(0xFFFFFEFA),
    surfaceElevated = Color(0xFFEBE8E1),
    primary = EmdrPrimary,
    primaryDim = EmdrPrimaryDim,
    primaryContainer = Color(0xFFDCE8DE),
    onPrimary = EmdrOnPrimary,
    text = Color(0xFF2C3036),
    textSecondary = Color(0xFF5A5F66),
    textHint = Color(0xFF6A707C),
    textMuted = Color(0xFF8B9097),
    outline = Color(0xFFD4D0C8),
    track = Color(0xFFD8D4CC),
    stop = Color(0xFFC46B5E),
    supporter = Color(0xFFE6E4DF),
    isDark = false,
)

val LocalEmdrPalette = staticCompositionLocalOf { EmdrDarkPalette }

@Composable
fun emdrColors(): EmdrPalette = LocalEmdrPalette.current
