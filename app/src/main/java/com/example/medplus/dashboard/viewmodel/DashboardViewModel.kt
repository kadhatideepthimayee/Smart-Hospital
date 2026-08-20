package com.example.medplus.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medplus.repository.DashboardRepository
import com.example.medplus.dashboard.model.PatientDashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    private val _uiState = MutableStateFlow(PatientDashboardUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private var dashboardJob: kotlinx.coroutines.Job? = null

    fun loadDashboardData() {
        dashboardJob?.cancel()
        val uid = com.example.medplus.data.network.SessionManager.getInstance(com.google.firebase.FirebaseApp.getInstance().applicationContext).getUserId()
        android.util.Log.d("PATIENT_APPOINTMENTS_DEBUG", "Loading dashboard data for patient UID: $uid")

        dashboardJob = viewModelScope.launch {
            // 1. Load User Profile
            repository.getCurrentUser(
                onSuccess = { user ->
                    _uiState.update { it.copy(
                        patientName = user.fullName,
                        email = user.email,
                        phone = user.phone,
                        role = user.role,
                        profileImageUrl = user.profileImage,
                        isLoading = false
                    ) }
                },
                onFailure = { error ->
                    android.util.Log.e("DashboardVM", "Failed to load user profile: $error")
                    _uiState.update { it.copy(
                        errorMessage = "Unable to load dashboard data. Please check your connection. Error: $error",
                        isLoading = false
                    ) }
                }
            )

            // 2. Load Notification Count
            repository.getUnreadNotificationCount(
                onSuccess = { count ->
                    _uiState.update { it.copy(unreadNotificationCount = count) }
                },
                onFailure = { /* Silent fail for secondary data */ }
            )

            // 3. Listen for Upcoming Appointment (Real-time)
            launch {
                repository.getUpcomingAppointmentUpdates().collect { appointment ->
                    android.util.Log.d("PATIENT_APPOINTMENTS_DEBUG", "Upcoming appointment update: ${appointment?.appointmentId ?: "None"}")
                    if (appointment != null) {
                        android.util.Log.d("PATIENT_APPOINTMENTS_DEBUG", "Fetched: ID=${appointment.appointmentId}, Doctor=${appointment.doctorName}, Date=${appointment.date}, Time=${appointment.time}, Status=${appointment.status}")
                    }
                    _uiState.update { it.copy(upcomingAppointment = appointment) }
                }
            }

            // 4. Load Recent Activities
            repository.getRecentActivities(
                onSuccess = { activities ->
                    _uiState.update { it.copy(recentActivity = activities) }
                },
                onFailure = { /* Silent fail */ }
            )

            // 5. Listen for Live Queue Updates (Real-time)
            launch {
                repository.getLiveQueueUpdates().collect { queueInfo ->
                    _uiState.update { it.copy(liveQueue = queueInfo) }
                }
            }
        }
    }

    // Keep for backward compatibility or if needed by other screens
    fun loadCurrentUser() {
        loadDashboardData()
    }

    fun updateUserProfile(fullName: String, phone: String, onResult: (Boolean) -> Unit) {
        repository.updateUserProfile(
            fullName = fullName,
            phone = phone,
            onSuccess = {
                loadDashboardData()
                onResult(true)
            },
            onFailure = {
                onResult(false)
            }
        )
    }
}
