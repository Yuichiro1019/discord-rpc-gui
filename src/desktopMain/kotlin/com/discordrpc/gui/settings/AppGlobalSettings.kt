package com.discordrpc.gui.settings

import kotlinx.serialization.Serializable

@Serializable
data class AppGlobalSettings(
    val themeId: String = "PlushAmber",
    val customThemeHex: String = ""
)
