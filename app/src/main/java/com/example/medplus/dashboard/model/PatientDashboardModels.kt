package com.example.medplus.dashboard.model

data class PatientDashboardUiState(
    val patientName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val profileImageUrl: String? = null,
    val unreadNotificationCount: Int = 0,
    val crowdLevel: CrowdLevel = CrowdLevel.LOW,
    val estimatedWaitMinutes: Int = 0,
    val upcomingAppointment: UpcomingAppointment? = null,
    val liveQueue: LiveQueueInfo? = null,
    val healthSummary: HealthSummary? = null,
    val recentActivity: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class CrowdLevel {
    LOW, MEDIUM, HIGH
}

data class UpcomingAppointment(
    val appointmentId: String,
    val doctorName: String,
    val department: String,
    val status: String,
    val date: String,
    val time: String
)

data class LiveQueueInfo(
    val isActive: Boolean = false,
    val queueNumber: String = "",
    val currentServingToken: String = "",
    val status: String = "",
    val patientsAhead: Int = 0,
    val estimatedWaitMinutes: Int = 0,
    val crowdLevel: CrowdLevel = CrowdLevel.LOW,
    val department: String? = null
)

data class HealthSummary(
    val hasData: Boolean,
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList()
)

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: String
)

enum class ActivityType {
    QUEUE, APPOINTMENT, RECORD, GENERAL
}
