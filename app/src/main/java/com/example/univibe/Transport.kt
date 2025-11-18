package com.example.univibe

import com.google.firebase.Timestamp

data class Transport(
    val id: String = "",
    val routeName: String = "",
    val source: String = "",
    val destination: String = "",
    val departureTime: Timestamp? = null,
    val arrivalTime: Timestamp? = null,
    val days: List<String> = emptyList(),
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: Timestamp? = null
)
