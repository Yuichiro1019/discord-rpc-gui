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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discordrpc.gui.state.AppState
import com.discordrpc.gui.state.MainViewModel

@Composable
fun ConfigPane(state: AppState, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val canEdit = state.isEditingSettings || state.isNewProcess

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
            ConfigActionButtons(state, viewModel)
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
                ActivityCard(state, viewModel, canEdit)
                AdvancedCard(state, viewModel, canEdit)
            }

            // Right Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                RpcFieldsCard(state, viewModel, canEdit)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Smart action buttons:
 * - No process selected: nothing shown
 * - Existing process, not editing: "Edit" button
 * - Editing, has unsaved changes: "Cancel" + "Save Changes"
 * - Editing, no changes: "Cancel" only
 * - New process: "Save Changes" always (since it's all new)
 */
@Composable
fun ConfigActionButtons(state: AppState, viewModel: MainViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.selectedProcess == null) {
            // No process selected — no buttons
            return
        }

        if (!state.isNewProcess && !state.isEditingSettings) {
            // Viewing saved config — show Edit + Unselect
            PlushButton(
                text = "✏️ Edit",
                onClick = { viewModel.toggleEditSettings() },
                bgColor = AppTheme.colors.secondaryContainer,
                textColor = AppTheme.colors.textPrimary,
                borderColor = AppTheme.colors.secondary
            )
            PlushButton(
                text = "Unselect",
                onClick = { viewModel.clearProcessSelection() },
                bgColor = AppTheme.colors.surfaceContainerHighest,
                textColor = AppTheme.colors.textPrimary,
                borderColor = AppTheme.colors.outlineVariant
            )
        } else {
            // Editing or New process
            if (!state.isNewProcess) {
                PlushButton(
                    text = "Cancel",
                    onClick = { viewModel.toggleEditSettings() },
                    bgColor = AppTheme.colors.surfaceContainerHighest,
                    textColor = AppTheme.colors.textPrimary,
                    borderColor = AppTheme.colors.outlineVariant
                )
            } else {
                PlushButton(
                    text = "Discard",
                    onClick = { viewModel.clearProcessSelection() },
                    bgColor = AppTheme.colors.surfaceContainerHighest,
                    textColor = AppTheme.colors.textPrimary,
                    borderColor = AppTheme.colors.outlineVariant
                )
            }

            if (state.hasUnsavedChanges) {
                PlushButton(
                    text = "💾 Save Changes",
                    onClick = { viewModel.saveCurrentSettings() },
                    bgColor = AppTheme.colors.primary,
                    textColor = AppTheme.colors.onPrimary,
                    borderColor = AppTheme.colors.primary
                )
            }
        }
    }
}

// ── Activity Card ──

@Composable
fun ActivityCard(state: AppState, viewModel: MainViewModel, canEdit: Boolean) {
    PlushCard(accentColor = AppTheme.colors.tertiaryContainer) {
        SectionHeader("✨", "Activity")

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ACTIVITY NAME", fontSize = 12.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = state.activityName,
                onValueChange = { viewModel.updateField { s -> s.copy(activityName = it) } },
                placeholder = { Text("e.g. Photoshop, Custom Game") },
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ACTIVITY TYPE", fontSize = 12.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ActivityTypeButton("Playing", 0, state, canEdit, viewModel, Modifier.weight(1f))
                    ActivityTypeButton("Streaming", 1, state, canEdit, viewModel, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ActivityTypeButton("Listening", 2, state, canEdit, viewModel, Modifier.weight(1f))
                    ActivityTypeButton("Watching", 3, state, canEdit, viewModel, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActivityTypeButton(label: String, typeId: Int, state: AppState, enabled: Boolean, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val selected = state.activityType == typeId

    PlushButton(
        text = label,
        onClick = { if (enabled) viewModel.updateField { s -> s.copy(activityType = typeId) } },
        enabled = enabled,
        bgColor = if (selected) AppTheme.colors.tertiary else AppTheme.colors.surfaceContainerHighest,
        textColor = if (selected) AppTheme.colors.onPrimary else AppTheme.colors.textPrimary,
        borderColor = if (selected) AppTheme.colors.tertiary else AppTheme.colors.outlineVariant,
        modifier = modifier
    )
}

// ── Details & Images Card ──

@Composable
fun RpcFieldsCard(state: AppState, viewModel: MainViewModel, canEdit: Boolean) {
    PlushCard(accentColor = AppTheme.colors.secondaryContainer) {
        SectionHeader("📝", "Details & Images")

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("DETAILS (LINE 1)", fontSize = 12.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = state.details,
                onValueChange = { viewModel.updateField { s -> s.copy(details = it) } },
                placeholder = { Text("Editing a cinematic sequence") },
                enabled = canEdit,
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
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        HorizontalDivider(color = AppTheme.colors.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

        ImageConfigRow(
            label = "LARGE IMAGE",
            keyVal = state.largeImageKey,
            keyOnChg = { viewModel.updateField { s -> s.copy(largeImageKey = it) } },
            ttVal = state.largeImageText,
            ttOnChg = { viewModel.updateField { s -> s.copy(largeImageText = it) } },
            enabled = canEdit
        )

        ImageConfigRow(
            label = "SMALL IMAGE",
            keyVal = state.smallImageKey,
            keyOnChg = { viewModel.updateField { s -> s.copy(smallImageKey = it) } },
            ttVal = state.smallImageText,
            ttOnChg = { viewModel.updateField { s -> s.copy(smallImageText = it) } },
            enabled = canEdit
        )

        OutlinedTextField(
            value = state.applicationId,
            onValueChange = { viewModel.updateField { s -> s.copy(applicationId = it) } },
            label = { Text("Application ID (optional)") },
            enabled = canEdit,
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

// ── Advanced Settings Card (with toggles) ──

@Composable
fun AdvancedCard(state: AppState, viewModel: MainViewModel, canEdit: Boolean) {
    PlushCard(accentColor = AppTheme.colors.surfaceContainerHigh) {
        SectionHeader("⚙️", "Advanced Settings")

        // ── Timestamps Section ──
        ToggleSection(
            title = "⏱ Timestamps",
            enabled = state.timestampsEnabled,
            onToggle = { viewModel.updateField { s -> s.copy(timestampsEnabled = it) } },
            canEdit = canEdit
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Text("START TIME", fontSize = 10.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = state.startTimestampStr,
                        onValueChange = { viewModel.updateField { s -> s.copy(startTimestampStr = it) } },
                        enabled = canEdit && state.timestampsEnabled,
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
                        enabled = canEdit && state.timestampsEnabled,
                        placeholder = { Text("Optional") },
                        singleLine = true,
                        colors = plushTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = AppTheme.colors.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

        // ── Party Secrets Section ──
        ToggleSection(
            title = "🔐 Party Secrets",
            enabled = state.partySecretsEnabled,
            onToggle = { viewModel.updateField { s -> s.copy(partySecretsEnabled = it) } },
            canEdit = canEdit
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.matchSecret,
                    onValueChange = { viewModel.updateField { s -> s.copy(matchSecret = it) } },
                    label = { Text("MATCH SECRET") },
                    enabled = canEdit && state.partySecretsEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.joinSecret,
                    onValueChange = { viewModel.updateField { s -> s.copy(joinSecret = it) } },
                    label = { Text("JOIN SECRET") },
                    enabled = canEdit && state.partySecretsEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        HorizontalDivider(color = AppTheme.colors.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

        // ── Action Buttons Section ──
        ToggleSection(
            title = "🔘 Action Buttons",
            enabled = state.buttonsEnabled,
            onToggle = { viewModel.updateField { s -> s.copy(buttonsEnabled = it) } },
            canEdit = canEdit
        ) {
            // Button 1
            Text("BUTTON 1", fontSize = 10.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.button1Label,
                    onValueChange = { viewModel.updateField { s -> s.copy(button1Label = it) } },
                    enabled = canEdit && state.buttonsEnabled,
                    placeholder = { Text("e.g. Website") },
                    label = { Text("LABEL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.button1Url,
                    onValueChange = { viewModel.updateField { s -> s.copy(button1Url = it) } },
                    enabled = canEdit && state.buttonsEnabled,
                    placeholder = { Text("https://...") },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button 2
            Text("BUTTON 2", fontSize = 10.sp, color = AppTheme.colors.textMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.button2Label,
                    onValueChange = { viewModel.updateField { s -> s.copy(button2Label = it) } },
                    enabled = canEdit && state.buttonsEnabled,
                    placeholder = { Text("e.g. GitHub") },
                    label = { Text("LABEL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.button2Url,
                    onValueChange = { viewModel.updateField { s -> s.copy(button2Url = it) } },
                    enabled = canEdit && state.buttonsEnabled,
                    placeholder = { Text("https://...") },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = plushTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * A reusable toggle section: header row with title + switch,
 * then the content below. Content fields are visually disabled
 * (grayed out) when the toggle is off.
 */
@Composable
fun ToggleSection(
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    canEdit: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
            Switch(
                checked = enabled,
                onCheckedChange = { if (canEdit) onToggle(it) },
                enabled = canEdit,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppTheme.colors.onPrimary,
                    checkedTrackColor = AppTheme.colors.secondary,
                    uncheckedThumbColor = AppTheme.colors.textMuted,
                    uncheckedTrackColor = AppTheme.colors.surfaceContainerLowest,
                    disabledCheckedThumbColor = AppTheme.colors.textMuted.copy(alpha = 0.5f),
                    disabledCheckedTrackColor = AppTheme.colors.secondary.copy(alpha = 0.3f),
                    disabledUncheckedThumbColor = AppTheme.colors.textMuted.copy(alpha = 0.3f),
                    disabledUncheckedTrackColor = AppTheme.colors.surfaceContainerLowest.copy(alpha = 0.3f)
                )
            )
        }

        // Content is always rendered, but fields inside check `enabled` flag
        Box(modifier = Modifier.alpha(if (enabled) 1f else 0.4f)) {
            Column {
                content()
            }
        }
    }
}
