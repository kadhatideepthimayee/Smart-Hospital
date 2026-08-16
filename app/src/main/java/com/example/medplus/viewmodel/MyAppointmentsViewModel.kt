package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medplus.model.Appointment
import com.example.medplus.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the My Appointments screen.
 */
data class MyAppointmentsUiState(
    val isLoading: Boolean = false,
    val appointments: List<Appointment> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel to manage patient's appointments.
 */
class MyAppointmentsViewModel : ViewModel() {

    private val repository = AppointmentRepository()

    private val _uiState = MutableStateFlow(MyAppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAppointments()
    }

    /**
     * Fetches appointments from Firestore.
     */
    fun loadAppointments() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        repository.getPatientAppointments(
            onSuccess = { list ->
                _uiState.update { it.copy(
                    appointments = list,
                    isLoading = false
                ) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(
                    errorMessage = "Unable to load appointments. Please try again.",
                    isLoading = false
                ) }
            }
        )
    }

    /**
     * Cancels an appointment.
     */
    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.cancelAppointment(
                appointmentId = appointmentId,
                onSuccess = {
                    // Refresh the list after cancellation
                    loadAppointments()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        errorMessage = "Failed to cancel appointment. Please try again.",
                        isLoading = false
                    ) }
                }
            )
        }
    }
}
