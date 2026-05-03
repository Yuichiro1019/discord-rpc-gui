package com.discordrpc.gui.rpc

import com.discordrpc.gui.state.AppState

/**
 * Abstraction for Discord RPC connectivity.
 * Implementations:
 * - [GatewayRpcServiceImpl]: Direct Gateway (needs user token, dynamic names)
 * - [LocalRpcServiceImpl]: Local arRPC/Discord WebSocket (no token, dynamic names if arRPC)
 */
interface DiscordRpcService {
    /** Connect to Discord. Parameter meaning depends on implementation. */
    suspend fun connect(credential: String): Boolean

    /** Disconnect from Discord */
    suspend fun disconnect()

    /** Update the Rich Presence activity from current app state */
    suspend fun updatePresence(state: AppState)

    /** Whether currently connected */
    val isConnected: Boolean
}

/** Available connection modes */
enum class RpcMode(val label: String, val description: String) {
    LOCAL(
        "Local (arRPC)",
        "Connects to local arRPC/Discord WebSocket — no token needed"
    ),
    GATEWAY(
        "Gateway (Token)",
        "Connects directly to Discord Gateway — needs user token, works standalone"
    )
}
