package com.example.univibe

import com.google.firebase.Timestamp

data class LostFoundItem(
    val id: String = "",
    val itemName: String = "",
    val description: String = "",
    val locationLost: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val expiryDate: Timestamp? = null,
    val status: String = "active",
    val imageUrl: String = ""
)
