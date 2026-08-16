package com.example.medplus.model

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

/**
 * Data model for a patient appointment in MedPlus.
 */
data class Appointment(
    @SerializedName("_id") val appointmentId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val department: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "UPCOMING",
    val tokenNumber: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val timestamp: Timestamp? = null, // For sorting and filtering
    val consultationStartedAt: String? = null,
    val consultationCompletedAt: String? = null
)
