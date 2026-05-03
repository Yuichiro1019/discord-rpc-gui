package com.discordrpc.gui.settings

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Handles loading and saving per-app RPC settings to a JSON file on disk.
 *
 * File location:
 * - Linux:   ~/.config/discord-rpc-gui/app-settings.json
 * - Windows: %APPDATA%/discord-rpc-gui/app-settings.json
 */
class SettingsRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val settingsFile: File by lazy {
        val os = System.getProperty("os.name").lowercase()
        val baseDir = when {
            os.contains("windows") -> {
                val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
                File(appData, "discord-rpc-gui")
            }
            else -> {
                val configHome = System.getenv("XDG_CONFIG_HOME")
                    ?: "${System.getProperty("user.home")}/.config"
                File(configHome, "discord-rpc-gui")
            }
        }
        baseDir.mkdirs()
        File(baseDir, "app-settings.json")
    }

    /**
     * Load all saved app settings from disk.
     */
    fun loadAll(): Map<String, AppRpcSettings> {
        return try {
            if (!settingsFile.exists()) return emptyMap()
            val content = settingsFile.readText()
            if (content.isBlank()) return emptyMap()
            val list = json.decodeFromString<List<AppRpcSettings>>(content)
            list.associateBy { it.processName }
        } catch (t: Throwable) {
            t.printStackTrace()
            emptyMap()
        }
    }

    /**
     * Get saved settings for a specific process name, or null if not found.
     */
    fun getForProcess(processName: String): AppRpcSettings? {
        return loadAll()[processName]
    }

    /**
     * Save or update settings for a specific process.
     */
    fun saveForProcess(settings: AppRpcSettings) {
        val allSettings = loadAll().toMutableMap()
        allSettings[settings.processName] = settings
        writeAll(allSettings)
    }

    /**
     * Delete saved settings for a specific process.
     */
    fun deleteForProcess(processName: String) {
        val allSettings = loadAll().toMutableMap()
        allSettings.remove(processName)
        writeAll(allSettings)
    }

    private fun writeAll(allSettings: Map<String, AppRpcSettings>) {
        try {
            val content = json.encodeToString(allSettings.values.toList())
            settingsFile.writeText(content)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
