package com.forgeai.assistant.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ForgeColors = darkColorScheme(
    primary = Color(0xFF40D6DF),
    onPrimary = Color(0xFF042C34),
    secondary = Color(0xFFB8DDE0),
    background = Color(0xFF10171C),
    surface = Color(0xFF182228),
    surfaceVariant = Color(0xFF26343C),
    onBackground = Color(0xFFE7F3F4),
    onSurface = Color(0xFFE7F3F4)
)

@Composable
fun ForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ForgeColors, content = content)
}
