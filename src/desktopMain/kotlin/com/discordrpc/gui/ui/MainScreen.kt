package com.discordrpc.gui.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.discordrpc.gui.state.AppState
import com.discordrpc.gui.state.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discord RPC Control Panel") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConnectionSection(state, viewModel)
            HorizontalDivider()
            BasicInfoSection(state, viewModel)
            HorizontalDivider()
            ImagesSection(state, viewModel)
            HorizontalDivider()
            TimestampsSection(state, viewModel)
            HorizontalDivider()
            PartyAndSecretsSection(state, viewModel)
        }
    }
}

@Composable
fun ConnectionSection(state: AppState, viewModel: MainViewModel) {
    Text("Connection", style = MaterialTheme.typography.titleMedium)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.clientId,
            onValueChange = { viewModel.updateField { s -> s.copy(clientId = it) } },
            label = { Text("Client ID") },
            enabled = !state.isConnected,
            modifier = Modifier.weight(1f)
        )
        if (state.isConnected) {
            Button(
                onClick = { viewModel.disconnect() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Disconnect")
            }
            Button(
                onClick = { viewModel.updatePresence() }
            ) {
                Text("Update Presence")
            }
        } else {
            Button(
                onClick = { viewModel.connect() }
            ) {
                Text("Connect")
            }
        }
    }
    Text(
        text = if (state.isConnected) "Status: Connected" else "Status: Disconnected",
        color = if (state.isConnected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun BasicInfoSection(state: AppState, viewModel: MainViewModel) {
    Text("Basic Info", style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = state.details,
        onValueChange = { viewModel.updateField { s -> s.copy(details = it) } },
        label = { Text("Details (Row 1)") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = state.state,
        onValueChange = { viewModel.updateField { s -> s.copy(state = it) } },
        label = { Text("State (Row 2)") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ImagesSection(state: AppState, viewModel: MainViewModel) {
    Text("Images", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.largeImageKey,
            onValueChange = { viewModel.updateField { s -> s.copy(largeImageKey = it) } },
            label = { Text("Large Image Key") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.largeImageText,
            onValueChange = { viewModel.updateField { s -> s.copy(largeImageText = it) } },
            label = { Text("Large Image Text") },
            modifier = Modifier.weight(1f)
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.smallImageKey,
            onValueChange = { viewModel.updateField { s -> s.copy(smallImageKey = it) } },
            label = { Text("Small Image Key") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.smallImageText,
            onValueChange = { viewModel.updateField { s -> s.copy(smallImageText = it) } },
            label = { Text("Small Image Text") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TimestampsSection(state: AppState, viewModel: MainViewModel) {
    Text("Timestamps (Unix Epoch)", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.startTimestampStr,
            onValueChange = { viewModel.updateField { s -> s.copy(startTimestampStr = it) } },
            label = { Text("Start Timestamp") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.endTimestampStr,
            onValueChange = { viewModel.updateField { s -> s.copy(endTimestampStr = it) } },
            label = { Text("End Timestamp") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PartyAndSecretsSection(state: AppState, viewModel: MainViewModel) {
    Text("Party & Secrets", style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(
        value = state.partyId,
        onValueChange = { viewModel.updateField { s -> s.copy(partyId = it) } },
        label = { Text("Party ID") },
        modifier = Modifier.fillMaxWidth()
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.partySizeStr,
            onValueChange = { viewModel.updateField { s -> s.copy(partySizeStr = it) } },
            label = { Text("Party Size") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.partyMaxStr,
            onValueChange = { viewModel.updateField { s -> s.copy(partyMaxStr = it) } },
            label = { Text("Party Max") },
            modifier = Modifier.weight(1f)
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.matchSecret,
            onValueChange = { viewModel.updateField { s -> s.copy(matchSecret = it) } },
            label = { Text("Match Secret") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = state.joinSecret,
            onValueChange = { viewModel.updateField { s -> s.copy(joinSecret = it) } },
            label = { Text("Join Secret") },
            modifier = Modifier.weight(1f)
        )
    }
    OutlinedTextField(
        value = state.spectateSecret,
        onValueChange = { viewModel.updateField { s -> s.copy(spectateSecret = it) } },
        label = { Text("Spectate Secret") },
        modifier = Modifier.fillMaxWidth()
    )
}
