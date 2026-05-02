package com.discordrpc.gui.state

data class AppState(
    val isConnected: Boolean = false,
    val clientId: String = "922036277996556318", // A default client ID for testing
    val details: String = "Playing a Game",
    val state: String = "In a Match",
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
}
