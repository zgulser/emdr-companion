package com.zeki.emdr.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import com.zeki.emdr.theme.emdrColors
import com.zeki.emdr.theme.emdrTouchTarget
import com.zeki.emdr.ui.components.BpmSlider
import com.zeki.emdr.ui.components.ChoiceChipRow
import com.zeki.emdr.ui.components.ColorSwatchRow
import com.zeki.emdr.ui.components.SetupCard
import com.zeki.emdr.ui.localizedLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun SessionPreferencesForm(
    settings: SessionSettings,
    onMode: (SessionMode) -> Unit,
    onOrbColor: (OrbPalette) -> Unit,
    onPattern: (VisualPattern) -> Unit,
    onOrbSize: (OrbSize) -> Unit,
    onAudioSound: (AudioSound) -> Unit,
    onBpm: (Int) -> Unit,
    onDuration: (SessionDuration) -> Unit,
    onSessionType: (SessionType) -> Unit,
    onSetLength: (SetLength) -> Unit,
    onThemePreference: (ThemePreference) -> Unit,
    onAllowBackgroundPlayback: (Boolean) -> Unit,
    onHapticsEnabled: (Boolean) -> Unit,
) {
    val colors = emdrColors()
    val visualEnabled = settings.mode.usesVisual
    val audioEnabled = settings.mode.usesAudio

    SetupCard(
        title = stringResource(Res.string.appearance_title),
        caption = stringResource(Res.string.appearance_caption),
    ) {
        ChoiceChipRow(
            options = ThemePreference.entries,
            selected = settings.themePreference,
            label = { it.localizedLabel() },
            onSelect = onThemePreference,
        )
    }

    SetupCard(
        title = stringResource(Res.string.mode_title),
        caption = stringResource(Res.string.mode_caption),
    ) {
        ChoiceChipRow(
            options = SessionMode.entries,
            selected = settings.mode,
            label = { it.localizedLabel() },
            onSelect = onMode,
        )
    }

    SetupCard(
        title = stringResource(Res.string.visual_title),
        caption = stringResource(
            if (visualEnabled) Res.string.visual_caption else Res.string.visual_caption_disabled,
        ),
        enabled = visualEnabled,
    ) {
        Text(
            text = stringResource(Res.string.orb_color),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
        )
        Spacer(Modifier.height(8.dp))
        ColorSwatchRow(
            selected = settings.orbColor,
            onSelect = onOrbColor,
            enabled = visualEnabled,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.pattern),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
        )
        Spacer(Modifier.height(8.dp))
        ChoiceChipRow(
            options = VisualPattern.entries,
            selected = settings.pattern,
            label = { it.localizedLabel() },
            onSelect = onPattern,
            enabled = visualEnabled,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.size),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
        )
        Spacer(Modifier.height(8.dp))
        ChoiceChipRow(
            options = OrbSize.entries,
            selected = settings.orbSize,
            label = { it.localizedLabel() },
            onSelect = onOrbSize,
            enabled = visualEnabled,
        )
    }

    SetupCard(
        title = stringResource(Res.string.audio_title),
        caption = stringResource(
            if (audioEnabled) Res.string.audio_caption else Res.string.audio_caption_disabled,
        ),
        enabled = audioEnabled,
    ) {
        ChoiceChipRow(
            options = listOf(AudioSound.SoftTone, AudioSound.WOODEN_METRONOME),
            selected = settings.audioSound,
            label = { it.localizedLabel() },
            onSelect = onAudioSound,
            enabled = audioEnabled,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.audio_headphones_hint),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
            ),
            color = colors.textHint,
        )
    }

    SetupCard(
        title = stringResource(Res.string.haptics_title),
        caption = stringResource(Res.string.haptics_caption),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .emdrTouchTarget(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    if (settings.hapticsEnabled) Res.string.state_on else Res.string.state_off,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary,
            )
            Switch(
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsEnabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.primary,
                    checkedThumbColor = colors.onPrimary,
                ),
            )
        }
    }

    SetupCard(
        title = stringResource(Res.string.tempo_title),
        caption = stringResource(Res.string.tempo_caption),
    ) {
        BpmSlider(
            bpm = settings.bpm,
            onBpmChange = onBpm,
        )
    }

    SetupCard(
        title = stringResource(Res.string.session_type_title),
        caption = stringResource(Res.string.session_type_caption),
    ) {
        ChoiceChipRow(
            options = SessionType.entries,
            selected = settings.sessionType,
            label = { it.localizedLabel() },
            onSelect = onSessionType,
        )
    }

    if (settings.sessionType == SessionType.SETS) {
        SetupCard(
            title = stringResource(Res.string.set_length_title),
            caption = stringResource(Res.string.set_length_caption),
        ) {
            ChoiceChipRow(
                options = SetLength.entries,
                selected = settings.setLength,
                label = { it.localizedLabel() },
                onSelect = onSetLength,
            )
        }
    } else {
        SetupCard(
            title = stringResource(Res.string.duration_title),
            caption = stringResource(Res.string.duration_caption),
        ) {
            ChoiceChipRow(
                options = SessionDuration.entries,
                selected = settings.duration,
                label = { it.localizedLabel() },
                onSelect = onDuration,
            )
        }
    }

    SetupCard(
        title = stringResource(Res.string.background_title),
        caption = stringResource(Res.string.background_caption),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .emdrTouchTarget(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    if (settings.allowBackgroundPlayback) Res.string.state_on else Res.string.state_off,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary,
            )
            Switch(
                checked = settings.allowBackgroundPlayback,
                onCheckedChange = onAllowBackgroundPlayback,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.primary,
                    checkedThumbColor = colors.onPrimary,
                ),
            )
        }
    }
}
