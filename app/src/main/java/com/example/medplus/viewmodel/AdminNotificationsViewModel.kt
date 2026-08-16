package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.AdminNotification
import com.example.medplus.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdminNotificationsUiState(
    val notifications: List<AdminNotification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminNotificationsViewModel : ViewModel() {

    private val repository = AdminRepository()

    private val _uiState = MutableStateFlow(AdminNotificationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getAdminNotifications(
            onSuccess = { notifications ->
                _uiState.update { it.copy(notifications = notifications, isLoading = false) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun markAsRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun deleteNotification(id: String) {
        android.util.Log.d("ADMIN_NOTIFICATION_DEBUG", "Deleted notification: $id")
        repository.deleteNotification(
            id = id,
            onSuccess = {
                loadNotifications() // Refresh list
            },
            onFailure = { error ->
                _uiState.update { it.copy(errorMessage = error) }
            }
        )
    }
}
