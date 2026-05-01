package com.discordrpc.gui.model

/**
 * Represents information about a running process.
 * Used for identifying applications to associate with RPC profiles.
 *
 * @param name The display name of the process
 * @param executable The executable name or path
 * @param pid The process ID
 * @param iconPath Optional path to the process icon (nullable)
 */
data class ProcessInfo(
    val name: String,
    val executable: String,
    val pid: Long,
    val iconPath: String? = null
)
