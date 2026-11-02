package com.forgeai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeai.assistant.inference.ForgeRuntime

@Composable
fun ChatScreen(runtime: ForgeRuntime) {
    var draft by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(runtime.engineStatus)
        if (!runtime.modelReady && !runtime.generating) {
            Text("Go to Setup, import a GGUF model and load it to begin.")
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(runtime.messages) { item ->
                ScreenCard(
                    item.speaker,
                    item.message.ifBlank { "Generating…" },
                    emptyList()
                )
            }
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("Message to Forge") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            enabled = runtime.modelReady && !runtime.generating
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                enabled = runtime.modelReady && !runtime.busy && !runtime.generating && draft.isNotBlank(),
                onClick = { val message = draft; draft = ""; runtime.send(message) }
            ) { Text("Send") }
            OutlinedButton(
                enabled = runtime.generating,
                onClick = { runtime.stopGeneration() }
            ) { Text("Stop") }
        }
        Text(runtime.operationStatus, modifier = Modifier.padding(bottom = 8.dp))
    }
}
