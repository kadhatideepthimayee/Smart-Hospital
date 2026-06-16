package com.example.smarthospitalqueue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthospitalqueue.data.AppointmentRepository
import com.example.smarthospitalqueue.data.model.AppointmentModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────
data class ConfirmationUiState(
    val isLoading: Boolean = false,
    val appointment: AppointmentModel? = null,
    val error: String? = null
)

class AppointmentConfirmationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmationUiState())
    val uiState: StateFlow<ConfirmationUiState> = _uiState

    // Called when screen opens — loads latest appointment from Firestore
    fun loadLatestAppointment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val appointment = AppointmentRepository.getLatestAppointment()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    appointment = appointment
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}