package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medplus.model.Appointment
import com.example.medplus.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        observeAppointments()
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            repository.getDoctorAppointmentsFlow().collect { list ->
                _uiState.update { it.copy(appointments = list) }
            }
        }
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
        return _uiState.value.appointments.filter { 
            it.date == todayStr && it.status.trim().uppercase() != "COMPLETED" && it.status.trim().uppercase() != "CANCELLED"
        }
    }

    fun getUpcomingAppointments(): List<Appointment> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        return _uiState.value.appointments.filter {
            if (it.status.trim().uppercase() == "COMPLETED" || it.status.trim().uppercase() == "CANCELLED") {
                return@filter false
            }
            try {
                val apptDate = LocalDate.parse(it.date, formatter)
                apptDate.isAfter(today)
            } catch (e: Exception) {
                val todayStr = today.format(formatter)
                it.date != todayStr
            }
        }.sortedBy { it.timestamp?.seconds ?: 0L }
    }

    fun getCompletedAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter { it.status.trim().uppercase() == "COMPLETED" }
    }

    fun getThisWeekAppointments(): List<Appointment> {
        val today = LocalDate.now()
        val endOfWeek = today.plusDays(7)
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        return _uiState.value.appointments.filter {
            val status = it.status.trim().uppercase()
            if (status == "COMPLETED" || status == "CANCELLED") {
                return@filter false
            }
            try {
                val apptDate = LocalDate.parse(it.date, formatter)
                !apptDate.isBefore(today) && !apptDate.isAfter(endOfWeek)
            } catch (e: Exception) {
                false
            }
        }.sortedWith { a1, a2 ->
            try {
                val d1 = LocalDate.parse(a1.date, formatter)
                val d2 = LocalDate.parse(a2.date, formatter)
                d1.compareTo(d2)
            } catch (e: Exception) {
                0
            }
        }
    }
}
