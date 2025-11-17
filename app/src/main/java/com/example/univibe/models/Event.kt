package com.example.univibe.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Data class representing an Event document in Firestore.
 */
data class Event(
    @DocumentId
    val id: String = "",

    // Main Event Details
    val title: String = "",
    val description: String = "",

    // Event Time/Date
    val date: Timestamp? = null,

    // Location
    val location: String = "",

    // Metadata
    val createdBy: String = "", // User ID of the creator
    val category: String = "", // e.g., "Events", "Transport", "Lost & Found"
    val createdAt: Timestamp? = null
)
