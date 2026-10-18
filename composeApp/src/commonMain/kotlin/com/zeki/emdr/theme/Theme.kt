package com.zeki.emdr.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeki.emdr.model.ThemePreference

val EmdrMinTouchTarget = 48.dp

fun Modifier.emdrTouchTarget(): Modifier =
    defaultMinSize(minWidth = EmdrMinTouchTarget, minHeight = EmdrMinTouchTarget)

fun ThemePreference.resolvesToDark(systemDark: Boolean): Boolean =
    when (this) {
        ThemePreference.System -> systemDark
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }

private fun EmdrPalette.toColorScheme() =
    if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = text,
            secondary = primaryDim,
            onSecondary = onPrimary,
            background = background,
            onBackground = text,
            surface = surface,
            onSurface = text,
            surfaceVariant = surfaceElevated,
            onSurfaceVariant = textSecondary,
            outline = outline,
            outlineVariant = track,
            error = stop,
            onError = onPrimary,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = text,
            secondary = primaryDim,
            onSecondary = onPrimary,
            background = background,
            onBackground = text,
            surface = surface,
            onSurface = text,
            surfaceVariant = surfaceElevated,
            onSurfaceVariant = textSecondary,
            outline = outline,
            outlineVariant = track,
            error = stop,
            onError = onPrimary,
        )
    }

@Composable
fun EmdrTheme(
    preference: ThemePreference = ThemePreference.System,
    content: @Composable () -> Unit,
) {
    val palette = if (preference.resolvesToDark(isSystemInDarkTheme())) {
        EmdrDarkPalette
    } else {
        EmdrLightPalette
    }
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides EmdrMinTouchTarget,
        LocalEmdrPalette provides palette,
        LocalContentColor provides palette.text,
    ) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(),
            typography = EmdrTypography,
            shapes = EmdrShapes,
            content = content,
        )
    }
}
