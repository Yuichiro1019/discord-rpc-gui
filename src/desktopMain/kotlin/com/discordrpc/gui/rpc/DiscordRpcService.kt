package com.discordrpc.gui.rpc

import com.discordrpc.gui.state.AppState

interface DiscordRpcService {
    suspend fun connect(clientId: String): Boolean
    suspend fun disconnect()
    suspend fun updatePresence(state: AppState)
}
