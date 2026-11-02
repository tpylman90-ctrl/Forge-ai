package com.forgeai.assistant.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun HistoryScreen() = ScreenCard(
    "Conversation History",
    "Previous conversations will appear here after the G2 database milestone.",
    listOf("List and search conversations", "Resume a thread", "Rename or delete threads")
)
