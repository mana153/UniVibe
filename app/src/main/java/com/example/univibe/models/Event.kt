package com.example.univibe.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",  // or use Timestamp
    val timestamp: com.google.firebase.Timestamp? = null,
    val location: String = "",
    val category: String = "",
    val createdBy: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)