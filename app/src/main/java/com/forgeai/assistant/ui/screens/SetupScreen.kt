package com.forgeai.assistant.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(onOpenChat: () -> Unit) {
    ScreenCard(
        title = "Initialize Forge",
        description = "Your personal assistant will run on your device. This first build establishes navigation and Android packaging.",
        details = listOf(
            "Current milestone: G0 — app shell",
            "Model import and verification: planned for G1",
            "No AI model is bundled or connected yet",
            "No internet or personal-data permissions requested"
        )
    )
    Spacer(Modifier.height(18.dp))
    Button(onClick = onOpenChat) { Text("Explore chat interface") }
}
