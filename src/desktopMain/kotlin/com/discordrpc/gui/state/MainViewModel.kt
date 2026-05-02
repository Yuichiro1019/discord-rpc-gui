package com.discordrpc.gui.state

import com.discordrpc.gui.rpc.DiscordRpcService
import com.discordrpc.gui.rpc.KDiscordRpcServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val rpcService: DiscordRpcService = KDiscordRpcServiceImpl(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun updateField(updater: (AppState) -> AppState) {
        _state.update(updater)
    }

    fun connect() {
        coroutineScope.launch {
            val currentState = _state.value
            if (currentState.clientId.isBlank()) return@launch
            
            val success = rpcService.connect(currentState.clientId)
            if (success) {
                _state.update { it.copy(isConnected = true) }
                updatePresence() // update initially after connecting
            }
        }
    }

    fun disconnect() {
        coroutineScope.launch {
            rpcService.disconnect()
            _state.update { it.copy(isConnected = false) }
        }
    }

    fun updatePresence() {
        coroutineScope.launch {
            if (_state.value.isConnected) {
                rpcService.updatePresence(_state.value)
            }
        }
    }
}
