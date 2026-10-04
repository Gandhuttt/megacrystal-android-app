package com.example.megacrystal_android_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Figma uses light surfaces throughout the app, including on dark-mode devices.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0066FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF2FF),
    onPrimaryContainer = Color(0xFF001A42),
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFAF8FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFF3F2F8),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFB3261E)
)

@Composable
fun MegacrystalandroidappTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
