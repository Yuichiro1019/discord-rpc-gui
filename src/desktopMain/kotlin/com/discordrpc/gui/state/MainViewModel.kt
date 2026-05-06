package com.discordrpc.gui.state

import com.discordrpc.gui.process.ProcessDetector
import com.discordrpc.gui.process.ProcessInfo
import com.discordrpc.gui.rpc.DiscordRpcService
import com.discordrpc.gui.rpc.GatewayRpcServiceImpl
import com.discordrpc.gui.rpc.LocalRpcServiceImpl
import com.discordrpc.gui.rpc.NativeIpcRpcServiceImpl
import com.discordrpc.gui.rpc.RpcMode
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
    private val processDetector: ProcessDetector = ProcessDetector.create(),
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // Active RPC service — switches based on mode
    private var rpcService: DiscordRpcService = LocalRpcServiceImpl()

    init {
        val globalSettings = settingsRepository.loadGlobalSettings()
        _state.value = AppState(
            activeThemeId = globalSettings.themeId,
            customThemeHex = globalSettings.customThemeHex
        )
    }

    fun updateField(updater: (AppState) -> AppState) {
        _state.update(updater)
    }

    fun changeTheme(themeId: String, customHex: String = "") {
        _state.update { it.copy(activeThemeId = themeId, customThemeHex = customHex) }
        coroutineScope.launch(Dispatchers.IO) {
            settingsRepository.saveGlobalSettings(
                com.discordrpc.gui.settings.AppGlobalSettings(themeId = themeId, customThemeHex = customHex)
            )
        }
    }

    // ── Mode Switching ──

    fun switchMode(mode: RpcMode) {
        coroutineScope.launch {
            // Disconnect current service if connected
            if (_state.value.isConnected) {
                rpcService.disconnect()
            }

            // Create new service for the selected mode
            rpcService = when (mode) {
                RpcMode.LOCAL -> LocalRpcServiceImpl()
                RpcMode.GATEWAY -> GatewayRpcServiceImpl()
                RpcMode.NATIVE_IPC -> NativeIpcRpcServiceImpl()
            }

            _state.update {
                it.copy(
                    rpcMode = mode,
                    isConnected = false,
                    isConnecting = false,
                    statusMessage = "Mode: ${mode.label} — Disconnected"
                )
            }
        }
    }

    // ── Connection ──

    fun connect() {
        coroutineScope.launch {
            val currentState = _state.value

            // Validate credentials based on mode
            val credential = when (currentState.rpcMode) {
                RpcMode.LOCAL -> {
                    // For local/arRPC, use applicationId if set, otherwise a default
                    currentState.applicationId.ifBlank { "1045800378228281345" } // arRPC default
                }
                RpcMode.GATEWAY -> {
                    if (currentState.token.isBlank()) {
                        _state.update { it.copy(statusMessage = "Token is required for Gateway mode") }
                        return@launch
                    }
                    currentState.token
                }
                RpcMode.NATIVE_IPC -> {
                    // For Native IPC, use applicationId as the Discord Client ID
                    currentState.applicationId.ifBlank { "835460649798467584" }
                }
            }

            _state.update { it.copy(isConnecting = true, statusMessage = "Connecting...") }
            try {
                val success = rpcService.connect(credential)
                _state.update {
                    it.copy(
                        isConnected = success,
                        isConnecting = false,
                        statusMessage = if (success) "Connected via ${currentState.rpcMode.label}" else "Connection failed"
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

    /**
     * When user clicks a process:
     * - Check JSON DB for saved config
     * - If exists: load all fields, set isEditingSettings=false (read-only view)
     * - If not: blank slate, set isNewProcess=true, isEditingSettings=true (editable)
     */
    fun selectProcess(process: ProcessInfo) {
        coroutineScope.launch {
            val saved = withContext(Dispatchers.IO) {
                settingsRepository.getForProcess(process.name)
            }

            if (saved != null) {
                // Existing config — load read-only
                _state.update {
                    it.copy(
                        selectedProcess = process,
                        savedSettings = saved,
                        isNewProcess = false,
                        isEditingSettings = false,
                        activityName = saved.displayName.ifBlank { process.displayName },
                        activityType = saved.activityType,
                        details = saved.details,
                        state = saved.state,
                        largeImageKey = saved.largeImageKey,
                        largeImageText = saved.largeImageText,
                        smallImageKey = saved.smallImageKey,
                        smallImageText = saved.smallImageText,
                        startTimestampStr = saved.startTimestamp,
                        endTimestampStr = saved.endTimestamp,
                        matchSecret = saved.matchSecret,
                        joinSecret = saved.joinSecret,
                        button1Label = saved.button1Label,
                        button1Url = saved.button1Url,
                        button2Label = saved.button2Label,
                        button2Url = saved.button2Url,
                        timestampsEnabled = saved.timestampsEnabled,
                        partySecretsEnabled = saved.partySecretsEnabled,
                        buttonsEnabled = saved.buttonsEnabled
                    )
                }
            } else {
                // New process — editable blank slate
                _state.update {
                    it.copy(
                        selectedProcess = process,
                        savedSettings = null,
                        isNewProcess = true,
                        isEditingSettings = true,
                        activityName = process.displayName,
                        activityType = 0,
                        details = "",
                        state = "",
                        largeImageKey = "",
                        largeImageText = "",
                        smallImageKey = "",
                        smallImageText = "",
                        startTimestampStr = "",
                        endTimestampStr = "",
                        matchSecret = "",
                        joinSecret = "",
                        button1Label = "",
                        button1Url = "",
                        button2Label = "",
                        button2Url = "",
                        timestampsEnabled = false,
                        partySecretsEnabled = false,
                        buttonsEnabled = false
                    )
                }
            }

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
        val current = _state.value
        if (current.isEditingSettings) {
            // Cancelling edit — revert to saved values
            val saved = current.savedSettings
            if (saved != null) {
                _state.update {
                    it.copy(
                        isEditingSettings = false,
                        activityName = saved.displayName.ifBlank { current.selectedProcess?.displayName ?: "" },
                        activityType = saved.activityType,
                        details = saved.details,
                        state = saved.state,
                        largeImageKey = saved.largeImageKey,
                        largeImageText = saved.largeImageText,
                        smallImageKey = saved.smallImageKey,
                        smallImageText = saved.smallImageText,
                        startTimestampStr = saved.startTimestamp,
                        endTimestampStr = saved.endTimestamp,
                        matchSecret = saved.matchSecret,
                        joinSecret = saved.joinSecret,
                        button1Label = saved.button1Label,
                        button1Url = saved.button1Url,
                        button2Label = saved.button2Label,
                        button2Url = saved.button2Url,
                        timestampsEnabled = saved.timestampsEnabled,
                        partySecretsEnabled = saved.partySecretsEnabled,
                        buttonsEnabled = saved.buttonsEnabled
                    )
                }
            } else {
                _state.update { it.copy(isEditingSettings = false) }
            }
        } else {
            _state.update { it.copy(isEditingSettings = true) }
        }
    }

    fun saveCurrentSettings() {
        val currentState = _state.value
        val process = currentState.selectedProcess ?: return

        val settings = AppRpcSettings(
            processName = process.name,
            displayName = currentState.activityName,
            activityType = currentState.activityType,
            details = currentState.details,
            state = currentState.state,
            largeImageKey = currentState.largeImageKey,
            largeImageText = currentState.largeImageText,
            smallImageKey = currentState.smallImageKey,
            smallImageText = currentState.smallImageText,
            startTimestamp = currentState.startTimestampStr,
            endTimestamp = currentState.endTimestampStr,
            matchSecret = currentState.matchSecret,
            joinSecret = currentState.joinSecret,
            button1Label = currentState.button1Label,
            button1Url = currentState.button1Url,
            button2Label = currentState.button2Label,
            button2Url = currentState.button2Url,
            timestampsEnabled = currentState.timestampsEnabled,
            partySecretsEnabled = currentState.partySecretsEnabled,
            buttonsEnabled = currentState.buttonsEnabled
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
