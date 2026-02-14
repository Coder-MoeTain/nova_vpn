package com.novavpn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonCyan = Color(0xFF00E5FF)
private val NeonPurple = Color(0xFFB388FF)
private val DarkBg = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1E)
private val OnSurface = Color(0xFFE0E0E0)
private val ErrorRed = Color(0xFFFF5252)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonPurple,
    onSecondary = Color.Black,
    background = DarkBg,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    error = ErrorRed,
    onError = Color.Black
)

@Composable
fun NovaVpnTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
