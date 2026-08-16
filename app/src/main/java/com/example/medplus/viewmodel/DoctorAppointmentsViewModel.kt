package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.Appointment
import com.example.medplus.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DoctorAppointmentsUiState(
    val isLoading: Boolean = false,
    val appointments: List<Appointment> = emptyList(),
    val errorMessage: String? = null
)

class DoctorAppointmentsViewModel : ViewModel() {

    private val repository = AppointmentRepository()

    private val _uiState = MutableStateFlow(DoctorAppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getDoctorAppointments(
            onSuccess = { list ->
                _uiState.update { it.copy(isLoading = false, appointments = list) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun getTodayAppointments(): List<Appointment> {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
        return _uiState.value.appointments.filter { it.date == todayStr }
    }

    fun getUpcomingAppointments(): List<Appointment> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        return _uiState.value.appointments.filter {
            try {
                val apptDate = LocalDate.parse(it.date, formatter)
                apptDate.isAfter(today) && it.status.trim().uppercase() != "COMPLETED" && it.status.trim().uppercase() != "CANCELLED"
            } catch (e: Exception) {
                false
            }
        }.sortedBy { it.timestamp?.seconds ?: 0L }
    }

    fun getCompletedAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter { it.status.trim().uppercase() == "COMPLETED" }
    }
}
