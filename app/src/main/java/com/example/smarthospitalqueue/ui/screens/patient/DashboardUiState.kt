package com.example.smarthospitalqueue.ui.screens.patient

import com.example.smarthospitalqueue.utils.*

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val patient: Patient,
        val queueStatus: QueueStatus,
        val aiPrediction: AiPrediction,
        val upcomingAppointment: UpcomingAppointment?,
        val notifications: NotificationBadge,
        val quickActions: List<QuickAction>,
        val greeting: String,
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}
