package com.discordrpc.gui.process

import androidx.compose.ui.graphics.ImageBitmap

data class ProcessInfo(
    val pid: Long,
    val name: String,
    val executablePath: String?,
    val memoryKb: Long = 0,
    val desktopName: String? = null,  // human-readable name from .desktop file
    val iconPath: String? = null       // resolved icon file path
) {
    /** Display name: prefer .desktop Name, fall back to process name */
    val displayName: String get() = desktopName ?: name
}
