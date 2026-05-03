package com.discordrpc.gui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.discordrpc.gui.process.ProcessInfo
import com.discordrpc.gui.state.AppState
import com.discordrpc.gui.state.MainViewModel
import java.io.File
import javax.imageio.ImageIO
import androidx.compose.ui.window.WindowScope
import androidx.compose.foundation.window.WindowDraggableArea

// ── Plush Modern Components ──

@Composable
fun PlushCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val beveledEdge = Brush.verticalGradient(
        0.0f to AppTheme.colors.surfaceContainerHighest,
        1.0f to AppTheme.colors.surfaceContainerLow
    )
    val shadowColor = accentColor ?: AppTheme.colors.background

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (accentColor != null) 12.dp else 6.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(AppTheme.colors.surfaceContainer)
            .border(1.dp, beveledEdge, shape)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content
    )
}

@Composable
fun SectionHeader(icon: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.surfaceContainerHigh)
                .border(1.dp, AppTheme.colors.surfaceContainerHighest, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary
        )
    }
}

// ── Main Screen Container ──

@Composable
fun WindowScope.MainScreen(viewModel: MainViewModel, onClose: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surfaceContainerLowest.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──
            WindowDraggableArea {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.background.copy(alpha = 0.95f))
                        .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppTheme.colors.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎮", fontSize = 28.sp)
                        }
                        Column {
                            Text(
                                "Rich Presence",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                "VIBE UTILITY",
                                fontSize = 13.sp,
                                color = AppTheme.colors.secondary,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeSwitcher(state, viewModel)
                        StatusPill(state)
                        
                        // Custom Window Close Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.errorDim.copy(alpha = 0.5f))
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = AppTheme.colors.error, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // ── Layout ──
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Left Sidebar (Solid background extending down)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.3f)
                        .background(AppTheme.colors.background)
                        .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(bottomEnd = 24.dp))
                ) {
                    Sidebar(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Right Config Area
                Box(modifier = Modifier.weight(0.7f).padding(32.dp)) {
                    ConfigPane(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(state: AppState) {
    val (pillColor, label) = when {
        state.isConnecting -> AppTheme.colors.warning to "CONNECTING"
        state.isConnected -> AppTheme.colors.online to "CONNECTED"
        else -> AppTheme.colors.offline to state.statusMessage.uppercase()
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(AppTheme.colors.surfaceContainerHighest)
            .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(32.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(pillColor)
        )
        Text(
            text = label,
            color = pillColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ProcessItem(process: ProcessInfo, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) AppTheme.colors.surfaceContainerHighest else Color.Transparent
    val borderColor = if (isSelected) AppTheme.colors.secondaryContainer else Color.Transparent

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
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = process.displayName,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.surfaceContainerHighest)
                    .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    process.displayName.first().uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        // Name + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                process.displayName,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.textPrimary
            )
            Text(
                if (isSelected) "Active" else "Idle", 
                fontSize = 12.sp, 
                color = if (isSelected) AppTheme.colors.secondary else AppTheme.colors.textSecondary
            )
        }

        // Memory badge
        Text(
            formatMemory(process.memoryKb),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textMuted
        )
    }
}

// ── Reusable Components ──

@Composable
fun PlushButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    bgColor: Color = AppTheme.colors.primaryContainer,
    textColor: Color = AppTheme.colors.onPrimaryContainer,
    borderColor: Color = AppTheme.colors.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(999.dp), // fully pill shaped
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor,
            disabledContainerColor = AppTheme.colors.surfaceContainerLow,
            disabledContentColor = AppTheme.colors.textMuted
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (enabled) borderColor else AppTheme.colors.outlineVariant
        ),
        modifier = modifier
    ) {
        Text(
            text = text, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
    }
}

@Composable
fun SmallPillButton(
    text: String, 
    onClick: () -> Unit,
    color: Color = AppTheme.colors.surfaceContainerHighest,
    textColor: Color = AppTheme.colors.textPrimary
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .border(1.dp, AppTheme.colors.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
fun plushTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppTheme.colors.secondary,
    unfocusedBorderColor = AppTheme.colors.outlineVariant,
    focusedLabelColor = AppTheme.colors.secondary,
    unfocusedLabelColor = AppTheme.colors.textMuted,
    cursorColor = AppTheme.colors.secondary,
    focusedTextColor = AppTheme.colors.textPrimary,
    unfocusedTextColor = AppTheme.colors.textSecondary,
    focusedContainerColor = AppTheme.colors.surfaceContainerLowest,
    unfocusedContainerColor = AppTheme.colors.surfaceContainerLowest,
    disabledTextColor = AppTheme.colors.textMuted,
    disabledBorderColor = AppTheme.colors.outlineVariant.copy(alpha = 0.5f),
    disabledLabelColor = AppTheme.colors.textMuted.copy(alpha = 0.5f),
    disabledContainerColor = AppTheme.colors.surfaceContainerLowest
)

private fun formatMemory(kb: Long): String = when {
    kb >= 1_048_576 -> "%.1f GB".format(kb / 1_048_576.0)
    kb >= 1024 -> "%.0f MB".format(kb / 1024.0)
    else -> "$kb KB"
}

@Composable
fun ThemeSwitcher(state: AppState, viewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }

    val themes = listOf(
        "PlushAmber" to "Plush Amber",
        "ObsidianViolet" to "Obsidian Violet",
        "DiscordBlurple" to "Discord Blurple",
        "NeonCyber" to "Neon Cyber",
        "CrimsonForge" to "Crimson Forge",
        "Custom" to "Custom (Hex)"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.activeThemeId == "Custom") {
            OutlinedTextField(
                value = state.customThemeHex,
                onValueChange = { viewModel.changeTheme("Custom", it) },
                label = { Text("Hex Color") },
                modifier = Modifier.width(120.dp),
                singleLine = true,
                colors = plushTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Box {
            SmallPillButton(
                text = "🎨 Theme",
                onClick = { expanded = true }
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AppTheme.colors.surfaceContainerHighest)
            ) {
                themes.forEach { (id, label) ->
                    DropdownMenuItem(
                        text = { Text(label, color = AppTheme.colors.textPrimary) },
                        onClick = {
                            viewModel.changeTheme(id, state.customThemeHex)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
