package com.example.medplus.model

import com.google.firebase.Timestamp

/**
 * Data model for notifications in MedPlus.
 */
data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL", // e.g., APPOINTMENT, VERIFICATION, SYSTEM
    val isRead: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
)
