package com.forgeai.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgeai.assistant.inference.ChatLine
import com.forgeai.assistant.inference.ForgeRuntime

@Composable
private fun MessageBubble(line: ChatLine) {
    val isUser = line.speaker == "You"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.89f),
            shape = RoundedCornerShape(18.dp),
            color = if (isUser) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isUser) 0.dp else 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = line.speaker,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = line.message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun ChatScreen(runtime: ForgeRuntime) {
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Keep new turns in view; don't auto-scroll on every generated token.
    LaunchedEffect(runtime.messages.size) {
        if (runtime.messages.isNotEmpty()) {
            listState.animateScrollToItem(runtime.messages.lastIndex)
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(runtime.engineStatus, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary)
        if (!runtime.modelReady && !runtime.generating) {
            Text("Open Setup to load a GGUF model.")
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(runtime.messages) { index, line ->
                MessageBubble(
                    if (runtime.generating && index == runtime.messages.lastIndex && line.message.isBlank()) {
                        line.copy(message = "Thinking…")
                    } else line
                )
            }
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("Message Forge") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            minLines = 1,
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
        Text(
            runtime.operationStatus,
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 2
        )
    }
}
