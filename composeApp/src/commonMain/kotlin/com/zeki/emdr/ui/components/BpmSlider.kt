package com.zeki.emdr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeki.emdr.model.SessionSettings
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.bpm_value, bpm),
                style = MaterialTheme.typography.labelLarge,
                color = colors.text,
            )
            Text(
                text = stringResource(
                    Res.string.bpm_range,
                    SessionSettings.MIN_BPM,
                    SessionSettings.MAX_BPM,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
        }
        Slider(
            value = bpm.toFloat(),
            onValueChange = { onBpmChange(it.toInt()) },
            valueRange = SessionSettings.MIN_BPM.toFloat()..SessionSettings.MAX_BPM.toFloat(),
            steps = SessionSettings.MAX_BPM - SessionSettings.MIN_BPM - 1,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
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
    }
}
