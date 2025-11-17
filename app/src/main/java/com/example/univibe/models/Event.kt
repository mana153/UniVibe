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
    val date: Timestamp = Timestamp.now(),

    // Metadata
    val createdBy: String = "", // User ID of the creator
    val category: String = "", // e.g., "Sports", "Academic", "Social"
    val createdAt: Timestamp = Timestamp.now()
)
