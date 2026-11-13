package com.worldflags.quiz.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalReducedMotion = staticCompositionLocalOf { false }

private val AtlasColors = lightColorScheme(
    primary = AtlasPurple, onPrimary = AtlasWhite,
    primaryContainer = AtlasLilac, onPrimaryContainer = AtlasInk,
    secondary = AtlasPurple, onSecondary = AtlasWhite,
    secondaryContainer = AtlasLime, onSecondaryContainer = AtlasInk,
    tertiary = AtlasPurple, onTertiary = AtlasWhite,
    background = AtlasPaper, onBackground = AtlasInk,
    surface = AtlasWhite, onSurface = AtlasInk,
    surfaceVariant = AtlasPaper, onSurfaceVariant = AtlasInk.copy(alpha = 0.76f),
    outline = AtlasPurple.copy(alpha = 0.4f), error = AtlasPurple, onError = AtlasWhite
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AtlasColors, typography = AtlasTypography, content = content)
}
