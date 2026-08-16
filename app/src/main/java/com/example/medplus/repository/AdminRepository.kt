package com.example.medplus.repository

import com.example.medplus.model.AdminNotification
import com.example.medplus.model.DoctorProfile
import com.example.medplus.data.network.RetrofitClient
import com.example.medplus.data.network.VerifyDoctorRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository to handle admin-related data operations in MongoDB.
 */
class AdminRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = RetrofitClient.getApiService(context)

    /**
     * Fetches admin notifications.
     */
    fun getAdminNotifications(
        onSuccess: (List<AdminNotification>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getAdminNotifications()
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
     * Deletes a notification.
     */
    fun deleteNotification(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.deleteAdminNotification(id)
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
     * Marks a notification as read.
     */
    fun markNotificationAsRead(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.markAdminNotificationRead(id)
            } catch (e: Exception) {
                android.util.Log.e("AdminRepository", "Failed to mark notification read", e)
            }
        }
    }

    /**
     * Gets the unread notification count.
     */
    fun getUnreadNotificationCount(
        onSuccess: (Int) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getAdminUnreadCount()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body()?.count ?: 0)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onSuccess(0)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onSuccess(0)
                }
            }
        }
    }

    /**
     * Fetches doctor profiles based on their verification status.
     */
    fun getDoctorsByStatus(
        status: String,
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getDoctorsByStatus(status)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch doctors by status")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch doctors")
                }
            }
        }
    }

    /**
     * Fetches all doctor profiles.
     */
    fun getAllDoctorProfiles(
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getAllDoctorProfiles()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch all doctor profiles")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch doctors")
                }
            }
        }
    }

    /**
     * Updates the verification status of a doctor with review metadata.
     */
    fun updateDoctorVerificationStatus(
        uid: String,
        adminUid: String,
        newStatus: String,
        rejectionReason: String? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = VerifyDoctorRequest(
                    doctorId = uid,
                    newStatus = newStatus,
                    rejectionReason = rejectionReason
                )
                val response = apiService.verifyDoctor(request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to update verification status")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to update status")
                }
            }
        }
    }
}
