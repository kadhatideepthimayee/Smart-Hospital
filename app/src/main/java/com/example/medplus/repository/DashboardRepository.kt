package com.example.medplus.repository

import com.example.medplus.auth.model.User
import com.example.medplus.data.network.*
import com.example.medplus.dashboard.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = RetrofitClient.getApiService(context)
    private val sessionManager = SessionManager.getInstance(context)

    fun getCurrentUser(
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getMe()
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body()!!)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to get current user details")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to get current user")
                }
            }
        }
    }

    /**
     * Update user profile in MongoDB
     */
    fun updateUserProfile(
        fullName: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UpdateProfileRequest(fullName, phone)
                val response = apiService.updateProfile(request)
                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    sessionManager.updateProfile(updatedUser.fullName, updatedUser.phone)
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to update profile details")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to update profile")
                }
            }
        }
    }

    /**
     * Get unread notification count for the current user
     */
    fun getUnreadNotificationCount(
        onSuccess: (Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getUnreadNotificationCount()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body()?.count ?: 0)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch unread notification count")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to get unread notification count")
                }
            }
        }
    }

    /**
     * Get all notifications for the current user
     */
    fun getNotifications(
        onSuccess: (List<com.example.medplus.model.Notification>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getNotifications()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch notifications")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch notifications")
                }
            }
        }
    }

    /**
     * Mark a notification as read
     */
    fun markNotificationAsRead(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.markNotificationRead(id)
            } catch (e: Exception) {
                android.util.Log.e("DashboardRepository", "Failed to mark notification read", e)
            }
        }
    }

    /**
     * Delete a notification
     */
    fun deleteNotification(id: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.deleteNotification(id)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to delete notification")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to delete notification")
                }
            }
        }
    }

    /**
     * Mark all notifications as read for current user
     */
    fun markAllNotificationsAsRead(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.markAllNotificationsRead()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to mark all as read")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to mark all as read")
                }
            }
        }
    }

    /**
     * Get the next upcoming appointment for the patient (Real-time updates via polling)
     */
    fun getUpcomingAppointmentUpdates(): Flow<UpcomingAppointment?> = flow {
        while (true) {
            try {
                val response = apiService.getUpcomingAppointment()
                if (response.isSuccessful) {
                    emit(response.body())
                }
            } catch (e: Exception) {
                android.util.Log.e("PATIENT_APPOINTMENTS_DEBUG", "Failed to fetch upcoming appts", e)
            }
            delay(10000)
        }
    }

    /**
     * Get the next upcoming appointment for the patient
     */
    fun getUpcomingAppointment(
        onSuccess: (UpcomingAppointment?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getUpcomingAppointment()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch upcoming appointment details")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to get upcoming appointment")
                }
            }
        }
    }

    /**
     * Listen for real-time queue updates (via polling)
     */
    fun getLiveQueueUpdates(appointmentId: String? = null): Flow<LiveQueueInfo?> = flow {
        while (true) {
            try {
                val response = apiService.getLiveQueue(appointmentId)
                if (response.isSuccessful) {
                    emit(response.body())
                }
            } catch (e: Exception) {
                android.util.Log.e("LIVE_QUEUE_DEBUG", "Failed to fetch live queue details", e)
            }
            delay(5000)
        }
    }

    /**
     * Get recent activities
     */
    fun getRecentActivities(
        onSuccess: (List<ActivityItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getRecentActivities()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch recent activities")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch activities")
                }
            }
        }
    }

    /**
     * Create a notification for a user
     */
    fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: String = "GENERAL",
        onComplete: (() -> Unit)? = null
    ) {
        // Handled automatically on the backend server for all relevant database writes,
        // but we can mock success if needed
        onComplete?.invoke()
    }

    /**
     * Create an activity log for a user
     */
    fun createActivityLog(
        userId: String,
        type: String,
        title: String,
        description: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ActivityLogRequest(type, title, description)
                apiService.logActivity(request)
            } catch (e: Exception) {
                android.util.Log.e("DashboardRepository", "Failed to log activity log", e)
            }
        }
    }
}
