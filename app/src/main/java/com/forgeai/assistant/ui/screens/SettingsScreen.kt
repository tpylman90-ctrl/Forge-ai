package com.forgeai.assistant.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen() = ScreenCard(
    "Settings",
    "The configurable model and privacy controls are planned. G0 makes no system-level changes.",
    listOf("Model selection and context size (G1)", "Retention and memory controls (G2–G3)", "Permissions are opt-in in future releases")
)
