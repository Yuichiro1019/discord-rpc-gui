package com.discordrpc.gui.rpc

import com.discordrpc.gui.state.AppState
import dev.cbyrne.kdiscordipc.KDiscordIPC
import dev.cbyrne.kdiscordipc.core.event.impl.ReadyEvent
import dev.cbyrne.kdiscordipc.data.activity.Activity
import kotlinx.coroutines.*

class KDiscordRpcServiceImpl : DiscordRpcService {
    private var ipc: KDiscordIPC? = null
    private var connectionJob: Job? = null

    override suspend fun connect(clientId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val newIpc = KDiscordIPC(clientId)
            val readyDeferred = CompletableDeferred<Boolean>()

            // Register the ReadyEvent handler BEFORE connecting (required by KDiscordIPC)
            newIpc.on<ReadyEvent> {
                readyDeferred.complete(true)
            }

            // connect() blocks forever (it collects a flow), so launch it separately
            connectionJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    newIpc.connect()
                } catch (t: Throwable) {
                    readyDeferred.complete(false)
                }
            }

            // Wait up to 10 seconds for Discord to respond with ReadyEvent
            val ready = withTimeoutOrNull(10_000) { readyDeferred.await() } ?: false
            if (ready) {
                ipc = newIpc
                true
            } else {
                connectionJob?.cancel()
                connectionJob = null
                false
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                ipc?.disconnect()
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                connectionJob?.cancel()
                connectionJob = null
                ipc = null
            }
        }
    }

    override suspend fun updatePresence(state: AppState) = withContext(Dispatchers.IO) {
        val currentIpc = ipc ?: return@withContext

        try {
            val activity = Activity(
                state = state.state.takeIf { it.isNotBlank() },
                details = state.details.takeIf { it.isNotBlank() },
                timestamps = if (state.startTimestamp != null || state.endTimestamp != null) {
                    Activity.Timestamps(
                        start = state.startTimestamp ?: 0L,
                        end = state.endTimestamp ?: 0L
                    )
                } else null,
                assets = if (state.largeImageKey.isNotBlank() || state.smallImageKey.isNotBlank()) {
                    Activity.Assets(
                        largeImage = state.largeImageKey.takeIf { it.isNotBlank() },
                        largeText = state.largeImageText.takeIf { it.isNotBlank() },
                        smallImage = state.smallImageKey.takeIf { it.isNotBlank() },
                        smallText = state.smallImageText.takeIf { it.isNotBlank() }
                    )
                } else null,
                party = if (state.partyId.isNotBlank() || state.partySize != null) {
                    Activity.Party(
                        id = state.partyId.takeIf { it.isNotBlank() } ?: "",
                        size = if (state.partySize != null && state.partyMax != null)
                            Activity.Party.PartySize(state.partySize!!, state.partyMax!!)
                        else Activity.Party.PartySize(0, 0)
                    )
                } else null,
                secrets = if (state.matchSecret.isNotBlank() || state.joinSecret.isNotBlank() || state.spectateSecret.isNotBlank()) {
                    Activity.Secrets(
                        match = state.matchSecret.takeIf { it.isNotBlank() },
                        join = state.joinSecret.takeIf { it.isNotBlank() },
                        spectate = state.spectateSecret.takeIf { it.isNotBlank() }
                    )
                } else null
            )

            currentIpc.activityManager.setActivity(activity)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
