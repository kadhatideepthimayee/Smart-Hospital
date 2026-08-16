package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import android.util.Log

/**
 * UI models for Appointment Booking
 */
data class TimeSlot(
    val time: String,
    val isAvailable: Boolean = true
)

data class DateItem(
    val dayName: String,
    val dayNumber: String,
    val fullDate: String,
    val localDate: LocalDate
)

/**
 * Steps in the Appointment Booking Flow
 */
enum class BookingStep {
    SELECT_SLOT,
    CONFIRM_APPOINTMENT,
    BOOKING_SUCCESS
}

/**
 * UI State for the Book Appointment screen.
 */
data class BookAppointmentUiState(
    val isLoading: Boolean = false,
    val doctors: List<DoctorProfile> = emptyList(),
    val errorMessage: String? = null,
    val isBookingSuccessful: Boolean = false,
    val availableDates: List<DateItem> = emptyList(),
    val availableTimeSlots: List<TimeSlot> = emptyList(),
    val bookedSlots: List<String> = emptyList(),
    
    // Step-based navigation
    val currentStep: BookingStep = BookingStep.SELECT_SLOT,
    
    // Selected data for confirmation
    val selectedDoctor: DoctorProfile? = null,
    val selectedDepartment: String = "",
    val selectedDate: DateItem? = null,
    val selectedTime: String = "",
    
    // Result
    val confirmedAppointment: com.example.medplus.model.Appointment? = null
)

/**
 * ViewModel to manage the logic and data for booking an appointment.
 */
class BookAppointmentViewModel : ViewModel() {

    private val repository = DoctorRepository()

    private val _uiState = MutableStateFlow(BookAppointmentUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Navigation: Move to Confirmation step
     */
    fun moveToConfirmation(
        doctor: DoctorProfile,
        department: String,
        date: DateItem,
        time: String
    ) {
        _uiState.update { it.copy(
            currentStep = BookingStep.CONFIRM_APPOINTMENT,
            selectedDoctor = doctor,
            selectedDepartment = department,
            selectedDate = date,
            selectedTime = time
        ) }
    }

    /**
     * Navigation: Go back to selection step
     */
    fun goBack() {
        _uiState.update { 
            if (it.currentStep == BookingStep.CONFIRM_APPOINTMENT) {
                it.copy(currentStep = BookingStep.SELECT_SLOT)
            } else {
                it
            }
        }
    }

    /**
     * Fetches doctors from Firestore based on the selected department.
     */
    fun fetchDoctors(department: String = "") {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(
                errorMessage = "Please sign in to view available doctors.",
                isLoading = false
            ) }
            return
        }

        // Clear existing doctors and error message while loading
        _uiState.update { it.copy(isLoading = true, doctors = emptyList(), errorMessage = null) }

        Log.d("BOOK_APPOINTMENT_DEBUG", "ViewModel fetchDoctors called for department: $department")
        Log.d("BOOK_APPOINTMENT_DEBUG", "Current User UID: ${currentUser.uid}")

        repository.getDoctorsByDepartment(
            department = department,
            onSuccess = { doctorList ->
                _uiState.update { it.copy(
                    doctors = doctorList,
                    isLoading = false,
                    errorMessage = null
                ) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(
                    errorMessage = error,
                    isLoading = false
                ) }
            }
        )
    }

    /**
     * Generates available dates based on doctor's working days.
     */
    fun generateAvailableDates(doctor: DoctorProfile) {
        Log.d("APPOINTMENT_AVAILABILITY_DEBUG", "--- generateAvailableDates START ---")
        Log.d("APPOINTMENT_AVAILABILITY_DEBUG", "Doctor: ${doctor.fullName} (${doctor.uid})")
        Log.d("APPOINTMENT_AVAILABILITY_DEBUG", "Working Days from Firestore: ${doctor.workingDays}")

        if (doctor.workingDays.isEmpty()) {
            Log.w("APPOINTMENT_AVAILABILITY_DEBUG", "REASON: Doctor has no working days configured.")
            _uiState.update { it.copy(availableDates = emptyList()) }
            return
        }

        val today = LocalDate.now()
        val dates = mutableListOf<DateItem>()
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

        // Generate dates for the next 14 days
        for (i in 0..13) {
            val date = today.plusDays(i.toLong())
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) // e.g., "Mon"
            
            // Check if doctor works on this day
            // Normalize both to upper case for comparison
            val isWorkingDay = doctor.workingDays.any { configDay ->
                val normalizedConfig = configDay.trim().uppercase()
                val normalizedActual = dayName.uppercase()
                
                normalizedConfig.startsWith(normalizedActual) || normalizedActual.startsWith(normalizedConfig)
            }

            if (isWorkingDay) {
                dates.add(DateItem(
                    dayName = if (i == 0) "Today" else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    dayNumber = date.dayOfMonth.toString(),
                    fullDate = date.format(dateFormatter),
                    localDate = date
                ))
            }
        }

        Log.d("APPOINTMENT_AVAILABILITY_DEBUG", "Generated ${dates.size} available dates.")
        _uiState.update { it.copy(availableDates = dates, availableTimeSlots = emptyList()) }
    }

    /**
     * Helper to parse time in multiple common formats safely
     */
    private fun parseLocalTime(timeStr: String): LocalTime? {
        val formats = listOf(
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH)
        )
        
        for (format in formats) {
            try {
                return LocalTime.parse(timeStr.trim(), format)
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    /**
     * Generates available time slots for a specific date based on doctor's schedule.
     */
    fun generateTimeSlots(doctor: DoctorProfile, dateStr: String) {
        Log.d("HOURLY_APPOINTMENT_DEBUG", "--- generateTimeSlots START ---")
        Log.d("HOURLY_APPOINTMENT_DEBUG", "Doctor: ${doctor.fullName} (${doctor.uid})")
        Log.d("HOURLY_APPOINTMENT_DEBUG", "Date Selected: $dateStr")
        Log.d("HOURLY_APPOINTMENT_DEBUG", "Working Days: ${doctor.workingDays}")
        Log.d("HOURLY_APPOINTMENT_DEBUG", "Doctor Start: ${doctor.consultationStartTime}, End: ${doctor.consultationEndTime}")

        Log.d("HOURLY_APPOINTMENT_DEBUG", "Lunch Break: ${doctor.lunchStartTime} - ${doctor.lunchEndTime}")
        Log.d("HOURLY_APPOINTMENT_DEBUG", "Other Break: ${doctor.breakStartTime} - ${doctor.breakEndTime}")

        if (doctor.consultationStartTime.isBlank() || doctor.consultationEndTime.isBlank()) {
            Log.w("HOURLY_APPOINTMENT_DEBUG", "REASON: Consultation start/end time is missing.")
            _uiState.update { it.copy(availableTimeSlots = emptyList()) }
            return
        }

        try {
            val startTime = parseLocalTime(doctor.consultationStartTime)
            val endTime = parseLocalTime(doctor.consultationEndTime)
            
            val lunchStart = parseLocalTime(doctor.lunchStartTime)
            val lunchEnd = parseLocalTime(doctor.lunchEndTime)
            val breakStart = parseLocalTime(doctor.breakStartTime)
            val breakEnd = parseLocalTime(doctor.breakEndTime)

            if (startTime == null || endTime == null) {
                Log.e("HOURLY_APPOINTMENT_DEBUG", "REASON: Failed to parse consultation times.")
                _uiState.update { it.copy(availableTimeSlots = emptyList()) }
                return
            }

            if (!endTime.isAfter(startTime)) {
                Log.e("HOURLY_APPOINTMENT_DEBUG", "REASON: End time ($endTime) is not after start time ($startTime).")
                _uiState.update { it.copy(availableTimeSlots = emptyList()) }
                return
            }

            val duration = 60L
            val rawSlots = mutableListOf<TimeSlot>()
            val displayFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
            
            var current: LocalTime = startTime
            while (current.plusMinutes(duration).isBefore(endTime) || current.plusMinutes(duration) == endTime) {
                val slotTime = current
                val slotEndTime = current.plusMinutes(60)
                
                val isInLunch = if (lunchStart != null && lunchEnd != null) {
                    slotTime.isBefore(lunchEnd) && slotEndTime.isAfter(lunchStart)
                } else false
                
                val isInBreak = if (breakStart != null && breakEnd != null) {
                    slotTime.isBefore(breakEnd) && slotEndTime.isAfter(breakStart)
                } else false
                
                if (!isInLunch && !isInBreak) {
                    rawSlots.add(TimeSlot(time = current.format(displayFormatter)))
                } else {
                    Log.d("HOURLY_APPOINTMENT_DEBUG", "Skipping slot $slotTime due to break (Lunch: $isInLunch, Other: $isInBreak)")
                }
                
                current = current.plusMinutes(duration)
            }

            Log.d("HOURLY_APPOINTMENT_DEBUG", "Generated Slots (Before past filtering): ${rawSlots.map { it.time }}")

            // Filter out past slots if the date is Today
            val todayDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
            val filteredByTimeSlots = if (dateStr == todayDateStr) {
                val now = LocalTime.now()
                Log.d("HOURLY_APPOINTMENT_DEBUG", "Filtering today's slots. Current LocalTime: $now")
                rawSlots.filter { slot ->
                    val slotTime = parseLocalTime(slot.time)
                    slotTime?.isAfter(now) ?: true
                }
            } else {
                rawSlots
            }

            if (filteredByTimeSlots.size < rawSlots.size) {
                Log.d("HOURLY_APPOINTMENT_DEBUG", "Filtered out ${rawSlots.size - filteredByTimeSlots.size} past slots.")
            }

            // Fetch booked slots from backend to mark availability
            _uiState.update { it.copy(isLoading = true) }
            fetchBookedSlots(doctor.uid, dateStr) { booked ->
                Log.d("HOURLY_APPOINTMENT_DEBUG", "Already Booked Slots: $booked")
                val finalSlots = filteredByTimeSlots.map { slot ->
                    val bookedCount = booked.count { it == slot.time }
                    val maxCapacity = (60 / doctor.slotDuration).coerceAtLeast(1)
                    slot.copy(isAvailable = bookedCount < maxCapacity)
                }
                
                Log.d("HOURLY_APPOINTMENT_DEBUG", "Final Available Slots: ${finalSlots.filter { it.isAvailable }.map { it.time }}")
                
                _uiState.update { it.copy(availableTimeSlots = finalSlots, isLoading = false) }
            }

        } catch (e: Exception) {
            Log.e("HOURLY_APPOINTMENT_DEBUG", "CRITICAL ERROR in generateTimeSlots", e)
            _uiState.update { it.copy(availableTimeSlots = emptyList(), isLoading = false) }
        }
    }

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = com.example.medplus.data.network.RetrofitClient.getApiService(context)

    private fun fetchBookedSlots(doctorId: String, date: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.getAppointmentsByDoctorId(doctorId)
                if (response.isSuccessful && response.body() != null) {
                    val booked = response.body()!!
                        .filter { it.date == date && it.status == "UPCOMING" }
                        .map { it.time }
                    onResult(booked)
                } else {
                    onResult(emptyList())
                }
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun bookAppointment() {
        val state = _uiState.value
        val doctor = state.selectedDoctor ?: return
        val date = state.selectedDate?.fullDate ?: return
        val time = state.selectedTime
        val department = state.selectedDepartment
        val patientId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

        Log.d("APPOINTMENT_BOOKING_DEBUG", "--- STARTING BOOKING PROCESS ---")
        Log.d("APPOINTMENT_BOOKING_DEBUG", "Patient UID: $patientId")
        Log.d("APPOINTMENT_BOOKING_DEBUG", "Doctor UID: ${doctor.uid}")
        Log.d("APPOINTMENT_BOOKING_DEBUG", "Selected Date: $date")
        Log.d("APPOINTMENT_BOOKING_DEBUG", "Selected Time: $time")

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        repository.createAppointment(
            doctorId = doctor.uid,
            doctorName = doctor.fullName,
            department = department,
            date = date,
            time = time,
            onSuccess = { appointment ->
                Log.d("APPOINTMENT_BOOKING_DEBUG", "SUCCESS: Appointment created. ID: ${appointment.appointmentId}, Token: ${appointment.tokenNumber}")
                _uiState.update { it.copy(
                    isLoading = false, 
                    isBookingSuccessful = true,
                    currentStep = BookingStep.BOOKING_SUCCESS,
                    confirmedAppointment = appointment
                ) }
            },
            onFailure = { error ->
                Log.e("APPOINTMENT_BOOKING_DEBUG", "FAILURE: $error")
                _uiState.update { it.copy(
                    isLoading = false, 
                    errorMessage = error ?: "Unable to confirm appointment. Please try again."
                ) }
            }
        )
    }
}
