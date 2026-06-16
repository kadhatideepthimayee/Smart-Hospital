package com.example.smarthospitalqueue.ui.screens.patient

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.example.smarthospitalqueue.utils.*
import com.google.firebase.auth.FirebaseAuth

object DashboardMockData {

    fun patient(): PatientInfo {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return PatientInfo(
            id = "USR-20248821",
            name = currentUser?.displayName ?: "Deepthi Krishnan",
            avatarInitials = "DK",
            profileInitials = "DK",
            patientId = "HOSP-20248821",
            bloodGroup = "B+",
            hospitalName = "Apollo Smart Hospital"
        )
    }

    fun queueStatus() = QueueStatus(
        isActive = true,
        tokenNumber = 47,
        currentServing = 39,
        totalAhead = 8,
        estimatedWaitMinutes = 24,
        department = "Cardiology",
        doctorName = "Dr. Priya Venkatesh",
        roomNumber = "OPD-3B",
        queueProgress = 0.82f,
        status = QueueStatusType.ALMOST_THERE,
        isLoading = false
    )

    fun aiPrediction() = AiPrediction(
        message = "Best visiting time today: 2 PM – 3 PM",
        predictedWaitMinutes = 22,
        confidencePercent = 94,
        peakHour = "10:30 AM – 12:00 PM",
        recommendedSlot = "2:15 PM",
        congestionLevel = CongestionLevel.MODERATE,
        historicalAccuracyPercent = 91,
    )

    fun upcomingAppointment() = UpcomingAppointment(
        id = "APT-9934",
        doctorName = "Dr. Ramesh Iyer",
        specialization = "Neurologist",
        date = "Today, 28 May",
        time = "10:00 AM",
        roomNumber = "OPD-3B",
        appointmentType = AppointmentType.IN_PERSON,
        status = AppointmentStatus.UPCOMING,
    )

    fun notifications() = NotificationBadge(count = 3, hasUrgent = true)

    fun quickActions() = listOf(
        QuickAction("book", "Book Appointment", "New slot", Icons.Outlined.CalendarMonth, "book_appointment", accentColorHex = 0xFF0EA5E9),
        QuickAction("track", "Queue Tracking", "Live status", Icons.Outlined.TrackChanges, "queue_tracking", accentColorHex = 0xFF10B981),
        QuickAction("ai", "AI Prediction", "Smart wait", Icons.Outlined.AutoAwesome, "ai_prediction", accentColorHex = 0xFF8B5CF6),
        QuickAction("token", "Digital Token", "Your pass", Icons.Outlined.ConfirmationNumber, "digital_token", accentColorHex = 0xFFF59E0B),
        QuickAction("notif", "Notifications", "3 new", Icons.Outlined.Notifications, "notifications", badgeCount = 3, accentColorHex = 0xFFEF4444),
        QuickAction("pay", "Payments", "Billing", Icons.Outlined.CreditCard, "payments", accentColorHex = 0xFF06B6D4),
    )

    fun greeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning 👋"
            hour < 17 -> "Good Afternoon 👋"
            else -> "Good Evening 👋"
        }
    }

    fun successState() = DashboardUiState.Success(
        patient = patient(),
        queueStatus = queueStatus(),
        aiPrediction = aiPrediction(),
        upcomingAppointment = upcomingAppointment(),
        notifications = notifications(),
        quickActions = quickActions(),
        greeting = greeting(),
    )
}
