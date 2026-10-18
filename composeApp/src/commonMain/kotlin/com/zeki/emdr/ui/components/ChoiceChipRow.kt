package com.zeki.emdr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeki.emdr.theme.EmdrButtonShape
import com.zeki.emdr.theme.EmdrChipShape
import com.zeki.emdr.theme.EmdrMinTouchTarget
import com.zeki.emdr.resources.Res
import com.zeki.emdr.resources.*
import com.zeki.emdr.theme.emdrColors
import com.zeki.emdr.theme.emdrTouchTarget
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> ChoiceChipRow(
    options: Array<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ChoiceChipRow(
        options = options.asList(),
        selected = selected,
        label = label,
        onSelect = onSelect,
        modifier = modifier,
        enabled = enabled,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChipRow(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = emdrColors()
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                onClick = { onSelect(option) },
                enabled = enabled,
                shape = EmdrChipShape,
                color = if (isSelected) colors.primaryContainer else colors.surfaceElevated,
                border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.outline),
                modifier = Modifier
                    .emdrTouchTarget()
                    .heightIn(min = EmdrMinTouchTarget),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            !enabled -> colors.textSecondary
                            isSelected -> colors.primary
                            else -> colors.text
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun StartSessionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = emdrColors()
    Surface(
        onClick = onClick,
        shape = EmdrButtonShape,
        color = colors.primary,
        modifier = modifier
            .fillMaxWidth()
            .emdrTouchTarget()
            .heightIn(min = 56.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            Text(
                text = stringResource(Res.string.start_session),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onPrimary,
            )
        }
    }
}
