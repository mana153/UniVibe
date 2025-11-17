package com.example.univibe.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Data class representing a Transport sharing request or offering (e.g., carpool, ride share).
 */
data class Transport(
    @DocumentId
    val id: String = "",

    // Trip Details
    val title: String = "",
    val description: String = "",
    val origin: String = "",
    val destination: String = "",

    // Time and Status
    val departureTime: Timestamp = Timestamp.now(),
    val isOfferingRide: Boolean = true, // true: offering a ride (driver), false: requesting a ride (passenger)
    val availableSeats: Int = 1,

    // Metadata
    val postedBy: String = "", // User ID of the poster
    val contact: String = "",  // Contact information (e.g., phone, telegram handle)
    val postedAt: Timestamp = Timestamp.now()
)
// No-arg constructor not needed as all properties have defaults.