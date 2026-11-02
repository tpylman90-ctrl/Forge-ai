package com.forgeai.assistant.ui.screens

import androidx.compose.runtime.Composable
import com.forgeai.assistant.inference.ForgeRuntime

@Composable
fun DiagnosticsScreen(runtime: ForgeRuntime) = ScreenCard(
    "Local AI Diagnostics",
    "Build: Forge AI 0.2.0 — G1. Native llama.cpp library is built into this APK; model weights are not.",
    listOf(
        "Engine: ${runtime.engineStatus}",
        "Operation: ${runtime.operationStatus}",
        "Model: ${runtime.modelName.ifBlank { "Not imported" }}",
        "Model size: ${runtime.modelSize / 1_048_576L} MiB",
        "SHA-256: ${runtime.modelHash.ifBlank { "Not available" }}",
        "Last response: ${runtime.lastResponseCharacters} characters in ${"%.1f".format(runtime.lastGenerationSeconds)} seconds",
        "Context window: 2048 tokens (initial mobile setting)",
        "Messages are session-only until G2. No internet permission."
    )
)
