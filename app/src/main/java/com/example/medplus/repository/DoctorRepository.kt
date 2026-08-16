package com.example.medplus.repository

import android.net.Uri
import com.example.medplus.model.Appointment
import com.example.medplus.model.DoctorFeedback
import com.example.medplus.model.DoctorProfile
import com.example.medplus.model.QueueItem
import com.example.medplus.data.network.*
import com.example.medplus.dashboard.model.LiveQueueInfo
import com.example.medplus.auth.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository to handle doctor-related data operations in MongoDB.
 */
class DoctorRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = RetrofitClient.getApiService(context)

    /**
     * Converts a document Uri to a Base64 string with Data URI prefix.
     * Keep unchanged since it runs locally and returns base64 string.
     */
    fun uploadDoctorDocument(
        fileUri: Uri,
        documentName: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val contentResolver = context.contentResolver

        try {
            // 1. Validate File Size (Max 300 KB)
            val parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")
            val fileSize = parcelFileDescriptor?.statSize ?: 0
            parcelFileDescriptor?.close()

            if (fileSize > 300 * 1024) {
                onFailure("File is too large. Maximum size is 300 KB.")
                return
            }

            // 2. Determine MIME type
            var mimeType = contentResolver.getType(fileUri)
            if (mimeType == null) {
                val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
                if (extension != null) {
                    mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                }
            }
            val actualMimeType = mimeType ?: "application/octet-stream"

            // 3. Read bytes and convert to Base64
            val inputStream = contentResolver.openInputStream(fileUri)
            val bytes = try {
                inputStream?.use { input ->
                    val buffer = java.io.ByteArrayOutputStream()
                    val data = ByteArray(16384)
                    var nRead: Int
                    while (input.read(data, 0, data.size).also { nRead = it } != -1) {
                        buffer.write(data, 0, nRead)
                    }
                    buffer.toByteArray()
                }
            } catch (e: Exception) {
                null
            }

            if (bytes == null) {
                onFailure("Unable to read file content.")
                return
            }

            val base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            
            // 4. Return as Data URI
            val dataUri = "data:$actualMimeType;base64,$base64Data"
            onSuccess(dataUri)

        } catch (e: Exception) {
            android.util.Log.e("DoctorRepository", "Base64 conversion failed", e)
            onFailure("Failed to process document: ${e.message}")
        }
    }

    /**
     * Submits the doctor profile for verification (updates verificationStatus to PENDING).
     */
    fun submitForVerification(
        uid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch the current doctor profile to retain fields, setting status to PENDING
                val profileResponse = apiService.getDoctorProfile()
                if (profileResponse.isSuccessful) {
                    val profile = profileResponse.body()
                    if (profile != null) {
                        val updatedProfile = profile.copy(verificationStatus = "PENDING")
                        val response = apiService.setupDoctorProfile(updatedProfile)
                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onFailure("Failed to submit verification request")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onFailure("Doctor profile not found. Please complete profile details first.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to retrieve doctor profile details")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to submit verification request")
                }
            }
        }
    }

    /**
     * Saves or updates a doctor's professional profile.
     */
    fun saveDoctorProfile(
        profile: DoctorProfile,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.setupDoctorProfile(profile)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to save doctor profile: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to save profile")
                }
            }
        }
    }

    /**
     * Fetches a doctor's profile by UID.
     */
    fun getDoctorProfile(
        uid: String,
        onSuccess: (DoctorProfile?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getDoctorProfileByUid(uid)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onSuccess(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch profile")
                }
            }
        }
    }

    /**
     * Fetches all VERIFIED doctors for a specific department.
     */
    fun getDoctorsByDepartment(
        department: String,
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getVerifiedDoctors()
                if (response.isSuccessful) {
                    val doctors = response.body() ?: emptyList()
                    val filtered = if (department.isNotBlank()) {
                        doctors.filter { it.department.equals(department, ignoreCase = true) }
                    } else {
                        doctors
                    }
                    withContext(Dispatchers.Main) {
                        onSuccess(filtered)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch verified doctors")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch doctors")
                }
            }
        }
    }

    fun addDoctorFeedback(
        doctorId: String,
        rating: Int,
        feedback: String,
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = com.example.medplus.data.network.SubmitFeedbackRequest(
                    doctorId = doctorId,
                    rating = rating,
                    feedback = feedback,
                    appointmentId = appointmentId
                )
                val response = apiService.submitFeedback(request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to submit feedback"
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to submit feedback")
                }
            }
        }
    }

    /**
     * Checks if an appointment already exists for a doctor at a specific date and time.
     */
    fun checkAppointmentExists(
        doctorId: String,
        date: String,
        time: String,
        onResult: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getDoctorAppointments()
                if (response.isSuccessful) {
                    val exists = response.body()?.any {
                        it.doctorId == doctorId && it.date == date && it.time == time && it.status == "UPCOMING"
                    } ?: false
                    withContext(Dispatchers.Main) {
                        onResult(exists)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to verify slot availability")
                }
            }
        }
    }

    /**
     * Creates a new appointment in MongoDB.
     */
    fun createAppointment(
        doctorId: String,
        doctorName: String,
        department: String,
        date: String,
        time: String,
        onSuccess: (Appointment) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = BookAppointmentRequest(
                    doctorId = doctorId,
                    doctorName = doctorName,
                    department = department,
                    date = date,
                    time = time,
                    reason = "Regular consultation"
                )
                val response = apiService.bookAppointment(request)
                if (response.isSuccessful) {
                    val appointment = response.body()
                    if (appointment != null) {
                        withContext(Dispatchers.Main) {
                            onSuccess(appointment)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onFailure("Appointment confirmation returned empty response")
                        }
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to book appointment"
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to create appointment")
                }
            }
        }
    }

    /**
     * Listen for real-time full queue updates for the doctor (via polling).
     */
    fun getDoctorQueueFlow(date: String? = null): Flow<List<QueueItem>> = flow {
        while (true) {
            try {
                val response = apiService.getDoctorQueue(date)
                if (response.isSuccessful) {
                    emit(response.body() ?: emptyList())
                }
            } catch (e: Exception) {
                android.util.Log.e("DOCTOR_QUEUE_DEBUG", "Polling doctor queue failed", e)
            }
            delay(5000)
        }
    }

    /**
     * Fetches all active queue entries for the doctor.
     */
    fun getDoctorQueue(
        date: String? = null,
        onSuccess: (List<QueueItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getDoctorQueue(date)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to fetch doctor queue")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to fetch doctor queue")
                }
            }
        }
    }

    /**
     * Updates the status of a queue entry.
     */
    fun updateQueueStatus(
        queueId: String,
        doctorId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateQueueStatus(queueId, UpdateQueueStatusRequest(newStatus))
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to update queue status")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to update queue status")
                }
            }
        }
    }

    /**
     * Fetches all unique patients associated with the doctor through appointments.
     */
    fun getDoctorPatients(
        onSuccess: (List<User>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getDoctorPatients()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body() ?: emptyList())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to load doctor patients list")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to load doctor patients")
                }
            }
        }
    }

    /**
     * Updates doctor availability and practice details in MongoDB.
     */
    fun updateDoctorPracticeDetails(
        consultationFee: Double,
        workingDays: List<String>,
        startTime: String,
        endTime: String,
        lunchStart: String,
        lunchEnd: String,
        breakStart: String,
        breakEnd: String,
        slotDuration: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = AvailabilityRequest(
                    workingDays = workingDays,
                    consultationStartTime = startTime,
                    consultationEndTime = endTime,
                    lunchStartTime = lunchStart,
                    lunchEndTime = lunchEnd,
                    breakStartTime = breakStart,
                    breakEndTime = breakEnd,
                    slotDuration = slotDuration,
                    consultationFee = consultationFee
                )
                val response = apiService.updateAvailability(request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to update availability: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to update availability")
                }
            }
        }
    }
}
