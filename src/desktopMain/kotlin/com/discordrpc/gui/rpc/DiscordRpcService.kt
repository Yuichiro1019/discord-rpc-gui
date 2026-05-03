package com.discordrpc.gui.rpc

import com.discordrpc.gui.state.AppState

/**
 * Abstraction for Discord RPC connectivity.
 * Implementations can use either local IPC or the Discord Gateway (WebSocket).
 */
interface DiscordRpcService {
    /**
     * Connect to Discord using the provided token.
     * @return true if connected successfully
     */
    suspend fun connect(token: String): Boolean

    /** Disconnect from Discord */
    suspend fun disconnect()

    /** Update the Rich Presence activity from current app state */
    suspend fun updatePresence(state: AppState)

    /** Whether currently connected */
    val isConnected: Boolean
}
