package com.discordrpc.gui

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
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
        title = "Discord RPC GUI"
    ) {
        AppTheme {
            MainScreen(viewModel)
        }
    }
}
