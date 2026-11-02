package com.forgeai.assistant.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeai.assistant.ui.screens.ChatScreen
import com.forgeai.assistant.ui.screens.DiagnosticsScreen
import com.forgeai.assistant.ui.screens.HistoryScreen
import com.forgeai.assistant.ui.screens.MemoryScreen
import com.forgeai.assistant.ui.screens.SettingsScreen
import com.forgeai.assistant.ui.screens.SetupScreen

internal enum class ForgePage(val label: String) {
    SETUP("Setup"),
    CHAT("Chat"),
    HISTORY("History"),
    MEMORY("Memory"),
    SETTINGS("Settings"),
    DIAGNOSTICS("Diagnostics")
}

@Composable
fun ForgeApp() {
    var selectedName by rememberSaveable { mutableStateOf(ForgePage.SETUP.name) }
    val current = ForgePage.entries.firstOrNull { it.name == selectedName } ?: ForgePage.SETUP

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(
                    text = "FORGE  /  AI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Text(
                    text = "On-device personal assistant   •   G0 foundation",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ForgePage.entries.forEach { page ->
                        FilterChip(
                            selected = page == current,
                            onClick = { selectedName = page.name },
                            label = { Text(page.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            when (current) {
                ForgePage.SETUP -> SetupScreen(onOpenChat = { selectedName = ForgePage.CHAT.name })
                ForgePage.CHAT -> ChatScreen()
                ForgePage.HISTORY -> HistoryScreen()
                ForgePage.MEMORY -> MemoryScreen()
                ForgePage.SETTINGS -> SettingsScreen()
                ForgePage.DIAGNOSTICS -> DiagnosticsScreen()
            }
        }
    }
}
