package com.discordrpc.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val discordRPCClient = remember { DiscordRPCClient() }
    var isConnected by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf("Using Discord RPC GUI") }
    var state by remember { mutableStateOf("Idle") }
    var largeImage by remember { mutableStateOf("default") }
    var largeText by remember { mutableStateOf("Kotlin Discord RPC") }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            discordRPCClient.connect()
        } else {
            discordRPCClient.disconnect()
        }
    }

    LaunchedEffect(details, state, largeImage, largeText) {
        if (isConnected) {
            discordRPCClient.updatePresence(details, state, largeImage, largeText)
        }
    }

    Window(
        onCloseRequest = {
            discordRPCClient.cleanup()
            exitApplication()
        },
        title = "Discord RPC GUI - Kotlin",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen(
                    isConnected = isConnected,
                    onConnectToggle = { isConnected = it },
                    details = details,
                    onDetailsChange = { details = it },
                    state = state,
                    onStateChange = { state = it },
                    largeImage = largeImage,
                    onLargeImageChange = { largeImage = it },
                    largeText = largeText,
                    onLargeTextChange = { largeText = it },
                    onUpdatePresence = {
                        discordRPCClient.updatePresence(details, state, largeImage, largeText)
                    },
                    onAddButtons = {
                        discordRPCClient.updatePresenceWithButtons(
                            details = "Custom Buttons",
                            state = "Click below!",
                            buttons = listOf(
                                "GitHub" to "https://github.com",
                                "Discord" to "https://discord.gg"
                            )
                        )
                    },
                    onUpdateParty = {
                        discordRPCClient.updatePartyInfo("party-123", 1, 10)
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    isConnected: Boolean,
    onConnectToggle: (Boolean) -> Unit,
    details: String,
    onDetailsChange: (String) -> Unit,
    state: String,
    onStateChange: (String) -> Unit,
    largeImage: String,
    onLargeImageChange: (String) -> Unit,
    largeText: String,
    onLargeTextChange: (String) -> Unit,
    onUpdatePresence: () -> Unit,
    onAddButtons: () -> Unit,
    onUpdateParty: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Discord RPC GUI",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Connection Status",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isConnected) "✅ Connected" else "❌ Disconnected",
                        color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )

                    Switch(
                        checked = isConnected,
                        onCheckedChange = onConnectToggle
                    )
                }

                if (isConnected) {
                    Text(
                        text = "Client ID: 1491387013067182191",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Rich Presence Settings",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = onDetailsChange,
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What you're doing...") }
                )

                OutlinedTextField(
                    value = state,
                    onValueChange = onStateChange,
                    label = { Text("State") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Additional info...") }
                )

                OutlinedTextField(
                    value = largeImage,
                    onValueChange = onLargeImageChange,
                    label = { Text("Large Image Key") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("default") }
                )

                OutlinedTextField(
                    value = largeText,
                    onValueChange = onLargeTextChange,
                    label = { Text("Large Image Text") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Hover text...") }
                )

                Button(
                    onClick = onUpdatePresence,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isConnected
                ) {
                    Text("Update Presence")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = onAddButtons,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isConnected
                ) {
                    Text("Add Buttons to Presence")
                }

                Button(
                    onClick = onUpdateParty,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isConnected
                ) {
                    Text("Update Party Info")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Built with Kotlin + Compose + kdiscordrpc",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Preview function removed as it's causing build issues
// This can be re-enabled once the build is working
// @Preview
// @Composable
// fun MainScreenPreview() {
//     MaterialTheme {
//         MainScreen(
//             isConnected = true,
//             onConnectToggle = {},
//             details = "Using Discord RPC GUI",
//             onDetailsChange = {},
//             state = "Idle",
//             onStateChange = {},
//             largeImage = "default",
//             onLargeImageChange = {},
//             largeText = "Kotlin Discord RPC",
//             onLargeTextChange = {},
//             onUpdatePresence = {},
//             onAddButtons = {},
//             onUpdateParty = {}
//         )
//     }
// }
