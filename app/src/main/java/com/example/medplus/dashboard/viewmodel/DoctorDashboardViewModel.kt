package com.example.medplus.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medplus.model.Appointment
import com.example.medplus.model.DoctorProfile
import com.example.medplus.model.QueueItem
import com.example.medplus.repository.DoctorRepository
import com.example.medplus.repository.DashboardRepository
import com.example.medplus.repository.AppointmentRepository
import com.example.medplus.dashboard.model.LiveQueueInfo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DoctorDashboardUiState(
    val isLoading: Boolean = false,
    val doctorProfile: DoctorProfile? = null,
    val todayAppointments: List<Appointment> = emptyList(),
    val todayCount: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val queueInfo: LiveQueueInfo? = null,
    val waitingQueueCount: Int = 0,
    val nextPatientName: String? = null,
    val estimatedWaitMinutes: Int = 0,
    val currentConsultation: QueueItem? = null,
    val currentPatientTime: String? = null,
    val nextWaitingPatient: QueueItem? = null,
    val nextAppointment: Appointment? = null,
    val uniquePatientCount: Int = 0,
    val unreadNotificationCount: Int = 0,
    val errorMessage: String? = null,
    val actionLoading: Boolean = false,
    val successMessage: String? = null,
    val isAppointmentError: Boolean = false
)

class DoctorDashboardViewModel : ViewModel() {

    private val repository = DoctorRepository()
    private val dashboardRepository = DashboardRepository()
    private val appointmentRepository = AppointmentRepository()
    private val auth = FirebaseAuth.getInstance()
    private val sessionManager = com.example.medplus.data.network.SessionManager.getInstance(com.google.firebase.FirebaseApp.getInstance().applicationContext)

    private val _uiState = MutableStateFlow(DoctorDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeQueue()
        observeAppointments()
    }

    private fun observeQueue() {
        viewModelScope.launch {
            dashboardRepository.getLiveQueueUpdates().collectLatest { queue ->
                _uiState.update { it.copy(queueInfo = queue) }
            }
        }
        
        val todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.ENGLISH))
        viewModelScope.launch {
            repository.getDoctorQueueFlow(todayStr).collectLatest { items ->
                val waiting = items.filter { it.status.trim().uppercase() == "WAITING" }
                    .sortedBy { it.tokenNumber.toIntOrNull() ?: 0 }
                val inProgress = items.find { it.status.trim().uppercase() == "IN_PROGRESS" }
                
                val currentApptTime = inProgress?.let { qi ->
                    _uiState.value.todayAppointments.find { it.appointmentId == qi.appointmentId }?.time
                } ?: waiting.firstOrNull()?.let { qi ->
                    _uiState.value.todayAppointments.find { it.appointmentId == qi.appointmentId }?.time
                }

                _uiState.update { it.copy(
                    waitingQueueCount = waiting.size,
                    nextPatientName = waiting.firstOrNull()?.patientName,
                    estimatedWaitMinutes = waiting.size * 10,
                    currentConsultation = inProgress,
                    currentPatientTime = currentApptTime,
                    nextWaitingPatient = waiting.firstOrNull()
                ) }
            }
        }
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            appointmentRepository.getDoctorAppointmentsFlow().collectLatest { appointments ->
                android.util.Log.d("DOCTOR_APPOINTMENT_DEBUG", "Realtime appointments count: ${appointments.size}")
                val todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.ENGLISH))
                
                val todayAppointments = appointments.filter { it.date == todayStr }.sortedBy { it.timestamp?.seconds ?: 0L }
                val todayCount = todayAppointments.size
                val pendingCount = appointments.count { it.status.trim().uppercase() != "COMPLETED" && it.status.trim().uppercase() != "CANCELLED" }
                val completedCount = appointments.count { it.status.trim().uppercase() == "COMPLETED" }
                
                // Find Next Appointment (nearest future appointment)
                val nextAppointment = appointments
                    .filter { it.status.trim().uppercase() != "COMPLETED" && it.status.trim().uppercase() != "CANCELLED" }
                    .sortedBy { it.timestamp?.seconds ?: 0L }
                    .firstOrNull()

                _uiState.update { it.copy(
                    todayAppointments = todayAppointments,
                    todayCount = todayCount,
                    pendingCount = pendingCount,
                    completedCount = completedCount,
                    nextAppointment = nextAppointment,
                    isAppointmentError = false
                ) }
            }
        }
    }

    fun loadDashboardData() {
        val uid = sessionManager.getUserId() ?: return
        
        android.util.Log.d("DOCTOR_APPOINTMENT_DEBUG", "Doctor UID: $uid")
        android.util.Log.d("DOCTOR_APPOINTMENT_DEBUG", "Loading appointments")
        android.util.Log.d("DOCTOR_SCHEDULE_DEBUG", "Reloading dashboard doctor profile")
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null, isAppointmentError = false) }

        viewModelScope.launch {
            // Fetch Profile
            repository.getDoctorProfile(
                uid = uid,
                onSuccess = { profile ->
                    android.util.Log.d("DOCTOR_SCHEDULE_DEBUG", "Dashboard schedule updated")
                    _uiState.update { it.copy(doctorProfile = profile) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error) }
                }
            )

            // Fetch All Appointments now tracked dynamically via observeAppointments Flow

            // Fetch Notification Count
            dashboardRepository.getUnreadNotificationCount(
                onSuccess = { count ->
                    _uiState.update { it.copy(unreadNotificationCount = count) }
                },
                onFailure = { /* Silent fail */ }
            )

            // Fetch Unique Patient Count
            repository.getDoctorPatients(
                onSuccess = { patients ->
                    _uiState.update { it.copy(uniquePatientCount = patients.size) }
                },
                onFailure = { /* Silent fail */ }
            )
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun startConsultation(queueItem: QueueItem) {
        val doctorId = sessionManager.getUserId() ?: return
        val appointmentId = queueItem.appointmentId
        android.util.Log.d("DOCTOR_CONSULTATION_DEBUG", "Starting consultation: appointmentId=$appointmentId")
        _uiState.update { it.copy(actionLoading = true) }

        if (queueItem.queueId.isEmpty()) {
            repository.getDoctorQueue(
                date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.ENGLISH)),
                onSuccess = { queueList ->
                    val matchingItem = queueList.find { it.appointmentId == appointmentId }
                    if (matchingItem != null) {
                        performStartConsultation(matchingItem.queueId, doctorId, appointmentId)
                    } else {
                        _uiState.update { it.copy(actionLoading = false, errorMessage = "Queue item not found for this appointment.") }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(actionLoading = false, errorMessage = error) }
                }
            )
        } else {
            performStartConsultation(queueItem.queueId, doctorId, appointmentId)
        }
    }

    private fun performStartConsultation(queueId: String, doctorId: String, appointmentId: String) {
        repository.updateQueueStatus(queueId, doctorId, "IN_PROGRESS",
            onSuccess = {
                appointmentRepository.updateAppointmentStatus(appointmentId, doctorId, "IN_PROGRESS",
                    onSuccess = {
                        android.util.Log.d("DOCTOR_CONSULTATION_DEBUG", "Consultation updated successfully")
                        _uiState.update { it.copy(actionLoading = false) }
                        loadDashboardData()
                    },
                    onFailure = { error ->
                        android.util.Log.e("DOCTOR_CONSULTATION_DEBUG", "Failed to update consultation", Exception(error))
                        _uiState.update { it.copy(actionLoading = false, errorMessage = "Unable to update consultation. Please try again.") }
                    }
                )
            },
            onFailure = { error ->
                android.util.Log.e("DOCTOR_CONSULTATION_DEBUG", "Failed to update consultation", Exception(error))
                _uiState.update { it.copy(actionLoading = false, errorMessage = "Unable to update consultation. Please try again.") }
            }
        )
    }

    fun completeConsultation(
        queueItem: QueueItem,
        diagnosis: String,
        prescription: String,
        notes: String,
        followUpDate: String
    ) {
        val doctorId = sessionManager.getUserId() ?: return
        val appointmentId = queueItem.appointmentId
        android.util.Log.d("DOCTOR_CONSULTATION_DEBUG", "Completing consultation: appointmentId=$appointmentId")
        _uiState.update { it.copy(actionLoading = true) }

        val medicalRecordRepository = com.example.medplus.repository.MedicalRecordRepository()
        medicalRecordRepository.createMedicalRecord(
            appointmentId = appointmentId,
            patientId = queueItem.patientId.ifEmpty { _uiState.value.todayAppointments.find { it.appointmentId == appointmentId }?.patientId ?: "" },
            diagnosis = diagnosis,
            prescription = prescription,
            notes = notes,
            followUpDate = followUpDate,
            onSuccess = {
                if (queueItem.queueId.isEmpty()) {
                    repository.getDoctorQueue(
                        date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.ENGLISH)),
                        onSuccess = { queueList ->
                            val matchingItem = queueList.find { it.appointmentId == appointmentId }
                            if (matchingItem != null) {
                                performCompleteConsultation(matchingItem.queueId, doctorId, appointmentId)
                            } else {
                                appointmentRepository.updateAppointmentStatus(appointmentId, doctorId, "COMPLETED",
                                    onSuccess = {
                                        _uiState.update { it.copy(actionLoading = false, successMessage = "Consultation completed and medical record saved.") }
                                        loadDashboardData()
                                    },
                                    onFailure = { error ->
                                        _uiState.update { it.copy(actionLoading = false, errorMessage = error) }
                                    }
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(actionLoading = false, errorMessage = error) }
                        }
                    )
                } else {
                    performCompleteConsultation(queueItem.queueId, doctorId, appointmentId)
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(actionLoading = false, errorMessage = "Failed to save medical record: $error") }
            }
        )
    }

    private fun performCompleteConsultation(queueId: String, doctorId: String, appointmentId: String) {
        repository.updateQueueStatus(queueId, doctorId, "COMPLETED",
            onSuccess = {
                appointmentRepository.updateAppointmentStatus(appointmentId, doctorId, "COMPLETED",
                    onSuccess = {
                        android.util.Log.d("DOCTOR_CONSULTATION_DEBUG", "Consultation updated successfully")
                        _uiState.update { it.copy(actionLoading = false, successMessage = "Consultation completed and medical record saved.") }
                        loadDashboardData()
                    },
                    onFailure = { error ->
                        android.util.Log.e("DOCTOR_CONSULTATION_DEBUG", "Failed to update consultation", Exception(error))
                        _uiState.update { it.copy(actionLoading = false, errorMessage = "Unable to update consultation. Please try again.") }
                    }
                )
            },
            onFailure = { error ->
                android.util.Log.e("DOCTOR_CONSULTATION_DEBUG", "Failed to update consultation", Exception(error))
                _uiState.update { it.copy(actionLoading = false, errorMessage = "Unable to update consultation. Please try again.") }
            }
        )
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
