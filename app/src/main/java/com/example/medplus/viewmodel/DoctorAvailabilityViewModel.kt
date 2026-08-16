package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.repository.DoctorRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import android.util.Log

data class DoctorAvailabilityUiState(
    val isLoading: Boolean = false,
    val consultationFee: Double = 0.0,
    val workingDays: List<String> = emptyList(),
    val startTime: String = "09:00 AM",
    val endTime: String = "05:00 PM",
    val lunchStartTime: String = "",
    val lunchEndTime: String = "",
    val breakStartTime: String = "",
    val breakEndTime: String = "",
    val slotDuration: Int = 15,
    val isUpdateSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class DoctorAvailabilityViewModel : ViewModel() {

    private val repository = DoctorRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DoctorAvailabilityUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAvailability()
    }

    fun loadAvailability() {
        val uid = auth.currentUser?.uid ?: return
        Log.d("DOCTOR_SCHEDULE_DEBUG", "Loading availability for UID: $uid")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        repository.getDoctorProfile(
            uid = uid,
            onSuccess = { profile ->
                if (profile != null) {
                    Log.d("DOCTOR_SCHEDULE_DEBUG", "Availability loaded successfully")
                    _uiState.update { it.copy(
                        isLoading = false,
                        consultationFee = profile.consultationFee,
                        workingDays = profile.workingDays,
                        startTime = profile.consultationStartTime.ifBlank { "09:00 AM" },
                        endTime = profile.consultationEndTime.ifBlank { "05:00 PM" },
                        lunchStartTime = profile.lunchStartTime,
                        lunchEndTime = profile.lunchEndTime,
                        breakStartTime = profile.breakStartTime,
                        breakEndTime = profile.breakEndTime,
                        slotDuration = if (profile.slotDuration > 0) profile.slotDuration else 15
                    ) }
                } else {
                    Log.e("DOCTOR_SCHEDULE_DEBUG", "Profile not found for UID: $uid")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Profile not found") }
                }
            },
            onFailure = { error ->
                Log.e("DOCTOR_SCHEDULE_DEBUG", "Failed to load doctor availability: $error")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun updatePracticeDetails(
        consultationFee: Double,
        workingDays: List<String>,
        startTime: String,
        endTime: String,
        lunchStart: String = "",
        lunchEnd: String = "",
        breakStart: String = "",
        breakEnd: String = "",
        slotDuration: Int
    ) {
        val uid = auth.currentUser?.uid ?: return
        Log.d("DOCTOR_SCHEDULE_DEBUG", "Saving schedule for UID: $uid")
        
        // Basic Validation
        if (workingDays.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one working day.") }
            return
        }

        if (!isTimeAfter(startTime, endTime)) {
            _uiState.update { it.copy(errorMessage = "End time must be after start time.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isUpdateSuccess = false) }
        
        repository.updateDoctorPracticeDetails(
            consultationFee = consultationFee,
            workingDays = workingDays,
            startTime = startTime,
            endTime = endTime,
            lunchStart = lunchStart,
            lunchEnd = lunchEnd,
            breakStart = breakStart,
            breakEnd = breakEnd,
            slotDuration = slotDuration,
            onSuccess = {
                Log.d("DOCTOR_SCHEDULE_DEBUG", "Schedule saved successfully")
                _uiState.update { it.copy(isUpdateSuccess = true) }
                loadAvailability()
            },
            onFailure = { error ->
                Log.e("DOCTOR_SCHEDULE_DEBUG", "Failed to save doctor availability: $error")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, isUpdateSuccess = false) }
    }

    private fun isTimeAfter(startTime: String, endTime: String): Boolean {
        try {
            val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val start = format.parse(startTime)
            val end = format.parse(endTime)
            return end?.after(start) ?: false
        } catch (e: Exception) {
            return false
        }
    }
}
