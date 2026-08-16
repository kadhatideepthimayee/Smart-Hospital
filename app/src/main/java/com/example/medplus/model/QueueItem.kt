package com.example.medplus.model

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

/**
 * Data model for an entry in the patient queue.
 */
data class QueueItem(
    @SerializedName("_id") val queueId: String = "",
    val appointmentId: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val tokenNumber: String = "",
    val status: String = "WAITING", // WAITING, IN_PROGRESS, COMPLETED, CANCELLED
    val department: String? = null,
    val date: String = "",
    val isActive: Boolean = true,
    val timestamp: Timestamp = Timestamp.now(),
    val estimatedWaitMinutes: Int = 0,
    val consultationStartedAt: String? = null,
    val consultationCompletedAt: String? = null
)
