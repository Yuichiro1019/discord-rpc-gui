package com.discordrpc.gui.rpc

import com.discordrpc.gui.rpc.gateway.DiscordGateway
import com.discordrpc.gui.rpc.gateway.GatewayActivity
import com.discordrpc.gui.rpc.gateway.GatewayAssets
import com.discordrpc.gui.rpc.gateway.GatewayTimestamps
import com.discordrpc.gui.state.AppState

/**
 * Gateway-based Discord RPC service.
 * Uses the Discord WebSocket Gateway with a user token to send presence updates.
 * This allows setting the activity `name` (title) dynamically — not possible with IPC.
 */
class GatewayRpcServiceImpl : DiscordRpcService {
    private var gateway: DiscordGateway? = null

    override val isConnected: Boolean
        get() = gateway?.isConnected == true

    override suspend fun connect(credential: String): Boolean {
        // Disconnect existing connection first
        disconnect()

        val gw = DiscordGateway(credential)
        gateway = gw
        return gw.connect()
    }

    override suspend fun disconnect() {
        gateway?.close()
        gateway = null
    }

    override suspend fun updatePresence(state: AppState) {
        val gw = gateway ?: return
        if (!gw.isConnected) return

        // Build the activity name: use the selected process display name, or "Custom RPC" as fallback
        val activityName = state.activityName.ifBlank {
            state.selectedProcess?.displayName ?: "Custom Status"
        }

        val activity = GatewayActivity(
            name = activityName,
            type = state.activityType,
            details = state.details.ifBlank { null },
            state = state.state.ifBlank { null },
            timestamps = buildTimestamps(state),
            assets = buildAssets(state),
            applicationId = state.applicationId.ifBlank { null },
            buttons = buildButtons(state),
            metadata = buildMetadata(state)
        )

        gw.sendPresence(activity)
    }

    private fun buildButtons(state: AppState): List<String>? {
        if (!state.buttonsEnabled) return null
        val labels = mutableListOf<String>()
        if (state.button1Label.isNotBlank() && state.button1Url.isNotBlank()) labels.add(state.button1Label)
        if (state.button2Label.isNotBlank() && state.button2Url.isNotBlank()) labels.add(state.button2Label)
        return labels.ifEmpty { null }
    }

    private fun buildMetadata(state: AppState): com.discordrpc.gui.rpc.gateway.GatewayMetadata? {
        if (!state.buttonsEnabled) return null
        val urls = mutableListOf<String>()
        if (state.button1Label.isNotBlank() && state.button1Url.isNotBlank()) urls.add(state.button1Url)
        if (state.button2Label.isNotBlank() && state.button2Url.isNotBlank()) urls.add(state.button2Url)
        return if (urls.isNotEmpty()) com.discordrpc.gui.rpc.gateway.GatewayMetadata(buttonUrls = urls) else null
    }

    private fun buildTimestamps(state: AppState): GatewayTimestamps? {
        if (!state.timestampsEnabled) return null
        val start = state.startTimestamp
        val end = state.endTimestamp
        return if (start != null || end != null) {
            GatewayTimestamps(start = start, end = end)
        } else null
    }

    private fun buildAssets(state: AppState): GatewayAssets? {
        val hasAny = state.largeImageKey.isNotBlank() || state.smallImageKey.isNotBlank()
        return if (hasAny) {
            GatewayAssets(
                largeImage = state.largeImageKey.ifBlank { null },
                largeText = state.largeImageText.ifBlank { null },
                smallImage = state.smallImageKey.ifBlank { null },
                smallText = state.smallImageText.ifBlank { null }
            )
        } else null
    }
}
