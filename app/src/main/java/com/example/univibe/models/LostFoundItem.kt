package com.example.univibe.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Data class representing a Lost or Found item.
 */
data class LostFoundItem(
    @DocumentId
    val id: String = "",

    val title: String = "",
    val description: String = "",
    val location: String = "",
    val postedAt: Timestamp = Timestamp.now(),
    val isLost: Boolean = true, // true: lost item, false: found item
    val postedBy: String = "", // User ID
    val imageUrl: String = "",
    val contact: String = ""
)
