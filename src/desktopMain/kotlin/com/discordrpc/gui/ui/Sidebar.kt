package com.discordrpc.gui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discordrpc.gui.rpc.RpcMode
import com.discordrpc.gui.state.AppState
import com.discordrpc.gui.state.MainViewModel

@Composable
fun Sidebar(state: AppState, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(AppTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        ConnectionCard(state, viewModel)
        ProcessSelectionArea(state, viewModel)
    }
}

@Composable
fun ConnectionCard(state: AppState, viewModel: MainViewModel) {
    var showToken by remember { mutableStateOf(false) }

    PlushCard(accentColor = AppTheme.colors.primaryContainer) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Connection Method", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
            Text("Select how you connect to Discord", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
        }

        // ── Mode Toggle ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.surfaceContainerLowest)
                .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RpcMode.entries.forEach { mode ->
                val isActive = state.rpcMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isActive) AppTheme.colors.primary.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable(enabled = !state.isConnected) { viewModel.switchMode(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (mode == RpcMode.LOCAL) "♙" else "🔑", color = if (isActive) AppTheme.colors.primary else AppTheme.colors.textSecondary)
                        Text(
                            mode.label,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) AppTheme.colors.primary else AppTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        if (state.rpcMode == RpcMode.GATEWAY) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.token,
                    onValueChange = { viewModel.updateField { s -> s.copy(token = it) } },
                    label = { Text("Discord Token") },
                    enabled = !state.isConnected && !state.isConnecting,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(16.dp)
                )
                SmallPillButton(
                    text = if (showToken) "HIDE" else "SHOW",
                    onClick = { showToken = !showToken },
                    color = AppTheme.colors.secondaryContainer,
                    textColor = AppTheme.colors.textPrimary
                )
            }
        }

        // ── Connect / Disconnect buttons ──
        if (state.isConnected) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    PlushButton(
                        text = "Disconnect",
                        onClick = { viewModel.disconnect() },
                        bgColor = AppTheme.colors.tertiaryContainer,
                        textColor = AppTheme.colors.textPrimary,
                        borderColor = AppTheme.colors.outlineVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PlushButton(
                        text = "Update",
                        onClick = { viewModel.updatePresence() },
                        bgColor = AppTheme.colors.primary,
                        textColor = AppTheme.colors.onPrimary,
                        borderColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            val canConnect = when (state.rpcMode) {
                RpcMode.LOCAL -> !state.isConnecting
                RpcMode.GATEWAY -> !state.isConnecting && state.token.isNotBlank()
            }
            PlushButton(
                text = if (state.isConnecting) "Connecting..." else "⏻ Connect",
                onClick = { viewModel.connect() },
                enabled = canConnect,
                bgColor = AppTheme.colors.primary,
                textColor = AppTheme.colors.onPrimary,
                borderColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProcessSelectionArea(state: AppState, viewModel: MainViewModel) {
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "PROCESS LIST",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textMuted,
            letterSpacing = 1.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.processSearchQuery,
                onValueChange = { viewModel.updateField { s -> s.copy(processSearchQuery = it) } },
                placeholder = { Text("Search apps...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(16.dp)
            )
            SmallPillButton(
                text = if (state.isLoadingProcesses) "..." else "⟳",
                onClick = { viewModel.refreshProcesses() },
                color = AppTheme.colors.surfaceContainerHighest,
                textColor = AppTheme.colors.textPrimary
            )
        }

        val filtered = state.filteredProcesses
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.pid }) { process ->
                ProcessItem(
                    process = process,
                    isSelected = state.selectedProcess?.name == process.name,
                    onClick = { viewModel.selectProcess(process) }
                )
            }
        }
    }
}
