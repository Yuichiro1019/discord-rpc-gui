package com.discordrpc.gui.process

import java.io.File

/**
 * Cross-platform process detector.
 * Use [create] to get the right implementation for the current OS.
 */
interface ProcessDetector {
    /**
     * Returns a deduplicated list of running USER-FACING processes (GUI apps).
     * System/kernel/daemon processes are filtered out.
     * For processes with the same name, only the one using the most memory is returned.
     */
    fun listProcesses(): List<ProcessInfo>

    companion object {
        fun create(): ProcessDetector {
            val os = System.getProperty("os.name").lowercase()
            return when {
                os.contains("linux") -> LinuxProcessDetector()
                os.contains("windows") -> WindowsProcessDetector()
                else -> LinuxProcessDetector()
            }
        }
    }
}

// ── Linux Implementation ──

class LinuxProcessDetector : ProcessDetector {

    // .desktop file cache: maps executable name -> (Name, Icon path)
    private val desktopEntries: Map<String, DesktopEntry> by lazy { loadDesktopEntries() }

    override fun listProcesses(): List<ProcessInfo> {
        val procDir = File("/proc")
        if (!procDir.exists()) return emptyList()

        val allProcesses = procDir.listFiles()
            ?.filter { it.isDirectory && it.name.all { c -> c.isDigit() } }
            ?.mapNotNull { pidDir -> readProcessInfo(pidDir) }
            ?: emptyList()

        // Deduplicate: group by name, keep the one with the highest memory
        return allProcesses
            .filter { it.memoryKb > 1024 } // at least 1MB — skip tiny helpers
            .groupBy { it.name }
            .map { (_, processes) -> processes.maxByOrNull { it.memoryKb }!! }
            .sortedBy { it.displayName.lowercase() }
    }

    private fun readProcessInfo(pidDir: File): ProcessInfo? {
        return try {
            val pid = pidDir.name.toLong()
            val statusFile = File(pidDir, "status")
            if (!statusFile.exists()) return null

            val statusLines = statusFile.readLines()
            val name = statusLines
                .firstOrNull { it.startsWith("Name:") }
                ?.substringAfter("Name:")?.trim()
                ?: return null

            // Skip kernel threads and known system processes
            if (name in SYSTEM_PROCESS_BLACKLIST) return null

            // Check if this is a GUI process by looking for DISPLAY/WAYLAND in its environment
            if (!isGuiProcess(pidDir)) return null

            val memoryKb = statusLines
                .firstOrNull { it.startsWith("VmRSS:") }
                ?.substringAfter("VmRSS:")?.trim()
                ?.split("\\s+".toRegex())?.firstOrNull()
                ?.toLongOrNull() ?: 0L

            val exePath = try {
                File(pidDir, "exe").canonicalPath
            } catch (_: Throwable) {
                null
            }

            // Look up desktop entry for human-readable name and icon
            val desktop = findDesktopEntry(name, exePath)

            ProcessInfo(
                pid = pid,
                name = name,
                executablePath = exePath,
                memoryKb = memoryKb,
                desktopName = desktop?.name,
                iconPath = desktop?.iconPath
            )
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Check if a process is a GUI app by reading its environment for DISPLAY or WAYLAND_DISPLAY.
     */
    private fun isGuiProcess(pidDir: File): Boolean {
        return try {
            val envFile = File(pidDir, "environ")
            if (!envFile.canRead()) return false
            // environ is null-byte separated
            val envContent = envFile.readBytes().toString(Charsets.UTF_8)
            envContent.contains("DISPLAY=") || envContent.contains("WAYLAND_DISPLAY=")
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Find the matching .desktop entry for a process name or executable path.
     */
    private fun findDesktopEntry(processName: String, exePath: String?): DesktopEntry? {
        // Direct match by process name
        desktopEntries[processName.lowercase()]?.let { return it }

        // Try matching by executable basename from the .desktop Exec= field
        if (exePath != null) {
            val exeBasename = File(exePath).name.lowercase()
            desktopEntries[exeBasename]?.let { return it }
        }

        return null
    }

    /**
     * Parse all .desktop files from standard locations to build a lookup table.
     */
    private fun loadDesktopEntries(): Map<String, DesktopEntry> {
        val dirs = listOf(
            "/usr/share/applications",
            "/usr/local/share/applications",
            "${System.getProperty("user.home")}/.local/share/applications",
            "/var/lib/flatpak/exports/share/applications",
            "${System.getProperty("user.home")}/.local/share/flatpak/exports/share/applications"
        )

        val entries = mutableMapOf<String, DesktopEntry>()

        for (dir in dirs) {
            val folder = File(dir)
            if (!folder.exists()) continue

            folder.listFiles()
                ?.filter { it.extension == "desktop" }
                ?.forEach { file ->
                    try {
                        val lines = file.readLines()
                        val name = lines.firstOrNull { it.startsWith("Name=") }
                            ?.substringAfter("Name=")?.trim()
                        val exec = lines.firstOrNull { it.startsWith("Exec=") }
                            ?.substringAfter("Exec=")?.trim()
                            ?.split(" ")?.firstOrNull() // take just the command, not args
                        val iconName = lines.firstOrNull { it.startsWith("Icon=") }
                            ?.substringAfter("Icon=")?.trim()
                        val noDisplay = lines.any { it.trim() == "NoDisplay=true" }

                        if (name != null && exec != null && !noDisplay) {
                            val execBasename = File(exec).name.lowercase()
                            val iconPath = resolveIconPath(iconName)
                            val entry = DesktopEntry(name, iconPath)
                            entries[execBasename] = entry
                        }
                    } catch (_: Throwable) {
                        // skip unparseable desktop files
                    }
                }
        }

        return entries
    }

    /**
     * Resolve an icon name to an absolute file path.
     * Handles both absolute paths and theme icon names.
     */
    private fun resolveIconPath(iconName: String?): String? {
        if (iconName == null) return null

        // Already an absolute path
        if (iconName.startsWith("/") && File(iconName).exists()) return iconName

        // Search common icon locations for the theme icon name
        val searchDirs = listOf(
            "/usr/share/icons/hicolor/128x128/apps",
            "/usr/share/icons/hicolor/96x96/apps",
            "/usr/share/icons/hicolor/64x64/apps",
            "/usr/share/icons/hicolor/48x48/apps",
            "/usr/share/icons/hicolor/scalable/apps",
            "/usr/share/pixmaps"
        )
        val extensions = listOf("", ".png", ".svg", ".xpm")

        for (dir in searchDirs) {
            for (ext in extensions) {
                val candidate = File(dir, "$iconName$ext")
                if (candidate.exists()) return candidate.absolutePath
            }
        }

        return null
    }

    private data class DesktopEntry(val name: String, val iconPath: String?)

    companion object {
        /** Known system/daemon processes to always filter out */
        private val SYSTEM_PROCESS_BLACKLIST = setOf(
            "kthread", "init", "systemd", "dbus-daemon", "dbus-broker",
            "polkitd", "udisksd", "accounts-daemon", "at-spi2-registryd",
            "at-spi-bus-launcher", "gvfsd", "gvfsd-fuse", "gvfsd-metadata",
            "xdg-dbus-proxy", "xdg-document-portal", "xdg-permission-store",
            "xdg-desktop-portal", "xdg-desktop-portal-gtk", "xdg-desktop-portal-gnome",
            "xdg-desktop-portal-kde", "xdg-desktop-portal-hyprland",
            "pipewire", "pipewire-pulse", "wireplumber",
            "pulseaudio", "gdm", "sddm", "lightdm",
            "gnome-shell", "kwin_wayland", "kwin_x11", "mutter",
            "Xorg", "Xwayland", "gnome-session-binary",
            "ibus-daemon", "ibus-engine-simple", "ibus-extension-gtk3",
            "gsd-color", "gsd-datetime", "gsd-housekeeping", "gsd-keyboard",
            "gsd-media-keys", "gsd-power", "gsd-print-notifications",
            "gsd-rfkill", "gsd-screensaver-proxy", "gsd-sharing",
            "gsd-smartcard", "gsd-sound", "gsd-usb-protection",
            "gsd-wacom", "gsd-xsettings",
            "gnome-keyring-daemon", "ssh-agent", "gpg-agent",
            "evolution-data-server", "evolution-calendar-factory",
            "evolution-addressbook-factory", "evolution-source-registry",
            "tracker-miner-fs", "tracker-store",
            "gjs", "goa-daemon", "goa-identity-service",
            "packagekitd", "fwupd", "thermald", "upowerd",
            "NetworkManager", "wpa_supplicant", "iwd",
            "cupsd", "colord", "rtkit-daemon",
            "boltd", "power-profiles-daemon", "switcheroo-control",
            "low-memory-monitor", "geoclue", "avahi-daemon"
        )
    }
}

// ── Windows Implementation ──

class WindowsProcessDetector : ProcessDetector {
    override fun listProcesses(): List<ProcessInfo> {
        return try {
            // Use tasklist /V to get window titles — processes WITH a window title are GUI apps
            val process = ProcessBuilder("tasklist", "/FO", "CSV", "/V", "/NH")
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            val allProcesses = output.lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        val parts = parseCsvLine(line)
                        if (parts.size < 9) return@mapNotNull null

                        val rawName = parts[0].removeSuffix(".exe")
                        val pid = parts[1].toLongOrNull() ?: return@mapNotNull null
                        val memoryStr = parts[4]
                            .replace(",", "").replace(".", "")
                            .replace(" K", "").replace(" k", "").trim()
                        val memoryKb = memoryStr.toLongOrNull() ?: 0L
                        val windowTitle = parts[8].trim()

                        // Filter: only processes with a window title (GUI apps)
                        if (windowTitle == "N/A" || windowTitle.isBlank()) return@mapNotNull null
                        if (rawName.lowercase() in WINDOWS_SYSTEM_BLACKLIST) return@mapNotNull null

                        ProcessInfo(
                            pid = pid,
                            name = rawName,
                            executablePath = null,
                            memoryKb = memoryKb,
                            desktopName = windowTitle // use window title as display name
                        )
                    } catch (_: Throwable) {
                        null
                    }
                }

            allProcesses
                .filter { it.memoryKb > 1024 }
                .groupBy { it.name.lowercase() }
                .map { (_, processes) -> processes.maxByOrNull { it.memoryKb }!! }
                .sortedBy { it.displayName.lowercase() }
        } catch (t: Throwable) {
            t.printStackTrace()
            emptyList()
        }
    }

    /** Simple CSV line parser that handles quoted fields */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    companion object {
        private val WINDOWS_SYSTEM_BLACKLIST = setOf(
            "csrss", "smss", "wininit", "services", "lsass", "svchost",
            "dwm", "sihost", "taskhostw", "explorer", "runtimebroker",
            "searchhost", "startmenuexperiencehost", "shellexperiencehost",
            "textinputhost", "ctfmon", "fontdrvhost", "dllhost",
            "conhost", "audiodg", "securityhealthservice",
            "searchindexer", "searchprotocolhost", "spoolsv",
            "wudfhost", "dashost", "smartscreen"
        )
    }
}
