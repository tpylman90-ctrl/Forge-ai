package com.forgeai.assistant.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun MemoryScreen() = ScreenCard(
    "Long-term Memory",
    "Explicitly approved memories will be stored privately on your device once G3 is implemented.",
    listOf("Review before saving", "Edit individual entries", "Delete any entry", "Keep data out of public GitHub")
)
