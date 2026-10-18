package com.zeki.emdr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zeki.emdr.model.OrbPalette
import com.zeki.emdr.theme.EmdrMinTouchTarget
import com.zeki.emdr.theme.emdrColors
import com.zeki.emdr.ui.localizedLabel

@Composable
fun ColorSwatchRow(
    selected: OrbPalette,
    onSelect: (OrbPalette) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = emdrColors()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OrbPalette.entries.forEach { palette ->
            val isSelected = palette == selected
            val name = palette.localizedLabel()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(EmdrMinTouchTarget)
                    .semantics {
                        role = Role.Button
                        contentDescription = name
                        this.selected = isSelected
                    }
                    .clickable(enabled = enabled) { onSelect(palette) },
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 32.dp else 28.dp)
                        .clip(CircleShape)
                        .background(palette.color.copy(alpha = if (enabled) 1f else 0.45f))
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, colors.text, CircleShape)
                            } else {
                                Modifier.border(1.dp, colors.primary.copy(alpha = 0.25f), CircleShape)
                            },
                        ),
                )
            }
        }
    }
}
