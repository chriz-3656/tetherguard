package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TetherDarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberOnPrimary,
    primaryContainer = CyberPrimaryContainer,
    onPrimaryContainer = CyberPrimary,

    secondary = CyberSecondary,
    onSecondary = Color.Black,
    secondaryContainer = CyberSecondaryContainer,
    onSecondaryContainer = CyberSecondary,

    tertiary = CyberProtectedGreen,
    onTertiary = Color.Black,
    tertiaryContainer = CyberProtectedGreenContainer,
    onTertiaryContainer = CyberProtectedGreen,

    error = CyberAlertRed,
    onError = Color.White,
    errorContainer = CyberAlertRedContainer,
    onErrorContainer = CyberAlertRed,

    background = CyberBackground,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberSurfaceHighlight
)

@Composable
fun TetherGuardTheme(
    content: @Composable () -> Unit
) {
    // TetherGuard strictly adheres to the dark security console theme
    MaterialTheme(
        colorScheme = TetherDarkColorScheme,
        typography = Typography,
        content = content
    )
}
