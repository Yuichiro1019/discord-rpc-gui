package com.discordrpc.gui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discordrpc.gui.state.AppState
import com.discordrpc.gui.state.MainViewModel

@Composable
fun ConfigPane(state: AppState, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // ── Header & Actions ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Configuration", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                Text("Customize how you appear on Discord", fontSize = 14.sp, color = AppTheme.colors.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PlushButton(
                    text = "Reset",
                    onClick = { viewModel.clearProcessSelection() }, // Or reset to defaults
                    bgColor = AppTheme.colors.surfaceContainerHighest,
                    textColor = AppTheme.colors.textPrimary,
                    borderColor = AppTheme.colors.outlineVariant
                )
                PlushButton(
                    text = "Save Changes",
                    onClick = { viewModel.saveCurrentSettings() },
                    bgColor = AppTheme.colors.primary,
                    textColor = AppTheme.colors.onPrimary,
                    borderColor = AppTheme.colors.primary
                )
            }
        }

        // ── Masonry Grid ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Left Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                ActivityCard(state, viewModel)
                AdvancedCard(state, viewModel)
            }

            // Right Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                RpcFieldsCard(state, viewModel)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ActivityCard(state: AppState, viewModel: MainViewModel) {
    val enabled = state.selectedProcess == null || state.isEditingSettings || state.isNewProcess

    PlushCard(accentColor = AppTheme.colors.tertiaryContainer) {
        SectionHeader("✨", "Activity")

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ACTIVITY NAME", fontSize = 12.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = state.activityName,
                onValueChange = { viewModel.updateField { s -> s.copy(activityName = it) } },
                placeholder = { Text("e.g. Photoshop, Custom Game") },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ACTIVITY TYPE", fontSize = 12.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            
            // Grid-like toggles
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ActivityTypeButton(label = "Playing", typeId = 0, state = state, enabled = enabled, viewModel = viewModel, modifier = Modifier.weight(1f))
                    ActivityTypeButton(label = "Streaming", typeId = 1, state = state, enabled = enabled, viewModel = viewModel, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ActivityTypeButton(label = "Listening", typeId = 2, state = state, enabled = enabled, viewModel = viewModel, modifier = Modifier.weight(1f))
                    ActivityTypeButton(label = "Watching", typeId = 3, state = state, enabled = enabled, viewModel = viewModel, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActivityTypeButton(label: String, typeId: Int, state: AppState, enabled: Boolean, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val selected = state.activityType == typeId
    
    // Uses Tertiary Complementary color when active, subtle otherwise
    PlushButton(
        text = label,
        onClick = { if (enabled) viewModel.updateField { s -> s.copy(activityType = typeId) } },
        bgColor = if (selected) AppTheme.colors.tertiary else AppTheme.colors.surfaceContainerHighest,
        textColor = if (selected) AppTheme.colors.onPrimary else AppTheme.colors.textPrimary,
        borderColor = if (selected) AppTheme.colors.tertiary else AppTheme.colors.outlineVariant,
        modifier = modifier
    )
}

@Composable
fun RpcFieldsCard(state: AppState, viewModel: MainViewModel) {
    val enabled = state.selectedProcess == null || state.isEditingSettings || state.isNewProcess

    PlushCard(accentColor = AppTheme.colors.secondaryContainer) {
        SectionHeader("📝", "Details & Images")

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("DETAILS (LINE 1)", fontSize = 12.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = state.details,
                onValueChange = { viewModel.updateField { s -> s.copy(details = it) } },
                placeholder = { Text("Editing a cinematic sequence") },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("STATE (LINE 2)", fontSize = 12.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = state.state,
                onValueChange = { viewModel.updateField { s -> s.copy(state = it) } },
                placeholder = { Text("Project: Neo-Tokyo 2077") },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        HorizontalDivider(color = AppTheme.colors.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

        // Image Fields 
        ImageConfigRow(
            label = "LARGE IMAGE",
            keyVal = state.largeImageKey,
            keyOnChg = { viewModel.updateField { s -> s.copy(largeImageKey = it) } },
            ttVal = state.largeImageText,
            ttOnChg = { viewModel.updateField { s -> s.copy(largeImageText = it) } },
            enabled = enabled
        )

        ImageConfigRow(
            label = "SMALL IMAGE",
            keyVal = state.smallImageKey,
            keyOnChg = { viewModel.updateField { s -> s.copy(smallImageKey = it) } },
            ttVal = state.smallImageText,
            ttOnChg = { viewModel.updateField { s -> s.copy(smallImageText = it) } },
            enabled = enabled
        )

        OutlinedTextField(
            value = state.applicationId,
            onValueChange = { viewModel.updateField { s -> s.copy(applicationId = it) } },
            label = { Text("Application ID (optional)") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = plushTextFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun ImageConfigRow(
    label: String,
    keyVal: String, keyOnChg: (String) -> Unit,
    ttVal: String, ttOnChg: (String) -> Unit,
    enabled: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        // Image Placeholder Box
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🖼️", fontSize = 24.sp)
                Text(label, fontSize = 9.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold)
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = keyVal,
                onValueChange = keyOnChg,
                label = { Text("IMAGE KEY") },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = ttVal,
                onValueChange = ttOnChg,
                label = { Text("TOOLTIP TEXT") },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun AdvancedCard(state: AppState, viewModel: MainViewModel) {
    val timestampsEnabled = state.startTimestampStr.isNotBlank() || state.endTimestampStr.isNotBlank()

    PlushCard(accentColor = AppTheme.colors.surfaceContainerHigh) {
        SectionHeader("⚙️", "Advanced Settings")

        // Timestamps Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Timestamps", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
            Switch(
                checked = timestampsEnabled,
                onCheckedChange = { 
                    if (!it) {
                        viewModel.updateField { s -> s.copy(startTimestampStr = "", endTimestampStr = "") }
                    } else {
                        viewModel.updateField { s -> s.copy(startTimestampStr = "0:00:00") }
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppTheme.colors.onPrimary,
                    checkedTrackColor = AppTheme.colors.secondary, // Complementary pop
                    uncheckedThumbColor = AppTheme.colors.textMuted,
                    uncheckedTrackColor = AppTheme.colors.surfaceContainerLowest
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Text("START TIME", fontSize = 10.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = state.startTimestampStr,
                    onValueChange = { viewModel.updateField { s -> s.copy(startTimestampStr = it) } },
                    enabled = timestampsEnabled,
                    placeholder = { Text("0:00:00") },
                    singleLine = true,
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Text("END TIME", fontSize = 10.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = state.endTimestampStr,
                    onValueChange = { viewModel.updateField { s -> s.copy(endTimestampStr = it) } },
                    enabled = timestampsEnabled,
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        HorizontalDivider(color = AppTheme.colors.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

        Text("Party Secrets", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.matchSecret,
                onValueChange = { viewModel.updateField { s -> s.copy(matchSecret = it) } },
                label = { Text("MATCH SECRET") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = state.joinSecret,
                onValueChange = { viewModel.updateField { s -> s.copy(joinSecret = it) } },
                label = { Text("JOIN SECRET") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
