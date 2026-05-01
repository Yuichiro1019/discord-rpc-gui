package com.discordrpc.gui

import dev.cbyrne.kdiscordipc.KDiscordIPC
import dev.cbyrne.kdiscordipc.core.event.impl.ReadyEvent
import dev.cbyrne.kdiscordipc.core.event.impl.DisconnectedEvent
import dev.cbyrne.kdiscordipc.data.activity.Activity
import dev.cbyrne.kdiscordipc.data.activity.Activity.Party
import dev.cbyrne.kdiscordipc.data.activity.Activity.Timestamps
import dev.cbyrne.kdiscordipc.data.activity.Activity.Assets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class DiscordRPCClient(private val clientId: String = "922036277996556318") {
    private var discordIPC: KDiscordIPC? = null
    private var isConnected: Boolean = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun connect() {
        if (isConnected) return

        discordIPC = KDiscordIPC(clientId)
        
        // Use the events flow to handle events
        coroutineScope.launch {
            discordIPC?.events?.filterIsInstance<ReadyEvent>()?.onEach { event ->
                println("✅ Discord RPC Connected - ${event.data.user.username}")
            }?.launchIn(this)
            
            discordIPC?.events?.filterIsInstance<DisconnectedEvent>()?.onEach {
                println("❌ Discord RPC Disconnected")
            }?.launchIn(this)
        }
        
        coroutineScope.launch {
            discordIPC?.connect()
        }
        isConnected = true

        updatePresence(
            details = "Using Discord RPC GUI",
            state = "Idle",
            largeImage = "default",
            largeText = "Kotlin Discord RPC"
        )

        println("✅ Discord RPC Connected - Client ID: $clientId")
    }

    fun disconnect() {
        if (!isConnected) return

        discordIPC?.disconnect()
        discordIPC = null
        isConnected = false

        println("❌ Discord RPC Disconnected")
    }

    fun updatePresence(
        details: String = "",
        state: String = "",
        largeImage: String = "",
        largeText: String = "",
        smallImage: String = "",
        smallText: String = ""
    ) {
        if (!isConnected) return

        coroutineScope.launch {
            val activity = Activity(
                details = details.ifEmpty { null },
                state = state.ifEmpty { null },
                timestamps = Timestamps(System.currentTimeMillis(), null),
                assets = Assets(
                    largeImage = largeImage.ifEmpty { null },
                    largeText = largeText.ifEmpty { null },
                    smallImage = smallImage.ifEmpty { null },
                    smallText = smallText.ifEmpty { null }
                )
            )
            
            discordIPC?.activityManager?.setActivity(activity)
            println("📝 Presence Updated: $details - $state")
        }
    }

    fun updatePresenceWithButtons(
        details: String,
        state: String,
        buttons: List<Pair<String, String>>
    ) {
        if (!isConnected) return

        coroutineScope.launch {
            val activity = Activity(
                details = details.ifEmpty { null },
                state = state.ifEmpty { null },
                timestamps = Timestamps(System.currentTimeMillis(), null),
                buttons = buttons.map { (label, url) ->
                    Activity.Button(label, url)
                }.toMutableList()
            )
            
            discordIPC?.activityManager?.setActivity(activity)
            println("🔘 Presence with Buttons Updated")
        }
    }

    fun updatePartyInfo(
        partyId: String = "party-123",
        currentSize: Int = 1,
        maxSize: Int = 10
    ) {
        if (!isConnected) return

        coroutineScope.launch {
            val activity = Activity(
                details = "In a party",
                state = "$currentSize/$maxSize players",
                timestamps = Timestamps(System.currentTimeMillis(), null),
                party = Party(partyId, Party.PartySize(currentSize, maxSize))
            )
            
            discordIPC?.activityManager?.setActivity(activity)
            println("👥 Party Info Updated")
        }
    }

    fun isRPCConnected(): Boolean = isConnected

    fun cleanup() {
        disconnect()
        println("🧹 RPC Client Cleaned Up")
    }
}
