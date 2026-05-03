package com.discordrpc.gui

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
        AppTheme {
            MainScreen(viewModel)
        }
    }
}
