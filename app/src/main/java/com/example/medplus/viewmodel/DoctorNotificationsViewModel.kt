package com.example.medplus.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.medplus.model.Notification
import com.example.medplus.repository.DashboardRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DoctorNotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DoctorNotificationsViewModel : ViewModel() {

    private val repository = DashboardRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DoctorNotificationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val uid = com.example.medplus.data.network.SessionManager.getInstance(com.google.firebase.FirebaseApp.getInstance().applicationContext).getUserId() ?: return
        Log.d("DOCTOR_NOTIFICATION_DEBUG", "Loading notifications for UID: $uid")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        repository.getNotifications(
            onSuccess = { notifications ->
                Log.d("DOCTOR_NOTIFICATION_DEBUG", "Notifications loaded: ${notifications.size}")
                _uiState.update { it.copy(notifications = notifications, isLoading = false) }
            },
            onFailure = { error ->
                Log.e("DOCTOR_NOTIFICATION_DEBUG", "Failed to load notifications: $error")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Unable to load notifications.") }
            }
        )
    }

    fun markAsRead(id: String) {
        repository.markNotificationAsRead(id)
        // Optimistic UI update
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
            )
        }
    }

    fun markAllAsRead() {
        repository.markAllNotificationsAsRead(
            onSuccess = {
                loadNotifications()
            },
            onFailure = { error ->
                Log.e("DOCTOR_NOTIFICATION_DEBUG", "Failed to mark all as read: $error")
            }
        )
    }

    fun deleteNotification(id: String) {
        android.util.Log.d("DOCTOR_NOTIFICATION_DEBUG", "Deleted notification: $id")
        repository.deleteNotification(
            id = id,
            onSuccess = {
                loadNotifications()
            },
            onFailure = { error ->
                Log.e("DOCTOR_NOTIFICATION_DEBUG", "Failed to delete notification: $error")
            }
        )
    }
}
