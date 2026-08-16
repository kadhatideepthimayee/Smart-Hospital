package com.example.medplus.model

import com.google.firebase.Timestamp

/**
 * Detailed professional profile for a Doctor, including verification status.
 */
data class DoctorProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val qualification: String = "",
    val department: String = "",
    val specialization: String = "",
    val experienceYears: Int = 0,
    val registrationAuthority: String = "",
    val registrationNumber: String = "",
    val consultationFee: Double = 0.0,
    val bio: String = "",
    val profileImage: String = "",
    val registrationCertificateUrl: String = "",
    val verificationDocumentUrl: String = "",
    val workingDays: List<String> = emptyList(),
    val consultationStartTime: String = "",
    val consultationEndTime: String = "",
    val lunchStartTime: String = "",
    val lunchEndTime: String = "",
    val breakStartTime: String = "",
    val breakEndTime: String = "",
    val slotDuration: Int = 15,
    val verificationStatus: String = "DRAFT",
    val submittedAt: Timestamp = Timestamp.now(),
    val reviewedAt: Timestamp? = null,
    val reviewedBy: String? = null,
    val rejectionReason: String? = null
)
