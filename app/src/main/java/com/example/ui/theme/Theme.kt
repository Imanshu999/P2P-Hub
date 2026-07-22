package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = CyberBlue,
    onSecondary = Color.Black,
    tertiary = NeonPurple,
    onTertiary = Color.White,
    background = CyberBackgroundStart,
    onBackground = TextPrimary,
    surface = GlassSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceLight,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to ultra-modern dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
