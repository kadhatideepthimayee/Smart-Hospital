package com.example.smarthospitalqueue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthospitalqueue.data.AppointmentRepository
import com.example.smarthospitalqueue.data.model.AppointmentModel
import com.example.smarthospitalqueue.ui.screens.appointment.Doctor
import com.example.smarthospitalqueue.ui.screens.appointment.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── UI State ──────────────────────────────────────────────────────────────────
sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    data class Success(val appointment: AppointmentModel) : BookingState()
    data class Error(val message: String) : BookingState()
}

class BookAppointmentViewModel : ViewModel() {

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState

    fun bookAppointment(
        doctor: Doctor,
        date: LocalDate,
        slot: TimeSlot
    ) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading

            // Format dates
            val displayDate = date.format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
            )
            val rawDate = date.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )

            val result = AppointmentRepository.bookAppointment(
                doctorId        = doctor.id.toString(),
                doctorName      = doctor.name,
                specialization  = doctor.specialization,
                department      = doctor.department,
                hospital        = doctor.hospital,
                roomNumber      = "Room 204, Floor 2", // extend Doctor model later
                consultationFee = doctor.consultationFee,
                date            = displayDate,
                dateRaw         = rawDate,
                time            = slot.time
            )

            result.fold(
                onSuccess = { appointment ->
                    _bookingState.value = BookingState.Success(appointment)
                },
                onFailure = { error ->
                    _bookingState.value = BookingState.Error(
                        error.message ?: "Booking failed. Please try again."
                    )
                }
            )
        }
    }

    fun resetState() {
        _bookingState.value = BookingState.Idle
    }
}