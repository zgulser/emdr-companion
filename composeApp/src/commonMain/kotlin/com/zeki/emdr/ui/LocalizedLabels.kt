package com.zeki.emdr.ui

import androidx.compose.runtime.Composable
import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.model.OrbPalette
import com.zeki.emdr.model.OrbSize
import com.zeki.emdr.model.SessionDuration
import com.zeki.emdr.model.SessionMode
import com.zeki.emdr.model.SessionType
import com.zeki.emdr.model.SetLength
import com.zeki.emdr.model.ThemePreference
import com.zeki.emdr.model.VisualPattern
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SessionMode.localizedLabel(): String = stringResource(
    when (this) {
        SessionMode.Visual -> Res.string.mode_visual
        SessionMode.Audio -> Res.string.mode_audio
        SessionMode.Both -> Res.string.mode_both
    },
)

@Composable
fun ThemePreference.localizedLabel(): String = stringResource(
    when (this) {
        ThemePreference.System -> Res.string.theme_system
        ThemePreference.Light -> Res.string.theme_light
        ThemePreference.Dark -> Res.string.theme_dark
    },
)

@Composable
fun VisualPattern.localizedLabel(): String = stringResource(
    when (this) {
        VisualPattern.Horizontal -> Res.string.pattern_horizontal
        VisualPattern.Infinity -> Res.string.pattern_infinity
        VisualPattern.Diagonal -> Res.string.pattern_diagonal
        VisualPattern.Vertical -> Res.string.pattern_vertical
    },
)

@Composable
fun OrbSize.localizedLabel(): String = stringResource(
    when (this) {
        OrbSize.Small -> Res.string.size_s
        OrbSize.Medium -> Res.string.size_m
        OrbSize.Large -> Res.string.size_l
    },
)

@Composable
fun OrbPalette.localizedLabel(): String = stringResource(
    when (this) {
        OrbPalette.Sage -> Res.string.color_sage
        OrbPalette.SoftWhite -> Res.string.color_soft_white
        OrbPalette.Amber -> Res.string.color_amber
        OrbPalette.Teal -> Res.string.color_teal
        OrbPalette.Lavender -> Res.string.color_lavender
        OrbPalette.Coral -> Res.string.color_coral
    },
)

@Composable
fun AudioSound.localizedLabel(): String = stringResource(
    when (this) {
        AudioSound.SoftTone -> Res.string.sound_soft_sine_default
        AudioSound.WoodenMetronome -> Res.string.sound_wooden
    },
)

@Composable
fun SessionType.localizedLabel(): String = stringResource(
    when (this) {
        SessionType.CONTINUOUS -> Res.string.type_continuous
        SessionType.SETS -> Res.string.type_sets
    },
)

@Composable
fun SetLength.localizedLabel(): String = stringResource(Res.string.set_length_passes, passes)

@Composable
fun SessionDuration.localizedLabel(): String = stringResource(
    when (this) {
        SessionDuration.Seconds30 -> Res.string.duration_30s
        SessionDuration.Minutes1 -> Res.string.duration_1m
        SessionDuration.Minutes2 -> Res.string.duration_2m
        SessionDuration.Minutes5 -> Res.string.duration_5m
        SessionDuration.Open -> Res.string.duration_open
    },
)
