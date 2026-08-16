package com.example.medplus.repository

import com.example.medplus.model.Appointment
import com.example.medplus.data.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository to handle appointment-related data operations in MongoDB.
 */
class AppointmentRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = RetrofitClient.getApiService(context)

    /**
     * Fetches all appointments for the currently authenticated patient.
     */
    fun getPatientAppointments(
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getPatientAppointments()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch patient appointments")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch appointments")
                }
            }
        }
    }

    /**
     * Cancels an existing appointment by updating its status.
     */
    fun cancelAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.cancelAppointment(appointmentId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to cancel appointment")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to cancel appointment")
                }
            }
        }
    }

    /**
     * Fetches all appointments for the currently authenticated doctor.
     */
    fun getDoctorAppointments(
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getDoctorAppointments()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch doctor appointments")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch appointments")
                }
            }
        }
    }

    fun updateAppointmentStatus(
        appointmentId: String,
        doctorId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = com.example.medplus.data.network.UpdateAppointmentStatusRequest(status = newStatus)
                val response = apiService.updateAppointmentStatus(appointmentId, request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to update appointment status")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to update status")
                }
            }
        }
    }
}
