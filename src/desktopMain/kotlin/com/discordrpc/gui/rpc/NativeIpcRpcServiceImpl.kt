package com.discordrpc.gui.rpc

import com.discordrpc.gui.state.AppState
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File
import java.io.InputStream
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Connects to Discord's Native IPC using an external Python sidecar (`pypresence_sidecar.py`).
 *
 * It extracts the Python script from resources, spawns `python3`, and communicates
 * with it over stdin/stdout using JSON.
 */
class NativeIpcRpcServiceImpl : DiscordRpcService {
    private val json = Json { ignoreUnknownKeys = true }

    private var sidecarProcess: Process? = null
    private var stdinWriter: OutputStreamWriter? = null
    private var processMonitorJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var _isConnected = false
    override val isConnected: Boolean get() = _isConnected

    override suspend fun connect(credential: String): Boolean {
        disconnect()

        return withContext(Dispatchers.IO) {
            val executable = extractBinary() ?: return@withContext false

            val pb = ProcessBuilder(executable.absolutePath)
                .redirectErrorStream(true)

            try {
                val process = pb.start()
                sidecarProcess = process
                val writer = process.outputStream.writer(Charsets.UTF_8)
                stdinWriter = writer

                // Background loop to read sidecar responses/logs
                processMonitorJob = scope.launch {
                    process.inputStream.bufferedReader().use { reader ->
                        while (isActive && process.isAlive) {
                            val line = runCatching { reader.readLine() }.getOrNull() ?: break
                            println("[PySidecar] $line")
                            
                            // If the script crashes or reports pypresence is missing
                            if (line.contains("pypresence import failed")) {
                                println("[NativeIPC] CRITICAL: pypresence is not installed. Run 'pip install pypresence'")
                                _isConnected = false
                            }
                        }
                    }
                    _isConnected = false
                    println("[NativeIPC] Python sidecar exited.")
                }

                // Send connect command
                val clientId = credential.ifBlank { "835460649798467584" }
                val cmd = buildJsonObject {
                    put("cmd", "connect")
                    put("client_id", clientId)
                }

                writer.write(json.encodeToString(cmd) + "\n")
                writer.flush()

                // Wait briefly for connection (in reality we'd parse the stdout response synchronously, 
                // but since it's async we'll just assume connection succeeds and let the monitor catch errors)
                delay(1500)
                if (process.isAlive) {
                    _isConnected = true
                    true
                } else {
                    false
                }

            } catch (e: Exception) {
                println("[NativeIPC] Failed to start Python sidecar: ${e.message}")
                false
            }
        }
    }

    override suspend fun disconnect() {
        _isConnected = false
        
        runCatching {
            stdinWriter?.let { writer ->
                val cmd = buildJsonObject { put("cmd", "disconnect") }
                writer.write(json.encodeToString(cmd) + "\n")
                writer.flush()
                writer.close()
            }
        }
        stdinWriter = null

        processMonitorJob?.cancel()
        processMonitorJob = null

        sidecarProcess?.let { proc ->
            runCatching {
                proc.destroyForcibly()
                proc.waitFor()
            }
        }
        sidecarProcess = null
    }

    override suspend fun updatePresence(state: AppState) {
        val writer = stdinWriter ?: return
        if (!_isConnected) return

        val activityName = state.activityName.ifBlank {
            state.selectedProcess?.displayName ?: "Custom Status"
        }

        val cmd = buildJsonObject {
            put("cmd", "update")
            put("name", activityName)
            put("activity_type", state.activityType)
            if (state.details.isNotBlank()) put("details", state.details)
            if (state.state.isNotBlank()) put("state", state.state)
            
            if (state.timestampsEnabled) {
                state.startTimestamp?.let { put("start", it) }
                state.endTimestamp?.let { put("end", it) }
            }

            if (state.largeImageKey.isNotBlank()) put("large_image", state.largeImageKey)
            if (state.largeImageText.isNotBlank()) put("large_text", state.largeImageText)
            if (state.smallImageKey.isNotBlank()) put("small_image", state.smallImageKey)
            if (state.smallImageText.isNotBlank()) put("small_text", state.smallImageText)

            if (state.buttonsEnabled) {
                val btns = buildJsonArray {
                    if (state.button1Label.isNotBlank() && state.button1Url.isNotBlank()) {
                        add(buildJsonObject {
                            put("label", state.button1Label)
                            put("url", state.button1Url)
                        })
                    }
                    if (state.button2Label.isNotBlank() && state.button2Url.isNotBlank()) {
                        add(buildJsonObject {
                            put("label", state.button2Label)
                            put("url", state.button2Url)
                        })
                    }
                }
                if (btns.isNotEmpty()) put("buttons", btns)
            }
        }

        try {
            withContext(Dispatchers.IO) {
                writer.write(json.encodeToString(cmd) + "\n")
                writer.flush()
            }
            println("[NativeIPC] Sent update command to Python sidecar")
        } catch (e: Exception) {
            println("[NativeIPC] Failed to send update: ${e.message}")
            _isConnected = false
        }
    }

    private fun extractBinary(): File? {
        val osName = System.getProperty("os.name").lowercase()
        val isWindows = osName.contains("win")
        val isMac = osName.contains("mac")
        val binName = if (isWindows) "pypresence_sidecar.exe" else "pypresence_sidecar"
        val osDir = when {
            isWindows -> "windows"
            isMac -> "macos"
            else -> "linux"
        }

        // 1. Check local dev environment first
        val devBin = File("src/desktopMain/resources/bin/$osDir/$binName")
        if (devBin.isFile && devBin.canExecute()) return devBin

        // 2. Extract from JAR
        val resourcePath = "/bin/$osDir/$binName"
        val tmpFile = File.createTempFile("pypresence_sidecar", if (isWindows) ".exe" else "")
        tmpFile.deleteOnExit()
        
        val stream: InputStream? = javaClass.getResourceAsStream(resourcePath)
        if (stream == null) {
            println("[NativeIPC] Executable $resourcePath not found in resources!")
            return null
        }
        
        stream.use { input ->
            tmpFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        tmpFile.setExecutable(true)
        return tmpFile
    }
}
