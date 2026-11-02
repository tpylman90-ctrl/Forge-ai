package com.forgeai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen() {
    var draft by rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Conversation", style = MaterialTheme.typography.headlineSmall)
        ScreenCard(
            "Local AI not connected",
            "This is the G0 interface. Messages cannot be generated until the G1 inference engine is integrated.",
            listOf("No simulated assistant responses", "Conversation persistence: planned for G2")
        )
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("Message draft") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {}, enabled = false) {
            Text("Send (available in G1)")
        }
        Text(
            "Draft text stays in this screen's temporary state.",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
