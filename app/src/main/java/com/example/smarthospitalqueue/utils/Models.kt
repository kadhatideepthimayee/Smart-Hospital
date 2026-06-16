package com.example.smarthospitalqueue.utils

import androidx.compose.ui.graphics.vector.ImageVector

data class Doctor(
    val name: String,
    val specialization: String,
    val roomNumber: String,
    val rating: Double = 4.5,
    val experienceYears: Int = 10,
    val experience: Int = 10,
    val availability: String = "Available Today",
    val available: Boolean = true,
    val avatarInitials: String = "DR",
    val qualification: String = "MBBS, MD",
    val department: String = "General"
)

data class Department(
    val name: String,
    val iconRes: Int = 0,
    val icon: String = "🏥",
    val doctorCount: Int = 0,
    val congestionLevel: CongestionLevel = CongestionLevel.LOW,
    val avgWait: Int = 15
)

data class TimeSlot(
    val time: String,
    val isAvailable: Boolean = true,
    val available: Boolean = true,
    val aiRecommended: Boolean = false
)

data class Appointment(
    val doctor: Doctor,
    val department: String,
    val date: String,
    val time: String,
    val estimatedWait: Int,
    val consultationFee: Int,
    val tokenNumber: String
)

enum class RecordType {
    PRESCRIPTION, LAB_REPORT, CONSULTATION, SCAN
}

data class MedicalRecord(
    val type: RecordType,
    val date: String,
    val doctorName: String,
    val department: String,
    val description: String,
    val fileSize: String? = null
)

data class Patient(
    val id: String,
    val name: String,
    val profileInitials: String,
    val patientId: String = "",
    val bloodGroup: String,
    val age: Int = 25,
    val gender: String = "Female",
    val phone: String = "+91 98765 43210",
    val email: String = "patient@example.com",
    val address: String = "123, Park Street, Chennai",
    val emergencyContact: String = "Ravi (Husband) - 98765 00000",
    val avatarInitials: String = "P",
    val hospitalName: String = "Apollo Smart Hospital"
)

// Legacy alias for Dashboard
typealias PatientInfo = Patient

data class QueueStatus(
    val isActive: Boolean = true,
    val currentToken: String = "A-39",
    val patientToken: String = "A-47",
    val peopleAhead: Int = 8,
    val totalInQueue: Int = 45,
    val tokenNumber: Int = 47,
    val currentServing: Int = 39,
    val totalAhead: Int = 8,
    val estimatedWaitMinutes: Int = 24,
    val department: String,
    val doctorName: String,
    val roomNumber: String = "OPD-1",
    val queueProgress: Float,
    val status: QueueStatusType,
    val isLoading: Boolean = false
)

enum class QueueStatusType { WAITING, ALMOST_THERE, YOUR_TURN, PAUSED, NONE }

data class AiPrediction(
    val message: String = "",
    val predictedWaitMinutes: Int,
    val confidencePercent: Int,
    val peakHour: String,
    val recommendedSlot: String,
    val congestionLevel: CongestionLevel,
    val historicalAccuracyPercent: Int,
)

enum class CongestionLevel { LOW, MODERATE, HIGH, CRITICAL }

data class UpcomingAppointment(
    val id: String,
    val doctorName: String,
    val specialization: String,
    val date: String,
    val time: String,
    val roomNumber: String,
    val appointmentType: AppointmentType,
    val status: AppointmentStatus,
)

enum class AppointmentType { IN_PERSON, TELECONSULT }
enum class AppointmentStatus { CONFIRMED, PENDING, RESCHEDULED, UPCOMING, IN_PROGRESS, COMPLETED, CANCELLED }

data class NotificationBadge(
    val count: Int,
    val hasUrgent: Boolean,
)

data class QuickAction(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0,
    val accentColorHex: Long,
)

enum class NotificationType {
    QUEUE, APPOINTMENT, EMERGENCY, PAYMENT, ANNOUNCEMENT
}

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class PaymentStatus {
    PAID, PENDING, FAILED
}

data class Payment(
    val id: String,
    val description: String,
    val amount: Int,
    val date: String,
    val invoiceId: String,
    val status: PaymentStatus
)
