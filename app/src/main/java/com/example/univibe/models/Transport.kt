package com.example.univibe.models

import com.google.firebase.firestore.DocumentId

data class Transport(
    val id: String = "",
    val routename: String = "",  // Note: lowercase 'n' in your DB
    val source: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val arrivalTime: String? = null,
    val dayType: String = "",  // "Daily" or "Saturday"
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)