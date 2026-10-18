package com.zeki.emdr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeki.emdr.theme.emdrColors

@Composable
fun SetupCard(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    caption: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = emdrColors()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surface.copy(alpha = if (enabled) 1f else 0.55f),
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
            )
            if (caption != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}
