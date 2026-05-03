package com.discordrpc.gui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discordrpc.gui.process.ProcessInfo
import com.discordrpc.gui.rpc.RpcMode
import com.discordrpc.gui.state.AppState
import com.discordrpc.gui.state.MainViewModel
import java.io.File
import javax.imageio.ImageIO

// ── Reusable Glass Card ──

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderBrush = if (accentColor != null) {
        Brush.linearGradient(listOf(accentColor.copy(alpha = 0.4f), AppColors.cardBorder))
    } else {
        Brush.linearGradient(listOf(AppColors.cardBorder, AppColors.cardBorder))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.surfaceElevated.copy(alpha = 0.6f),
                        AppColors.surface.copy(alpha = 0.4f)
                    )
                )
            )
            .border(1.dp, borderBrush, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun SectionHeader(icon: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
        )
    }
}

// ── Main Screen ──

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.surface, AppColors.background, AppColors.background)
                )
            )
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(AppColors.primary, AppColors.accentGradientEnd, AppColors.accentPink)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎮", fontSize = 22.sp)
                    Column {
                        Text(
                            "Discord RPC",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            "Gateway Mode",
                            fontSize = 10.sp,
                            color = AppColors.textPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
                StatusPill(state)
            }
        }

        // ── Scrollable Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ConnectionCard(state, viewModel)
            ProcessSelectionCard(state, viewModel)
            ActivityCard(state, viewModel)
            RpcFieldsCard(state, viewModel)
            AdvancedCard(state, viewModel)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatusPill(state: AppState) {
    val (pillColor, label) = when {
        state.isConnecting -> AppColors.warning to "Connecting..."
        state.isConnected -> AppColors.online to "Connected"
        else -> AppColors.offline to state.statusMessage
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(pillColor.copy(alpha = 0.15f))
            .border(1.dp, pillColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(pillColor)
        )
        Text(
            text = label,
            color = pillColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Connection Card ──

@Composable
fun ConnectionCard(state: AppState, viewModel: MainViewModel) {
    var showToken by remember { mutableStateOf(false) }

    GlassCard(accentColor = AppColors.primary) {
        SectionHeader("🔗", "Connection")

        // ── Mode Toggle ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.background.copy(alpha = 0.5f))
                .border(1.dp, AppColors.cardBorder, RoundedCornerShape(10.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RpcMode.entries.forEach { mode ->
                val isActive = state.rpcMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isActive) Brush.horizontalGradient(
                                listOf(AppColors.primary.copy(alpha = 0.3f), AppColors.accentGradientEnd.copy(alpha = 0.15f))
                            )
                            else Brush.horizontalGradient(
                                listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Transparent)
                            )
                        )
                        .clickable(enabled = !state.isConnected) { viewModel.switchMode(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            mode.label,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) AppColors.primaryLight else AppColors.textMuted
                        )
                        Text(
                            if (mode == RpcMode.LOCAL) "No token needed" else "Standalone",
                            fontSize = 9.sp,
                            color = if (isActive) AppColors.textSecondary else AppColors.textMuted
                        )
                    }
                }
            }
        }

        // ── Mode description ──
        Text(
            state.rpcMode.description,
            fontSize = 11.sp,
            color = AppColors.textMuted
        )

        // ── Token input (Gateway mode only) ──
        if (state.rpcMode == RpcMode.GATEWAY) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.token,
                    onValueChange = { viewModel.updateField { s -> s.copy(token = it) } },
                    label = { Text("Discord Token") },
                    enabled = !state.isConnected && !state.isConnecting,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = glassTextFieldColors()
                )
                SmallPillButton(
                    text = if (showToken) "👁 Hide" else "👁 Show",
                    onClick = { showToken = !showToken }
                )
            }
        }

        // ── Connect / Disconnect buttons ──
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.isConnected) {
                GradientButton(
                    text = "Disconnect",
                    gradient = Brush.horizontalGradient(listOf(AppColors.error, AppColors.errorDim)),
                    onClick = { viewModel.disconnect() }
                )
                GradientButton(
                    text = "⟳ Update Presence",
                    gradient = Brush.horizontalGradient(listOf(AppColors.primary, AppColors.accentGradientEnd)),
                    onClick = { viewModel.updatePresence() }
                )
            } else {
                val canConnect = when (state.rpcMode) {
                    RpcMode.LOCAL -> !state.isConnecting
                    RpcMode.GATEWAY -> !state.isConnecting && state.token.isNotBlank()
                }
                GradientButton(
                    text = if (state.isConnecting) "Connecting..." else "Connect",
                    gradient = Brush.horizontalGradient(listOf(AppColors.primary, AppColors.primaryLight)),
                    onClick = { viewModel.connect() },
                    enabled = canConnect
                )
            }
        }
    }
}

// ── Process Selection Card ──

@Composable
fun ProcessSelectionCard(state: AppState, viewModel: MainViewModel) {
    GlassCard(accentColor = AppColors.secondary) {
        SectionHeader("📋", "Process Selection")

        val selected = state.selectedProcess
        if (selected != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (state.savedSettings != null && !state.isNewProcess)
                            Brush.horizontalGradient(
                                listOf(AppColors.online.copy(alpha = 0.1f), AppColors.secondary.copy(alpha = 0.05f))
                            )
                        else
                            Brush.horizontalGradient(
                                listOf(AppColors.warning.copy(alpha = 0.1f), AppColors.accentGold.copy(alpha = 0.05f))
                            )
                    )
                    .border(
                        1.dp,
                        if (state.savedSettings != null && !state.isNewProcess)
                            AppColors.online.copy(alpha = 0.25f)
                        else
                            AppColors.warning.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        selected.displayName,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        if (state.savedSettings != null && !state.isNewProcess)
                            "✅ Saved settings loaded"
                        else "⚡ New app — configure & save",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (state.savedSettings != null && !state.isNewProcess) {
                        SmallPillButton(
                            text = if (state.isEditingSettings) "🔒 Lock" else "✏️ Edit",
                            onClick = { viewModel.toggleEditSettings() }
                        )
                    }
                    SmallPillButton("💾 Save", onClick = { viewModel.saveCurrentSettings() })
                    SmallPillButton("✕", onClick = { viewModel.clearProcessSelection() })
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.processSearchQuery,
                onValueChange = { viewModel.updateField { s -> s.copy(processSearchQuery = it) } },
                label = { Text("🔍 Search apps...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            GradientButton(
                text = if (state.isLoadingProcesses) "..." else "Refresh",
                gradient = Brush.horizontalGradient(listOf(AppColors.surfaceVariant, AppColors.surfaceElevated)),
                onClick = { viewModel.refreshProcesses() },
                enabled = !state.isLoadingProcesses
            )
        }

        if (state.runningProcesses.isNotEmpty()) {
            val filtered = state.filteredProcesses
            Text(
                "${filtered.size} app${if (filtered.size != 1) "s" else ""} detected",
                fontSize = 11.sp,
                color = AppColors.textMuted
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.background.copy(alpha = 0.6f))
                    .border(1.dp, AppColors.cardBorder, RoundedCornerShape(12.dp)),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(filtered, key = { it.pid }) { process ->
                    ProcessItem(
                        process = process,
                        isSelected = state.selectedProcess?.name == process.name,
                        onClick = { viewModel.selectProcess(process) }
                    )
                    if (process != filtered.last()) {
                        HorizontalDivider(color = AppColors.cardBorder, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessItem(process: ProcessInfo, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected)
        AppColors.primary.copy(alpha = 0.15f)
    else
        androidx.compose.ui.graphics.Color.Transparent

    val icon: ImageBitmap? = remember(process.iconPath) {
        process.iconPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists() && (path.endsWith(".png") || path.endsWith(".xpm"))) {
                    ImageIO.read(file)?.toComposeImageBitmap()
                } else null
            } catch (_: Throwable) { null }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = process.displayName,
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.primary.copy(alpha = 0.3f), AppColors.accentGradientEnd.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    process.displayName.first().uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primaryLight
                )
            }
        }

        // Name + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                process.displayName,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AppColors.primaryLight else AppColors.textPrimary
            )
            if (process.desktopName != null && process.desktopName != process.name) {
                Text(process.name, fontSize = 10.sp, color = AppColors.textMuted)
            }
        }

        // Memory badge
        Text(
            formatMemory(process.memoryKb),
            fontSize = 10.sp,
            color = AppColors.textMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.card)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── Activity Card ──

@Composable
fun ActivityCard(state: AppState, viewModel: MainViewModel) {
    val enabled = state.selectedProcess == null || state.isEditingSettings || state.isNewProcess

    GlassCard(accentColor = AppColors.accentPink) {
        SectionHeader("✨", "Activity")
        Text(
            "The name is what shows as the title (e.g. \"Playing Heroic Games Launcher\")",
            fontSize = 11.sp,
            color = AppColors.textMuted
        )

        OutlinedTextField(
            value = state.activityName,
            onValueChange = { viewModel.updateField { s -> s.copy(activityName = it) } },
            label = { Text("Activity Name") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = glassTextFieldColors()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Type:", fontSize = 12.sp, color = AppColors.textSecondary)
            AppState.ACTIVITY_TYPES.forEach { (typeId, label) ->
                val selected = state.activityType == typeId
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (enabled) viewModel.updateField { s -> s.copy(activityType = typeId) }
                    },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.primary.copy(alpha = 0.25f),
                        selectedLabelColor = AppColors.primaryLight,
                        containerColor = AppColors.card,
                        labelColor = AppColors.textMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = AppColors.cardBorder,
                        selectedBorderColor = AppColors.primary.copy(alpha = 0.5f),
                        enabled = true,
                        selected = selected
                    )
                )
            }
        }
    }
}

// ── RPC Details Card ──

@Composable
fun RpcFieldsCard(state: AppState, viewModel: MainViewModel) {
    val enabled = state.selectedProcess == null || state.isEditingSettings || state.isNewProcess

    GlassCard(accentColor = AppColors.accent) {
        SectionHeader("📝", "Details & Images")

        OutlinedTextField(
            value = state.details,
            onValueChange = { viewModel.updateField { s -> s.copy(details = it) } },
            label = { Text("Details (Line 1)") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = glassTextFieldColors()
        )
        OutlinedTextField(
            value = state.state,
            onValueChange = { viewModel.updateField { s -> s.copy(state = it) } },
            label = { Text("State (Line 2)") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = glassTextFieldColors()
        )

        HorizontalDivider(color = AppColors.cardBorder, thickness = 0.5.dp)

        Text("🖼 Images", fontSize = 12.sp, color = AppColors.textSecondary)
        Text(
            "Use asset keys from Dev Portal, or mp:attachments/... URLs for external images",
            fontSize = 10.sp,
            color = AppColors.textMuted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.largeImageKey,
                onValueChange = { viewModel.updateField { s -> s.copy(largeImageKey = it) } },
                label = { Text("Large Image") },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            OutlinedTextField(
                value = state.largeImageText,
                onValueChange = { viewModel.updateField { s -> s.copy(largeImageText = it) } },
                label = { Text("Hover Text") },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.smallImageKey,
                onValueChange = { viewModel.updateField { s -> s.copy(smallImageKey = it) } },
                label = { Text("Small Image") },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            OutlinedTextField(
                value = state.smallImageText,
                onValueChange = { viewModel.updateField { s -> s.copy(smallImageText = it) } },
                label = { Text("Hover Text") },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
        }

        OutlinedTextField(
            value = state.applicationId,
            onValueChange = { viewModel.updateField { s -> s.copy(applicationId = it) } },
            label = { Text("Application ID (optional — for Dev Portal assets)") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = glassTextFieldColors()
        )
    }
}

// ── Advanced Card ──

@Composable
fun AdvancedCard(state: AppState, viewModel: MainViewModel) {
    GlassCard(accentColor = AppColors.accentGold) {
        SectionHeader("⚙️", "Advanced")

        Text("⏱ Timestamps", fontSize = 12.sp, color = AppColors.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.startTimestampStr,
                onValueChange = { viewModel.updateField { s -> s.copy(startTimestampStr = it) } },
                label = { Text("Start (epoch)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            OutlinedTextField(
                value = state.endTimestampStr,
                onValueChange = { viewModel.updateField { s -> s.copy(endTimestampStr = it) } },
                label = { Text("End (epoch)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
        }

        HorizontalDivider(color = AppColors.cardBorder, thickness = 0.5.dp)

        Text("👥 Party", fontSize = 12.sp, color = AppColors.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.partyId,
                onValueChange = { viewModel.updateField { s -> s.copy(partyId = it) } },
                label = { Text("Party ID") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            OutlinedTextField(
                value = state.partySizeStr,
                onValueChange = { viewModel.updateField { s -> s.copy(partySizeStr = it) } },
                label = { Text("Size") },
                modifier = Modifier.weight(0.5f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            OutlinedTextField(
                value = state.partyMaxStr,
                onValueChange = { viewModel.updateField { s -> s.copy(partyMaxStr = it) } },
                label = { Text("Max") },
                modifier = Modifier.weight(0.5f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
        }

        HorizontalDivider(color = AppColors.cardBorder, thickness = 0.5.dp)

        Text("🔑 Secrets", fontSize = 12.sp, color = AppColors.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.matchSecret,
                onValueChange = { viewModel.updateField { s -> s.copy(matchSecret = it) } },
                label = { Text("Match") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            OutlinedTextField(
                value = state.joinSecret,
                onValueChange = { viewModel.updateField { s -> s.copy(joinSecret = it) } },
                label = { Text("Join") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
            OutlinedTextField(
                value = state.spectateSecret,
                onValueChange = { viewModel.updateField { s -> s.copy(spectateSecret = it) } },
                label = { Text("Spectate") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = glassTextFieldColors()
            )
        }
    }
}

// ── Reusable Components ──

@Composable
fun GradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) gradient else Brush.horizontalGradient(listOf(AppColors.textMuted, AppColors.textMuted)))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
        }
    }
}

@Composable
fun SmallPillButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.card)
            .border(1.dp, AppColors.cardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.sp, color = AppColors.textSecondary)
    }
}

@Composable
fun glassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.primary,
    unfocusedBorderColor = AppColors.cardBorder,
    focusedLabelColor = AppColors.primaryLight,
    unfocusedLabelColor = AppColors.textMuted,
    cursorColor = AppColors.accent,
    focusedTextColor = AppColors.textPrimary,
    unfocusedTextColor = AppColors.textSecondary,
    disabledTextColor = AppColors.textMuted,
    disabledBorderColor = AppColors.cardBorder.copy(alpha = 0.2f),
    disabledLabelColor = AppColors.textMuted.copy(alpha = 0.4f)
)

private fun formatMemory(kb: Long): String = when {
    kb >= 1_048_576 -> "%.1f GB".format(kb / 1_048_576.0)
    kb >= 1024 -> "%.0f MB".format(kb / 1024.0)
    else -> "$kb KB"
}
