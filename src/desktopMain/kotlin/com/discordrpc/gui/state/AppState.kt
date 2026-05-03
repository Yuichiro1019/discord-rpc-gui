package com.discordrpc.gui.state

import com.discordrpc.gui.process.ProcessInfo
import com.discordrpc.gui.rpc.RpcMode
import com.discordrpc.gui.settings.AppRpcSettings

data class AppState(
    // Connection
    val rpcMode: RpcMode = RpcMode.LOCAL,  // default to local (arRPC) — no token needed
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val token: String = "",                // Discord user token (Gateway mode only)
    val statusMessage: String = "Disconnected",

    // Process selection
    val runningProcesses: List<ProcessInfo> = emptyList(),
    val selectedProcess: ProcessInfo? = null,
    val savedSettings: AppRpcSettings? = null,
    val isNewProcess: Boolean = false,
    val processSearchQuery: String = "",
    val isEditingSettings: Boolean = false,
    val isLoadingProcesses: Boolean = false,

    // Activity fields
    val activityName: String = "",         // the title shown on Discord (e.g. "Playing Heroic")
    val activityType: Int = 0,             // 0=Playing, 1=Streaming, 2=Listening, 3=Watching, 5=Competing
    val applicationId: String = "",        // optional — for rich presence assets from Dev Portal
    val details: String = "",
    val state: String = "",
    val largeImageKey: String = "",
    val largeImageText: String = "",
    val smallImageKey: String = "",
    val smallImageText: String = "",
    val startTimestampStr: String = "",
    val endTimestampStr: String = "",
    val partyId: String = "",
    val partySizeStr: String = "",
    val partyMaxStr: String = "",
    val matchSecret: String = "",
    val joinSecret: String = "",
    val spectateSecret: String = ""
) {
    val startTimestamp: Long? get() = startTimestampStr.toLongOrNull()
    val endTimestamp: Long? get() = endTimestampStr.toLongOrNull()
    val partySize: Int? get() = partySizeStr.toIntOrNull()
    val partyMax: Int? get() = partyMaxStr.toIntOrNull()

    val filteredProcesses: List<ProcessInfo>
        get() = if (processSearchQuery.isBlank()) {
            runningProcesses
        } else {
            runningProcesses.filter {
                it.name.contains(processSearchQuery, ignoreCase = true) ||
                (it.desktopName?.contains(processSearchQuery, ignoreCase = true) == true)
            }
        }

    companion object {
        val ACTIVITY_TYPES = listOf(
            0 to "Playing",
            1 to "Streaming",
            2 to "Listening",
            3 to "Watching",
            5 to "Competing"
        )
    }
}
