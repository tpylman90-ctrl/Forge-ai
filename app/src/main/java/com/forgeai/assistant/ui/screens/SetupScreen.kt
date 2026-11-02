package com.forgeai.assistant.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgeai.assistant.inference.ForgeRuntime

@Composable
fun SetupScreen(runtime: ForgeRuntime, onOpenChat: () -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runtime.importModel(uri)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenCard(
            "Local AI engine",
            "Import a GGUF model from Downloads or another document provider. Models are copied to Forge's private storage. No model or conversations are uploaded.",
            listOf(
                "Bundled model: Qwen3-0.6B Q4_K_M (Apache 2.0)",
                "On first launch allow time and about 0.5 GB free storage for model setup",
                "No network or file-management permission required",
                "You can still import another GGUF if you want to change models"
            )
        )
        Text("Native engine: ${runtime.engineStatus}")
        Text("Import: ${runtime.operationStatus}")
        Text("Selected: ${runtime.modelName.ifBlank { "None" }}")
        if (runtime.imported) {
            Text("Size: ${runtime.modelSize / 1_048_576L} MiB")
            Text("SHA-256: ${runtime.modelHash.take(20)}…")
        }
        Button(
            enabled = !runtime.busy && !runtime.generating && !runtime.modelReady,
            onClick = { picker.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Import a different GGUF (optional)") }
        Button(
            enabled = runtime.imported && !runtime.busy && !runtime.generating && !runtime.modelReady,
            onClick = { runtime.loadModel() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Load model into RAM") }
        OutlinedButton(
            enabled = runtime.modelReady && !runtime.busy && !runtime.generating,
            onClick = { runtime.unloadModel() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Unload model") }
        Button(
            enabled = runtime.modelReady,
            onClick = onOpenChat,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start offline conversation") }
    }
}
