package com.discordrpc.gui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import com.discordrpc.gui.state.MainViewModel
import com.discordrpc.gui.ui.AppTheme
import com.discordrpc.gui.ui.MainScreen

import androidx.compose.ui.res.painterResource

fun main() = application {
    val screenSize = java.awt.Toolkit.getDefaultToolkit().screenSize
    val ratio = if (screenSize.width <= 1920) 0.8 else 0.6
    val w = (screenSize.width * ratio).toInt()
    val h = (screenSize.height * ratio).toInt()
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Discord RPC GUI",
        state = rememberWindowState(size = DpSize(w.dp, h.dp)),
        transparent = true,
        undecorated = true,
        icon = painterResource("logo.png")
    ) {
        window.minimumSize = java.awt.Dimension(w, h)
        
        val viewModel = remember { MainViewModel() }
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
            MainScreen(viewModel, onClose = ::exitApplication)
        }
    }
}
