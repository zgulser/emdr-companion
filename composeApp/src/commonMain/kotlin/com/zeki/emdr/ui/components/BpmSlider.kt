package com.zeki.emdr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeki.emdr.model.SessionSettings
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import com.zeki.emdr.theme.EmdrButtonShape
import com.zeki.emdr.theme.EmdrMinTouchTarget
import com.zeki.emdr.theme.emdrColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun BpmSlider(
    bpm: Int,
    onBpmChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = emdrColors()
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.bpm_value, bpm),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BpmStepButton(
                onClick = { onBpmChange(SessionSettings.clampBpm(bpm - 1)) },
                enabled = enabled && bpm > SessionSettings.MIN_BPM,
            ) {
                Icon(
                    Icons.Rounded.Remove,
                    contentDescription = stringResource(Res.string.slower),
                    tint = colors.text,
                )
            }
            Slider(
                value = bpm.toFloat(),
                onValueChange = { onBpmChange(SessionSettings.clampBpm(it.toInt())) },
                valueRange = SessionSettings.MIN_BPM.toFloat()..SessionSettings.MAX_BPM.toFloat(),
                steps = SessionSettings.MAX_BPM - SessionSettings.MIN_BPM - 1,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(EmdrMinTouchTarget),
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary,
                    inactiveTrackColor = colors.track,
                    disabledThumbColor = colors.primary.copy(alpha = 0.4f),
                    disabledActiveTrackColor = colors.primary.copy(alpha = 0.3f),
                    disabledInactiveTrackColor = colors.track,
                ),
            )
            BpmStepButton(
                onClick = { onBpmChange(SessionSettings.clampBpm(bpm + 1)) },
                enabled = enabled && bpm < SessionSettings.MAX_BPM,
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = stringResource(Res.string.faster),
                    tint = colors.text,
                )
            }
        }
    }
}

@Composable
private fun BpmStepButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = emdrColors()
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = EmdrButtonShape,
        color = colors.surfaceElevated,
        modifier = Modifier.size(EmdrMinTouchTarget),
    ) {
        Box(contentAlignment = Alignment.Center, content = { content() })
    }
}
