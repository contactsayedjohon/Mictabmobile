package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Indigo400,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = Indigo400,
    secondary = Teal400,
    onSecondary = Slate900,
    secondaryContainer = Slate700,
    onSecondaryContainer = Teal400,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark mode for a sleek aesthetic
  dynamicColor: Boolean = false, // Disable dynamic colors to maintain brand look
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}

