package com.discordrpc.gui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.discordrpc.gui.state.MainViewModel
import com.discordrpc.gui.ui.AppTheme
import com.discordrpc.gui.ui.MainScreen

fun main() = application {
    val viewModel = remember { MainViewModel() }

    Window(
        onCloseRequest = {
            viewModel.disconnect()
            exitApplication()
        },
        title = "Discord RPC",
        state = WindowState(width = 720.dp, height = 800.dp)
    ) {
        val state by viewModel.state.collectAsState()
        
        val colors = when (state.activeThemeId) {
            "PlushAmber" -> com.discordrpc.gui.theme.ThemeEngine.PlushAmber
            "ObsidianViolet" -> com.discordrpc.gui.theme.ThemeEngine.ObsidianViolet
            "DiscordBlurple" -> com.discordrpc.gui.theme.ThemeEngine.DiscordBlurple
            "NeonCyber" -> com.discordrpc.gui.theme.ThemeEngine.NeonCyber
            "CrimsonForge" -> com.discordrpc.gui.theme.ThemeEngine.CrimsonForge
            "Custom" -> {
                try {
                    val hex = state.customThemeHex.removePrefix("#")
                    val color = androidx.compose.ui.graphics.Color(hex.toLong(16) or 0xFF000000)
                    com.discordrpc.gui.theme.ThemeEngine.generateTheme(color)
                } catch (e: Exception) {
                    com.discordrpc.gui.theme.ThemeEngine.PlushAmber
                }
            }
            else -> com.discordrpc.gui.theme.ThemeEngine.PlushAmber
        }

        AppTheme(colors = colors) {
            MainScreen(viewModel)
        }
    }
}
