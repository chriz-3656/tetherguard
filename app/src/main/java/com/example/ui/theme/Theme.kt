package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProfessionalDarkColorScheme = darkColorScheme(
    primary = Cobalt500,
    onPrimary = Color.White,
    primaryContainer = Cobalt900,
    onPrimaryContainer = Color.White,

    secondary = Cobalt500,
    onSecondary = Color.White,
    secondaryContainer = Cobalt900,
    onSecondaryContainer = Color.White,

    tertiary = Emerald500,
    onTertiary = Color.White,
    tertiaryContainer = Emerald900,
    onTertiaryContainer = Emerald400,

    error = Crimson500,
    onError = Color.White,
    errorContainer = Crimson900,
    onErrorContainer = Color.White,

    background = Slate950,
    onBackground = TextPrimary,
    surface = Slate900,
    onSurface = TextPrimary,
    surfaceVariant = Slate850,
    onSurfaceVariant = TextSecondary,
    outline = Slate700,
    outlineVariant = Slate600
)

@Composable
fun TetherGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProfessionalDarkColorScheme,
        typography = Typography,
        content = content
    )
}
