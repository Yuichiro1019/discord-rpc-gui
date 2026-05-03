package com.discordrpc.gui.rpc

import com.discordrpc.gui.state.AppState
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.util.UUID

/**
 * Connects to a local arRPC-compatible WebSocket (Vesktop built-in, or standalone).
 *
 * Strategy:
 * 1. First, try connecting directly to an existing arRPC on ports 6463-6472
 *    (covers Vesktop's built-in arRPC)
 * 2. If no existing arRPC found, spawn `npx arrpc` and connect to it
 */
class LocalRpcServiceImpl : DiscordRpcService {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var arrpcProcess: Process? = null
    private var client: HttpClient? = null
    private var session: DefaultClientWebSocketSession? = null
    private var receiveJob: Job? = null
    private var processMonitorJob: Job? = null
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var _isConnected = false
    override val isConnected: Boolean get() = _isConnected

    override suspend fun connect(credential: String): Boolean {
        disconnect()

        return withContext(Dispatchers.IO) {
            // Strategy 1: Try connecting to existing arRPC (Vesktop, standalone, etc.)
            println("[LocalRPC] Trying to connect to existing arRPC...")
            for (port in 6463..6472) {
                try {
                    if (tryConnectToPort(port, credential)) {
                        println("[LocalRPC] Connected to existing arRPC on port $port")
                        return@withContext true
                    }
                } catch (t: Throwable) {
                    println("[LocalRPC] Port $port: ${t.message}")
                }
            }

            // Strategy 2: Spawn our own arRPC
            println("[LocalRPC] No existing arRPC found, spawning...")
            try {
                val port = startArRpcProcess()
                if (port != null && tryConnectToPort(port, credential)) {
                    println("[LocalRPC] Connected to spawned arRPC on port $port")
                    return@withContext true
                }
            } catch (t: Throwable) {
                println("[LocalRPC] Spawn failed: ${t.message}")
            }

            println("[LocalRPC] All connection attempts failed")
            false
        }
    }

    private suspend fun tryConnectToPort(port: Int, clientId: String): Boolean {
        val httpClient = HttpClient(CIO) {
            install(WebSockets)
            engine {
                requestTimeout = 3000
            }
        }

        return try {
            val url = "ws://127.0.0.1:$port/?v=1&encoding=json&client_id=$clientId"
            val ws = httpClient.webSocketSession(url)

            val readyDeferred = CompletableDeferred<Boolean>()

            val job = scope.launch {
                try {
                    for (frame in ws.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val payload = json.parseToJsonElement(text).jsonObject
                                val cmd = payload["cmd"]?.jsonPrimitive?.contentOrNull
                                val evt = payload["evt"]?.jsonPrimitive?.contentOrNull

                                when {
                                    cmd == "DISPATCH" && evt == "READY" -> {
                                        _isConnected = true
                                        if (!readyDeferred.isCompleted) readyDeferred.complete(true)
                                        println("[LocalRPC] READY on port $port")
                                    }
                                    evt == "ERROR" -> {
                                        println("[LocalRPC] ERROR on port $port: $text")
                                        if (!readyDeferred.isCompleted) readyDeferred.complete(false)
                                    }
                                    cmd == "SET_ACTIVITY" -> {
                                        println("[LocalRPC] SET_ACTIVITY ack'd")
                                    }
                                }
                            } catch (_: Throwable) { }
                        }
                    }
                } catch (t: Throwable) {
                    if (_isConnected) println("[LocalRPC] Lost connection: ${t.message}")
                } finally {
                    _isConnected = false
                }
            }

            val ready = withTimeoutOrNull(3000) { readyDeferred.await() } ?: false

            if (ready) {
                // Success — store references
                receiveJob = job
                client = httpClient
                session = ws
                true
            } else {
                // Failed — cleanup
                job.cancel()
                runCatching { ws.close() }
                httpClient.close()
                false
            }
        } catch (t: Throwable) {
            httpClient.close()
            false
        }
    }

    private suspend fun startArRpcProcess(): Int? {
        val npxPath = findExecutable("npx") ?: run {
            println("[LocalRPC] npx not found — install Node.js")
            return null
        }

        val pb = ProcessBuilder(npxPath, "-y", "arrpc")
            .redirectErrorStream(true)

        val process = pb.start()
        arrpcProcess = process

        val reader = process.inputStream.bufferedReader()
        var discoveredPort: Int? = null

        withTimeoutOrNull(30_000) {
            while (isActive && process.isAlive) {
                val line = withContext(Dispatchers.IO) {
                    reader.readLineWithTimeout(5000)
                } ?: continue

                println("[arRPC] $line")

                if (line.contains("websocket") && line.contains("listening on")) {
                    discoveredPort = line.substringAfterLast("listening on").trim().toIntOrNull()
                }

                if (line.contains("process") && line.contains("started")) {
                    if (discoveredPort != null) return@withTimeoutOrNull
                }
            }
        }

        // Background log reader
        processMonitorJob = scope.launch {
            runCatching {
                while (isActive && process.isAlive) {
                    val line = withContext(Dispatchers.IO) { reader.readLineWithTimeout(1000) }
                    if (line != null) println("[arRPC] $line")
                }
            }
        }

        return discoveredPort
    }

    override suspend fun disconnect() {
        _isConnected = false
        receiveJob?.cancel()
        processMonitorJob?.cancel()
        receiveJob = null
        processMonitorJob = null

        runCatching { runBlocking { session?.close() } }
        runCatching { client?.close() }
        session = null
        client = null

        arrpcProcess?.let { proc ->
            runCatching {
                proc.descendants().forEach { it.destroyForcibly() }
                proc.destroyForcibly()
                proc.waitFor()
            }
        }
        arrpcProcess = null
    }

    override suspend fun updatePresence(state: AppState) {
        val ws = session ?: return
        if (!_isConnected || !ws.isActive) return

        val activityName = state.activityName.ifBlank {
            state.selectedProcess?.displayName ?: "Custom Status"
        }

        val activity = buildJsonObject {
            put("name", activityName)
            put("type", state.activityType)
            if (state.details.isNotBlank()) put("details", state.details)
            if (state.state.isNotBlank()) put("state", state.state)
            if (state.applicationId.isNotBlank()) put("application_id", state.applicationId)

            val startTs = state.startTimestamp
            val endTs = state.endTimestamp
            if (startTs != null || endTs != null) {
                put("timestamps", buildJsonObject {
                    startTs?.let { put("start", it) }
                    endTs?.let { put("end", it) }
                })
            }

            if (state.largeImageKey.isNotBlank() || state.smallImageKey.isNotBlank()) {
                put("assets", buildJsonObject {
                    if (state.largeImageKey.isNotBlank()) put("large_image", state.largeImageKey)
                    if (state.largeImageText.isNotBlank()) put("large_text", state.largeImageText)
                    if (state.smallImageKey.isNotBlank()) put("small_image", state.smallImageKey)
                    if (state.smallImageText.isNotBlank()) put("small_text", state.smallImageText)
                })
            }
        }

        val payload = buildJsonObject {
            put("cmd", "SET_ACTIVITY")
            put("args", buildJsonObject {
                put("activity", activity)
                put("pid", ProcessHandle.current().pid())
            })
            put("nonce", UUID.randomUUID().toString())
        }

        ws.send(Frame.Text(json.encodeToString(payload)))
        println("[LocalRPC] Sent SET_ACTIVITY: $activityName")
    }

    private fun findExecutable(name: String): String? {
        val paths = System.getenv("PATH")?.split(":") ?: return null
        for (dir in paths) {
            val file = java.io.File(dir, name)
            if (file.exists() && file.canExecute()) return file.absolutePath
        }
        return null
    }

    private fun BufferedReader.readLineWithTimeout(timeoutMs: Long): String? {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            if (this.ready()) return this.readLine()
            Thread.sleep(50)
        }
        return null
    }
}
