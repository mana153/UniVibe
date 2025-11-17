package com.example.univibe.models

import com.google.firebase.firestore.DocumentId

data class Transport(
    @DocumentId
    val id: String = "",
    val routeName: String = "",
    val source: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val days: List<String> = emptyList(),
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
