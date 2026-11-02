package com.forgeai.assistant.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun DiagnosticsScreen() = ScreenCard(
    "Diagnostics",
    "Build: Forge AI 0.1.0 — G0",
    listOf("UI: Compose application scaffold", "Inference engine: not installed", "Persistent memory: not installed", "App permissions: none requested")
)
