package com.forgeai.assistant.inference

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.os.StatFs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

data class ChatLine(val speaker: String, val message: String)

/**
 * Application-lifetime owner of a single native engine.
 * Text and model data are only stored in Android app-private storage.
 * G1 chat is intentionally ephemeral. G2 will add an explicit Room repository.
 */
class ForgeRuntime private constructor(private val context: Context) {
    companion object {
        @Volatile private var singleton: ForgeRuntime? = null
        fun obtain(context: Context): ForgeRuntime = singleton ?: synchronized(this) {
            singleton ?: ForgeRuntime(context.applicationContext).also { singleton = it }
        }
    }

    private val preferences = context.getSharedPreferences("forge_model_metadata", Context.MODE_PRIVATE)
    private val modelFile: File = File(File(context.filesDir, "models"), "current.gguf")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val engine: InferenceEngine = AiChat.getInferenceEngine(context)
    private var generation: Job? = null
    private val maxModelBytes = 3_000_000_000L

    val messages = mutableStateListOf<ChatLine>()
    var modelName by mutableStateOf(preferences.getString("name", "") ?: "")
        private set
    var modelHash by mutableStateOf(preferences.getString("sha256", "") ?: "")
        private set
    var modelSize by mutableStateOf(preferences.getLong("bytes", 0L))
        private set
    var engineStatus by mutableStateOf("Starting native engine")
        private set
    var operationStatus by mutableStateOf("No model loaded")
        private set
    var busy by mutableStateOf(false)
        private set
    var generating by mutableStateOf(false)
        private set
    var lastGenerationSeconds by mutableStateOf(0.0)
        private set
    var lastResponseCharacters by mutableStateOf(0)
        private set

    val imported: Boolean get() = modelFile.isFile && modelSize > 0
    val modelReady: Boolean get() = engine.state.value is InferenceEngine.State.ModelReady

    init {
        scope.launch {
            engine.state.collect { s ->
                engineStatus = when (s) {
                    is InferenceEngine.State.Uninitialized -> "Uninitialized"
                    is InferenceEngine.State.Initializing -> "Initializing native engine"
                    is InferenceEngine.State.Initialized -> "Engine ready • no model loaded"
                    is InferenceEngine.State.LoadingModel -> "Loading GGUF model"
                    is InferenceEngine.State.UnloadingModel -> "Unloading model"
                    is InferenceEngine.State.ModelReady -> "Model ready • offline"
                    is InferenceEngine.State.ProcessingSystemPrompt -> "Preparing assistant"
                    is InferenceEngine.State.ProcessingUserPrompt -> "Processing prompt"
                    is InferenceEngine.State.Generating -> "Generating locally"
                    is InferenceEngine.State.Benchmarking -> "Benchmarking"
                    is InferenceEngine.State.Error -> "Inference error: ${s.exception.message ?: "Unknown error"}"
                }
            }
        }
        initializeBundledModel()
    }

    /**
     * On fresh installs, extract our bundled GGUF to private storage and verify
     * it against the model SHA-256 produced by the build pipeline. Never
     * overwrite a model the user has already imported.
     *
     * This uses a background dispatcher and does not require network access.
     */
    private fun initializeBundledModel() {
        if (modelFile.isFile && modelSize > 0L) {
            operationStatus = "Previously imported model ready to load."
            loadModel()
            return
        }
        scope.launch {
            busy = true
            var installed = false
            val temporary = File(modelFile.parentFile, "bundled.gguf.part")
            operationStatus = "Preparing bundled Qwen3 model on this device..."
            try {
                val details = withContext(Dispatchers.IO) {
                    modelFile.parentFile?.mkdirs()
                    val manifest = context.assets.open("forge-default.sha256").bufferedReader().use {
                        it.readText().trim().split(Regex("\\s+"))
                    }
                    require(manifest.size >= 2 && manifest[0].length == 64) {
                        "Missing bundled model checksum"
                    }
                    val expectedHash = manifest[0].lowercase()
                    val expectedBytes = manifest[1].toLong()
                    require(expectedBytes > 1_000_000L) { "Invalid bundled model size" }

                    val freeBytes = StatFs(modelFile.parentFile!!.absolutePath).availableBytes
                    require(freeBytes > expectedBytes + 96L * 1024 * 1024) {
                        "Not enough free storage to unpack the bundled model"
                    }

                    val digest = MessageDigest.getInstance("SHA-256")
                    var count = 0L
                    context.assets.open("forge-default.gguf", android.content.res.AssetManager.ACCESS_STREAMING).use { input ->
                        FileOutputStream(temporary).use { output ->
                            val buffer = ByteArray(256 * 1024)
                            while (true) {
                                val n = input.read(buffer)
                                if (n < 0) break
                                count += n
                                require(count <= maxModelBytes && count <= expectedBytes) {
                                    "Bundled model size mismatch"
                                }
                                output.write(buffer, 0, n)
                                digest.update(buffer, 0, n)
                                if (count % (16L * 1024 * 1024) < buffer.size) {
                                    val pct = (count * 100L / expectedBytes).toInt()
                                    withContext(Dispatchers.Main) {
                                        operationStatus = "Preparing offline model: ${pct}%"
                                    }
                                }
                            }
                            output.fd.sync()
                        }
                    }
                    val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
                    require(count == expectedBytes && actualHash == expectedHash) {
                        "Bundled model failed checksum verification"
                    }
                    FileInputStream(temporary).use { input ->
                        val signature = ByteArray(4)
                        require(input.read(signature) == 4 &&
                            String(signature, Charsets.US_ASCII) == "GGUF") {
                            "Bundled model has an invalid GGUF header"
                        }
                    }
                    require(temporary.renameTo(modelFile)) {
                        "Unable to install bundled model"
                    }
                    Pair(count, actualHash)
                }
                modelSize = details.first
                modelHash = details.second
                modelName = "Qwen3-0.6B Q4_K_M (built in)"
                preferences.edit()
                    .putString("name", modelName).putString("sha256", modelHash)
                    .putLong("bytes", modelSize).apply()
                installed = true
                operationStatus = "Built-in model verified. Loading into RAM..."
            } catch (e: Exception) {
                operationStatus = "Bundled setup failed: ${e.message}. You can still import a GGUF manually."
            } finally {
                temporary.delete()
                busy = false
            }
            if (installed) loadModel()
        }
    }

    fun importModel(uri: Uri) {
        if (busy || generating || modelReady) {
            operationStatus = "Unload your model before importing another."
            return
        }
        scope.launch {
            busy = true
            operationStatus = "Copying GGUF into private storage..."
            val temporary = File(modelFile.parentFile, "incoming.gguf.part")
            try {
                val result = withContext(Dispatchers.IO) {
                    modelFile.parentFile?.mkdirs()
                    val digest = MessageDigest.getInstance("SHA-256")
                    var count = 0L
                    context.contentResolver.openInputStream(uri).use { input ->
                        requireNotNull(input) { "Cannot open selected document" }
                        FileOutputStream(temporary).use { output ->
                            val buffer = ByteArray(256 * 1024)
                            while (true) {
                                val n = input.read(buffer)
                                if (n < 0) break
                                count += n
                                require(count <= maxModelBytes) { "Model exceeds 3 GB import limit" }
                                output.write(buffer, 0, n)
                                digest.update(buffer, 0, n)
                            }
                            output.fd.sync()
                        }
                    }
                    require(count >= 64L) { "File is too small to be a GGUF model" }
                    FileInputStream(temporary).use { input ->
                        val signature = ByteArray(4)
                        require(input.read(signature) == 4 &&
                            String(signature, Charsets.US_ASCII) == "GGUF") {
                            "Selected file is not GGUF"
                        }
                    }
                    val hash = digest.digest().joinToString("") { "%02x".format(it) }
                    require(temporary.renameTo(modelFile)) {
                        "Unable to move imported model into private storage"
                    }
                    Pair(count, hash)
                }
                modelSize = result.first
                modelHash = result.second
                modelName = context.contentResolver.query(
                    uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                } ?: "Imported GGUF"
                preferences.edit()
                    .putString("name", modelName).putString("sha256", modelHash)
                    .putLong("bytes", modelSize).apply()
                operationStatus = "Model imported and SHA-256 verified. Tap Load."
            } catch (e: Exception) {
                temporary.delete()
                operationStatus = "Import failed: ${e.message ?: "Unknown error"}"
            } finally {
                busy = false
            }
        }
    }

    fun loadModel() {
        if (busy || generating || modelReady) return
        if (!imported) {
            operationStatus = "Import a GGUF model first."
            return
        }
        scope.launch {
            busy = true
            operationStatus = "Loading model into RAM; first load can take a while..."
            try {
                engine.state.first {
                    it is InferenceEngine.State.Initialized || it is InferenceEngine.State.Error
                }.let {
                    if (it is InferenceEngine.State.Error) throw it.exception
                }
                engine.loadModel(modelFile.absolutePath)
                engine.setSystemPrompt(
                    "You are Forge, a practical personal Android assistant running locally. " +
                    "You do not have internet, device-control tools or persistent memory in this version. " +
                    "Be helpful, concise and transparent about limitations. /no_think"
                )
                operationStatus = "Ready: offline chat is available."
            } catch (e: Exception) {
                operationStatus = "Load failed: ${e.message ?: "Unsupported GGUF or low memory"}"
            } finally {
                busy = false
            }
        }
    }

    fun unloadModel() {
        if (busy || generating || !modelReady) return
        scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) { engine.cleanUp() }
                operationStatus = "Model unloaded from RAM; imported file remains on this device."
            } catch (e: Exception) {
                operationStatus = "Unload failed: ${e.message}"
            } finally {
                busy = false
            }
        }
    }

    fun send(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || busy || generating || !modelReady) return
        messages.add(ChatLine("You", prompt))
        messages.add(ChatLine("Forge", ""))
        val responseIndex = messages.lastIndex
        generation = scope.launch {
            generating = true
            val start = SystemClock.elapsedRealtime()
            try {
                engine.sendUserPrompt(prompt, predictLength = 256).collect { piece ->
                    val old = messages[responseIndex]
                    messages[responseIndex] = old.copy(message = old.message + piece)
                }
                operationStatus = "Response completed locally."
            } catch (e: CancellationException) {
                operationStatus = "Generation stopped."
            } catch (e: Exception) {
                operationStatus = "Generation error: ${e.message}"
                if (messages[responseIndex].message.isEmpty()) {
                    messages[responseIndex] = ChatLine("Forge", "[Generation failed: ${e.message}]")
                }
            } finally {
                lastGenerationSeconds = (SystemClock.elapsedRealtime() - start) / 1000.0
                lastResponseCharacters = messages[responseIndex].message.length
                generating = false
            }
        }
    }

    fun stopGeneration() {
        generation?.cancel()
    }
}
