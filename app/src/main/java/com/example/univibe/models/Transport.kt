package com.example.univibe.models

import com.google.firebase.Timestamp

data class Transport(
    val id: String = "",
    val routename: String = "",
    val source: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val arrivalTime: String? = null,
    val dayType: String = "",
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: Timestamp? = null
)
