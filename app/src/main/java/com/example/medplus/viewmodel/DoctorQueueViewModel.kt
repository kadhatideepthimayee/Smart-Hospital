package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.QueueItem
import com.example.medplus.repository.DoctorRepository
import com.example.medplus.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth

data class DoctorQueueUiState(
    val isLoading: Boolean = false,
    val queueItems: List<QueueItem> = emptyList(),
    val errorMessage: String? = null
)

class DoctorQueueViewModel : ViewModel() {

    private val repository = DoctorRepository()
    private val appointmentRepository = AppointmentRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DoctorQueueUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeQueue()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeQueue() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.ENGLISH))
        viewModelScope.launch {
            repository.getDoctorQueueFlow(todayStr).collectLatest { items ->
                _uiState.update { it.copy(isLoading = false, queueItems = items) }
            }
        }
    }

    fun startConsultation(queueId: String, appointmentId: String) {
        val doctorId = auth.currentUser?.uid ?: return
        repository.updateQueueStatus(queueId, doctorId, "IN_PROGRESS",
            onSuccess = {
                appointmentRepository.updateAppointmentStatus(appointmentId, doctorId, "IN_PROGRESS",
                    onSuccess = { /* Updates via flow */ },
                    onFailure = { error -> _uiState.update { it.copy(errorMessage = error) } }
                )
            },
            onFailure = { error -> _uiState.update { it.copy(errorMessage = error) } }
        )
    }

    fun completeConsultation(
        queueId: String,
        appointmentId: String,
        patientId: String,
        diagnosis: String,
        prescription: String,
        notes: String,
        followUpDate: String
    ) {
        val doctorId = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true) }
        val medicalRecordRepository = com.example.medplus.repository.MedicalRecordRepository()
        medicalRecordRepository.createMedicalRecord(
            appointmentId = appointmentId,
            patientId = patientId,
            diagnosis = diagnosis,
            prescription = prescription,
            notes = notes,
            followUpDate = followUpDate,
            onSuccess = {
                repository.updateQueueStatus(queueId, doctorId, "COMPLETED",
                    onSuccess = {
                        appointmentRepository.updateAppointmentStatus(appointmentId, doctorId, "COMPLETED",
                            onSuccess = {
                                _uiState.update { it.copy(isLoading = false) }
                            },
                            onFailure = { error ->
                                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    }
                )
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun loadQueue() {
        observeQueue()
    }
}
