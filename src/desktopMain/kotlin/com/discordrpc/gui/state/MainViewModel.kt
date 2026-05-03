package com.discordrpc.gui.state

import com.discordrpc.gui.process.ProcessDetector
import com.discordrpc.gui.process.ProcessInfo
import com.discordrpc.gui.rpc.DiscordRpcService
import com.discordrpc.gui.rpc.GatewayRpcServiceImpl
import com.discordrpc.gui.settings.AppRpcSettings
import com.discordrpc.gui.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val rpcService: DiscordRpcService = GatewayRpcServiceImpl(),
    private val processDetector: ProcessDetector = ProcessDetector.create(),
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun updateField(updater: (AppState) -> AppState) {
        _state.update(updater)
    }

    // ── Connection ──

    fun connect() {
        coroutineScope.launch {
            val currentState = _state.value
            if (currentState.token.isBlank()) {
                _state.update { it.copy(statusMessage = "Token is required") }
                return@launch
            }

            _state.update { it.copy(isConnecting = true, statusMessage = "Connecting...") }
            try {
                val success = rpcService.connect(currentState.token)
                _state.update {
                    it.copy(
                        isConnected = success,
                        isConnecting = false,
                        statusMessage = if (success) "Connected" else "Connection failed — check token"
                    )
                }
                if (success) updatePresence()
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
                        statusMessage = "Error: ${t.message}"
                    )
                }
            }
        }
    }

    fun disconnect() {
        coroutineScope.launch {
            rpcService.disconnect()
            _state.update {
                it.copy(isConnected = false, statusMessage = "Disconnected")
            }
        }
    }

    fun updatePresence() {
        coroutineScope.launch {
            if (_state.value.isConnected) {
                try {
                    rpcService.updatePresence(_state.value)
                    _state.update { it.copy(statusMessage = "Presence updated") }
                } catch (t: Throwable) {
                    _state.update { it.copy(statusMessage = "Update failed: ${t.message}") }
                }
            }
        }
    }

    // ── Process Detection ──

    fun refreshProcesses() {
        coroutineScope.launch {
            _state.update { it.copy(isLoadingProcesses = true) }
            val processes = withContext(Dispatchers.IO) {
                processDetector.listProcesses()
            }
            _state.update {
                it.copy(runningProcesses = processes, isLoadingProcesses = false)
            }
        }
    }

    fun selectProcess(process: ProcessInfo) {
        coroutineScope.launch {
            val saved = withContext(Dispatchers.IO) {
                settingsRepository.getForProcess(process.name)
            }

            if (saved != null) {
                _state.update {
                    it.copy(
                        selectedProcess = process,
                        savedSettings = saved,
                        isNewProcess = false,
                        isEditingSettings = false,
                        activityName = saved.displayName.ifBlank { process.displayName },
                        details = saved.details,
                        state = saved.state,
                        largeImageKey = saved.largeImageKey,
                        largeImageText = saved.largeImageText,
                        smallImageKey = saved.smallImageKey,
                        smallImageText = saved.smallImageText
                    )
                }
            } else {
                // New process — pre-fill activity name from desktop name
                _state.update {
                    it.copy(
                        selectedProcess = process,
                        savedSettings = null,
                        isNewProcess = true,
                        isEditingSettings = true,
                        activityName = process.displayName,
                        details = "",
                        state = "",
                        largeImageKey = "",
                        largeImageText = "",
                        smallImageKey = "",
                        smallImageText = ""
                    )
                }
            }

            // Auto-update presence if connected
            if (_state.value.isConnected) updatePresence()
        }
    }

    fun clearProcessSelection() {
        _state.update {
            it.copy(
                selectedProcess = null,
                savedSettings = null,
                isNewProcess = false,
                isEditingSettings = false,
                processSearchQuery = ""
            )
        }
    }

    fun toggleEditSettings() {
        _state.update { it.copy(isEditingSettings = !it.isEditingSettings) }
    }

    fun saveCurrentSettings() {
        val currentState = _state.value
        val process = currentState.selectedProcess ?: return

        val settings = AppRpcSettings(
            processName = process.name,
            displayName = currentState.activityName,
            details = currentState.details,
            state = currentState.state,
            largeImageKey = currentState.largeImageKey,
            largeImageText = currentState.largeImageText,
            smallImageKey = currentState.smallImageKey,
            smallImageText = currentState.smallImageText
        )

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                settingsRepository.saveForProcess(settings)
            }
            _state.update {
                it.copy(
                    savedSettings = settings,
                    isNewProcess = false,
                    isEditingSettings = false,
                    statusMessage = "Settings saved for ${process.displayName}"
                )
            }
        }
    }
}
