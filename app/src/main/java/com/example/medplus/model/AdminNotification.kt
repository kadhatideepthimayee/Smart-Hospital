package com.example.medplus.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

/**
 * Data model for notifications sent to admins.
 */
data class AdminNotification(
    @get:Exclude val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // e.g., "DOCTOR_VERIFICATION"
    val doctorId: String = "",
    val isRead: Boolean = false,
    val timestamp: Timestamp? = null
)
