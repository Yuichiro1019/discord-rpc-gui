package com.discordrpc.gui.settings

import kotlinx.serialization.Serializable

/**
 * RPC display settings saved for a specific process/app.
 * Each app gets its own Discord Application ID since the RPC title
 * is determined by the application name in the Discord Developer Portal.
 */
@Serializable
data class AppRpcSettings(
    val processName: String,
    val displayName: String = "",
    val clientId: String = "",      // per-app Discord Application ID
    val details: String = "",
    val state: String = "",
    val largeImageKey: String = "",
    val largeImageText: String = "",
    val smallImageKey: String = "",
    val smallImageText: String = ""
)
