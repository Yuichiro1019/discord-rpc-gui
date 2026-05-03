package com.discordrpc.gui.rpc.gateway

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/**
 * Manages a WebSocket connection to the Discord Gateway.
 * Handles the full lifecycle: Hello → Identify → Heartbeat → Presence Update.
 */
class DiscordGateway(
    private val token: String
) {
    private val gatewayUrl = "wss://gateway.discord.gg/?v=10&encoding=json"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private var client: HttpClient? = null
    private var session: DefaultClientWebSocketSession? = null
    private var heartbeatJob: Job? = null
    private var receiveJob: Job? = null
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var sequence = 0
    private var sessionId: String? = null
    private var resumeGatewayUrl: String? = null

    @Volatile
    var isConnected = false
        private set

    private val readyDeferred = CompletableDeferred<Boolean>()

    /**
     * Connect to the Discord Gateway and wait for READY event.
     * Returns true if successfully authenticated.
     */
    suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                client = HttpClient(CIO) { install(WebSockets) }
                session = client!!.webSocketSession(gatewayUrl)

                // Start receiving in background
                receiveJob = scope.launch {
                    try {
                        session!!.incoming.receiveAsFlow().collect { frame ->
                            if (frame is Frame.Text) {
                                val payload = json.decodeFromString<GatewayPayload>(frame.readText())
                                handleMessage(payload)
                            }
                        }
                    } catch (t: Throwable) {
                        if (isConnected) {
                            println("[Gateway] Connection lost: ${t.message}")
                        }
                    } finally {
                        isConnected = false
                    }
                }

                // Wait for READY (max 15 seconds)
                withTimeoutOrNull(15_000) { readyDeferred.await() } ?: false
            } catch (t: Throwable) {
                println("[Gateway] Connect failed: ${t.message}")
                false
            }
        }
    }

    /**
     * Send a presence update with the given activity.
     */
    suspend fun sendPresence(activity: GatewayActivity, status: String = "online") {
        val presence = PresenceUpdate(
            activities = listOf(activity),
            status = status,
            since = System.currentTimeMillis()
        )
        send(3, json.encodeToJsonElement(presence))
    }

    /**
     * Close the Gateway connection cleanly.
     */
    fun close() {
        heartbeatJob?.cancel()
        receiveJob?.cancel()
        heartbeatJob = null
        receiveJob = null
        isConnected = false
        sessionId = null
        resumeGatewayUrl = null
        sequence = 0
        runCatching {
            runBlocking { session?.close() }
            client?.close()
        }
        session = null
        client = null
    }

    // ── Internal message handling ──

    private suspend fun handleMessage(payload: GatewayPayload) {
        payload.s?.let { sequence = it }

        when (payload.op) {
            0 -> handleDispatch(payload)    // DISPATCH
            1 -> sendHeartbeat()             // HEARTBEAT request
            7 -> reconnect()                 // RECONNECT
            9 -> handleInvalidSession()      // INVALID_SESSION
            10 -> handleHello(payload)       // HELLO
            11 -> { /* HEARTBEAT_ACK */ }
        }
    }

    private suspend fun handleHello(payload: GatewayPayload) {
        val hello = json.decodeFromJsonElement<Hello>(payload.d!!)

        // Send Identify
        val identify = Identify(
            token = token,
            properties = Identify.Properties(
                os = System.getProperty("os.name") ?: "linux",
                browser = "DiscordRPCGUI",
                device = "DiscordRPCGUI"
            )
        )
        send(2, json.encodeToJsonElement(identify))

        // Start heartbeat loop
        startHeartbeat(hello.heartbeatInterval)
    }

    private fun handleDispatch(payload: GatewayPayload) {
        when (payload.t) {
            "READY" -> {
                val ready = json.decodeFromJsonElement<Ready>(payload.d!!)
                sessionId = ready.sessionId
                resumeGatewayUrl = ready.resumeGatewayUrl
                isConnected = true
                readyDeferred.complete(true)
                println("[Gateway] Connected! Session: $sessionId")
            }
            "RESUMED" -> {
                println("[Gateway] Session resumed")
            }
        }
    }

    private suspend fun handleInvalidSession() {
        println("[Gateway] Invalid session, re-identifying...")
        delay(150)
        val identify = Identify(
            token = token,
            properties = Identify.Properties()
        )
        send(2, json.encodeToJsonElement(identify))
    }

    private suspend fun reconnect() {
        session?.close(CloseReason(4000, "Reconnecting"))
    }

    private suspend fun sendHeartbeat() {
        val d = if (sequence == 0) JsonNull else JsonPrimitive(sequence)
        send(1, d)
    }

    private fun startHeartbeat(intervalMs: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                sendHeartbeat()
                delay(intervalMs)
            }
        }
    }

    private suspend fun send(op: Int, d: JsonElement?) {
        val ws = session ?: return
        if (!ws.isActive) return
        val payload = json.encodeToString(GatewayPayload(op = op, d = d))
        ws.send(Frame.Text(payload))
    }
}
