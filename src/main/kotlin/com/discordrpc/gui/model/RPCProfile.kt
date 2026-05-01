package com.discordrpc.gui.model

import kotlinx.serialization.Serializable

/**
 * Represents a button for Discord Rich Presence.
 *
 * @param label The text displayed on the button
 * @param url The URL the button links to
 */
@Serializable
data class ProfileButton(
    val label: String,
    val url: String
)

/**
 * Represents a complete Discord Rich Presence profile.
 * Contains all fields supported by Discord RPC including metadata.
 *
 * @param profileId Unique identifier for the profile
 * @param profileName Human-readable name for the profile
 * @param executable The executable name or path this profile is for
 * @param applicationName Optional application name override
 * @param details Primary status text (what the user is doing)
 * @param state Secondary status text (additional context)
 * @param largeImage Image key for large image
 * @param largeText Tooltip text for large image
 * @param smallImage Image key for small image
 * @param smallText Tooltip text for small image
 * @param buttons List of buttons with label/url pairs
 * @param partyId Optional party identifier
 * @param currentSize Current number of players in party
 * @param maxSize Maximum party size
 * @param startTimestamp Unix timestamp for activity start
 * @param endTimestamp Unix timestamp for activity end
 * @param createdAt Unix timestamp when profile was created
 * @param updatedAt Unix timestamp when profile was last updated
 */
@Serializable
data class RPCProfile(
    val profileId: String,
    val profileName: String,
    val executable: String,
    val applicationName: String? = null,
    val details: String = "",
    val state: String = "",
    val largeImage: String = "",
    val largeText: String = "",
    val smallImage: String = "",
    val smallText: String = "",
    val buttons: List<ProfileButton> = emptyList(),
    val partyId: String? = null,
    val currentSize: Int? = null,
    val maxSize: Int? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Creates a copy of this profile with updated timestamp.
     */
    fun withUpdatedTimestamp(): RPCProfile {
        return copy(updatedAt = System.currentTimeMillis())
    }
}
