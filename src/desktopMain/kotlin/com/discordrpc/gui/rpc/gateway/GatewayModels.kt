package com.discordrpc.gui.rpc.gateway

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Gateway Payload (sent/received on WebSocket) ──

@Serializable
data class GatewayPayload(
    @SerialName("op") val op: Int,
    @SerialName("d") val d: JsonElement? = null,
    @SerialName("s") val s: Int? = null,
    @SerialName("t") val t: String? = null
)

// ── Hello (opcode 10) ──

@Serializable
data class Hello(
    @SerialName("heartbeat_interval") val heartbeatInterval: Long
)

// ── Ready (opcode 0, event READY) ──

@Serializable
data class Ready(
    @SerialName("session_id") val sessionId: String,
    @SerialName("resume_gateway_url") val resumeGatewayUrl: String? = null
)

// ── Identify (opcode 2) ──

@Serializable
data class Identify(
    @SerialName("token") val token: String,
    @SerialName("properties") val properties: Properties,
    @SerialName("compress") val compress: Boolean = false,
    @SerialName("intents") val intents: Int = 0
) {
    @Serializable
    data class Properties(
        @SerialName("os") val os: String = "linux",
        @SerialName("browser") val browser: String = "DiscordRPCGUI",
        @SerialName("device") val device: String = "DiscordRPCGUI"
    )
}

// ── Resume (opcode 6) ──

@Serializable
data class Resume(
    @SerialName("token") val token: String,
    @SerialName("session_id") val sessionId: String?,
    @SerialName("seq") val seq: Int
)

// ── Presence Update (opcode 3) ──

@Serializable
data class PresenceUpdate(
    @SerialName("activities") val activities: List<GatewayActivity>,
    @SerialName("afk") val afk: Boolean = false,
    @SerialName("since") val since: Long = 0,
    @SerialName("status") val status: String = "online"
)

@Serializable
data class GatewayActivity(
    @SerialName("name") val name: String,
    @SerialName("type") val type: Int = 0, // 0=Playing, 1=Streaming, 2=Listening, 3=Watching, 5=Competing
    @SerialName("state") val state: String? = null,
    @SerialName("details") val details: String? = null,
    @SerialName("timestamps") val timestamps: GatewayTimestamps? = null,
    @SerialName("assets") val assets: GatewayAssets? = null,
    @SerialName("application_id") val applicationId: String? = null,
    @SerialName("buttons") val buttons: List<String>? = null,
    @SerialName("metadata") val metadata: GatewayMetadata? = null
)

@Serializable
data class GatewayTimestamps(
    @SerialName("start") val start: Long? = null,
    @SerialName("end") val end: Long? = null
)

@Serializable
data class GatewayAssets(
    @SerialName("large_image") val largeImage: String? = null,
    @SerialName("large_text") val largeText: String? = null,
    @SerialName("small_image") val smallImage: String? = null,
    @SerialName("small_text") val smallText: String? = null
)

@Serializable
data class GatewayMetadata(
    @SerialName("button_urls") val buttonUrls: List<String>? = null
)
