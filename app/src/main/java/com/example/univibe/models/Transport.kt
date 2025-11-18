package com.example.univibe.models
data class Transport(
    var id: String = "",
    val routename: String = "",
    val source: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val dayType: String = "",
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: Long = 0
)

