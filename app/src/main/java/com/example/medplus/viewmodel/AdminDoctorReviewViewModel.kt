package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.repository.AdminRepository
import com.example.medplus.repository.DoctorRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdminDoctorReviewUiState(
    val isLoading: Boolean = false,
    val doctor: DoctorProfile? = null,
    val errorMessage: String? = null,
    val isActionSuccess: Boolean = false,
    val successMessage: String? = null
)

class AdminDoctorReviewViewModel : ViewModel() {

    private val adminRepository = AdminRepository()
    val doctorRepository = DoctorRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AdminDoctorReviewUiState())
    val uiState = _uiState.asStateFlow()

    fun loadDoctorDetails(uid: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, isActionSuccess = false) }
        doctorRepository.getDoctorProfile(
            uid = uid,
            onSuccess = { profile ->
                _uiState.update { it.copy(doctor = profile, isLoading = false) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(errorMessage = "Unable to load doctor details", isLoading = false) }
            }
        )
    }

    fun approveDoctor(uid: String) {
        performAction(uid, "VERIFIED", null)
    }

    fun rejectDoctor(uid: String, reason: String) {
        performAction(uid, "REJECTED", reason)
    }

    private fun performAction(uid: String, status: String, reason: String?) {
        val adminUid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        adminRepository.updateDoctorVerificationStatus(
            uid = uid,
            adminUid = adminUid,
            newStatus = status,
            rejectionReason = reason,
            onSuccess = {
                android.util.Log.d("ADMIN_VERIFY_DEBUG", "Reloading doctor data")
                // Fetch the updated profile immediately
                doctorRepository.getDoctorProfile(
                    uid = uid,
                    onSuccess = { updatedProfile ->
                        android.util.Log.d("ADMIN_PENDING_DEBUG", "Action performed for doctor: $uid, Status: $status")
                        val msg = if (status == "VERIFIED") "Doctor Verified" else "Doctor Rejected"
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isActionSuccess = true, 
                                successMessage = msg,
                                doctor = updatedProfile
                            ) 
                        }
                    },
                    onFailure = {
                        // Even if fetch fails, the action was successful
                        val msg = if (status == "VERIFIED") "Doctor Verified" else "Doctor Rejected"
                        _uiState.update { it.copy(isLoading = false, isActionSuccess = true, successMessage = msg) }
                    }
                )
            },
            onFailure = { error ->
                _uiState.update { it.copy(errorMessage = "Failed to update verification status: $error", isLoading = false) }
            }
        )
    }
}
