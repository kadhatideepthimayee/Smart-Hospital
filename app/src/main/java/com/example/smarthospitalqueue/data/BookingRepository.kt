package com.example.smarthospitalqueue.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object BookingRepository {
    var bookedDoctorName by mutableStateOf("Dr. Arjun Mehta")
    var bookedSpecialization by mutableStateOf("Senior Cardiologist")
    var bookedDepartment by mutableStateOf("Cardiology")
    var bookedDate by mutableStateOf("Mon, 2 Jun 2025")
    var bookedTime by mutableStateOf("11:00 AM")
    var bookedToken by mutableStateOf("T-52")
    var bookedHospital by mutableStateOf("Apollo Smart Hospital")
    var bookedFee by mutableStateOf("₹800")
    var hasActiveBooking by mutableStateOf(false)
}
