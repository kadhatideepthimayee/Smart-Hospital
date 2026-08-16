package com.example.medplus.model

/**
 * Data model for a Doctor in the MedPlus system.
 * This model is used for Firestore mapping and UI display.
 */
data class Doctor(
    val uid: String = "",
    val fullName: String = "",
    val specialization: String = "",
    val department: String = "",
    val profileImage: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val status: String = "ACTIVE"
)
