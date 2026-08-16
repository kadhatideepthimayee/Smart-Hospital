package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdminDashboardUiState(
    val adminName: String = "Admin",
    val unreadNotificationsCount: Int = 0,
    val isLoading: Boolean = false,
    val doctors: List<DoctorProfile> = emptyList(),
    val pendingDoctors: List<DoctorProfile> = emptyList(),
    val approvedDoctors: List<DoctorProfile> = emptyList(),
    val rejectedDoctors: List<DoctorProfile> = emptyList(),
    val pendingCount: Int = 0,
    val approvedCount: Int = 0,
    val rejectedCount: Int = 0,
    val totalCount: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminDashboardViewModel : ViewModel() {

    private val repository = AdminRepository()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val sessionManager = com.example.medplus.data.network.SessionManager.getInstance(com.google.firebase.FirebaseApp.getInstance().applicationContext)

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
        loadAdminName()
        loadUnreadNotificationsCount()
    }

    private fun loadUnreadNotificationsCount() {
        repository.getUnreadNotificationCount { count ->
            _uiState.update { it.copy(unreadNotificationsCount = count) }
        }
    }

    private fun loadAdminName() {
        val name = sessionManager.getName() ?: "MedPlus Admin"
        _uiState.update { it.copy(adminName = name) }
    }

    fun loadDashboardData() {
        android.util.Log.d("ADMIN_DOCTOR_DEBUG", "Loading pending doctors")
        android.util.Log.d("ADMIN_DOCTOR_DEBUG", "Loading approved doctors")
        android.util.Log.d("ADMIN_DOCTOR_DEBUG", "Loading rejected doctors")
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getAllDoctorProfiles(
            onSuccess = { profiles ->
                android.util.Log.d("ADMIN_DEBUG", "getAllDoctorProfiles success. Count: ${profiles.size}")
                
                val pending = profiles.filter { it.verificationStatus.trim().uppercase() == "PENDING" }
                val approved = profiles.filter { 
                    val s = it.verificationStatus.trim().uppercase()
                    s == "VERIFIED" || s == "APPROVED" 
                }
                val rejected = profiles.filter { it.verificationStatus.trim().uppercase() == "REJECTED" }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        doctors = profiles,
                        pendingDoctors = pending,
                        approvedDoctors = approved,
                        rejectedDoctors = rejected,
                        pendingCount = pending.size,
                        approvedCount = approved.size,
                        rejectedCount = rejected.size,
                        totalCount = profiles.size
                    )
                }
            },
            onFailure = { error ->
                android.util.Log.e("ADMIN_DEBUG", "getAllDoctorProfiles FAILURE: $error")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    /**
     * Rejects an already approved doctor.
     */
    fun rejectDoctor(doctorUid: String, doctorName: String) {
        val adminUid = auth.currentUser?.uid ?: return
        android.util.Log.d("ADMIN_DOCTOR_DEBUG", "Rejected doctor: $doctorUid")
        
        _uiState.update { it.copy(isLoading = true) }
        repository.updateDoctorVerificationStatus(
            uid = doctorUid,
            adminUid = adminUid,
            newStatus = "REJECTED",
            rejectionReason = "Admin manually rejected after approval",
            onSuccess = {
                _uiState.update { it.copy(successMessage = "Dr. $doctorName has been rejected.") }
                loadDashboardData() // Refresh list
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
