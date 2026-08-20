package com.example.medplus.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.medplus.auth.model.User
import com.example.medplus.model.Appointment
import com.example.medplus.repository.AppointmentRepository
import com.example.medplus.repository.DoctorRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DoctorPatientDetailsUiState(
    val isLoading: Boolean = false,
    val patient: User? = null,
    val appointments: List<Appointment> = emptyList(),
    val errorMessage: String? = null
)

class DoctorPatientDetailsViewModel : ViewModel() {

    private val doctorRepository = DoctorRepository()
    private val appointmentRepository = AppointmentRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DoctorPatientDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadPatientDetails(patientId: String) {
        val doctorId = com.example.medplus.data.network.SessionManager.getInstance(com.google.firebase.FirebaseApp.getInstance().applicationContext).getUserId() ?: return
        Log.d("DOCTOR_PATIENT_DEBUG", "Loading patient details: $patientId")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // Fetch patient basic info
        doctorRepository.getDoctorPatients(
            onSuccess = { patients ->
                val patient = patients.find { it.uid == patientId }
                if (patient != null) {
                    _uiState.update { it.copy(patient = patient) }
                    
                    // Now fetch appointment history with this doctor
                    appointmentRepository.getDoctorAppointments(
                        onSuccess = { allAppointments ->
                            val history = allAppointments.filter { it.patientId == patientId }
                                .sortedByDescending { it.timestamp?.seconds ?: 0L }
                            _uiState.update { it.copy(isLoading = false, appointments = history) }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                        }
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Patient not associated with you.") }
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }
}
