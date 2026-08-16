package com.example.medplus.model

import com.google.firebase.Timestamp

/**
 * Data model for patient feedback on doctors.
 */
data class DoctorFeedback(
    val doctorId: String = "",
    val patientId: String = "",
    val rating: Int = 0,
    val feedback: String = "",
    val createdAt: Timestamp? = null
)
