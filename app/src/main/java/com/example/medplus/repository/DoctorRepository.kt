package com.example.medplus.repository

import android.content.Context
import android.util.Log
import com.example.medplus.auth.model.User
import com.example.medplus.model.Appointment
import com.example.medplus.model.DoctorProfile
import com.example.medplus.model.QueueItem
import com.example.medplus.data.network.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoctorRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = SessionManager.getInstance(context)
    private val apiService: ApiService get() = RetrofitClient.getClient(context)

    /**
     * Submits the doctor profile for verification (updates verificationStatus to PENDING).
     */
    fun submitForVerification(
        uid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = DoctorProfileRequest(
            fullName = null,
            email = null,
            phone = null,
            qualification = null,
            department = null,
            specialization = null,
            experienceYears = null,
            registrationAuthority = null,
            registrationNumber = null,
            consultationFee = null,
            bio = null,
            profileImage = null,
            registrationCertificateUrl = null,
            verificationDocumentUrl = null,
            workingDays = null,
            consultationStartTime = null,
            consultationEndTime = null,
            lunchStartTime = null,
            lunchEndTime = null,
            breakStartTime = null,
            breakEndTime = null,
            slotDuration = null,
            verificationStatus = "PENDING"
        )
        apiService.updateDoctorProfile(uid, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to submit verification request")
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error submitting verification")
            }
        })
    }

    /**
     * Saves or updates a doctor's professional profile.
     */
    fun saveDoctorProfile(
        profile: DoctorProfile,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = DoctorProfileRequest(
            fullName = profile.fullName,
            email = profile.email,
            phone = profile.phone,
            qualification = profile.qualification,
            department = profile.department,
            specialization = profile.specialization,
            experienceYears = profile.experienceYears,
            registrationAuthority = profile.registrationAuthority,
            registrationNumber = profile.registrationNumber,
            consultationFee = profile.consultationFee,
            bio = profile.bio,
            profileImage = profile.profileImage,
            registrationCertificateUrl = profile.registrationCertificateUrl,
            verificationDocumentUrl = profile.verificationDocumentUrl,
            workingDays = profile.workingDays,
            consultationStartTime = profile.consultationStartTime,
            consultationEndTime = profile.consultationEndTime,
            lunchStartTime = profile.lunchStartTime,
            lunchEndTime = profile.lunchEndTime,
            breakStartTime = profile.breakStartTime,
            breakEndTime = profile.breakEndTime,
            slotDuration = profile.slotDuration,
            verificationStatus = profile.verificationStatus
        )
        apiService.updateDoctorProfile(profile.uid, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to save doctor profile")
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error saving profile")
            }
        })
    }

    /**
     * Fetches a doctor's profile by UID.
     */
    fun getDoctorProfile(
        uid: String,
        onSuccess: (DoctorProfile?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getDoctorProfile(uid).enqueue(object : Callback<DoctorProfileResponse> {
            override fun onResponse(call: Call<DoctorProfileResponse>, response: Response<DoctorProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val profile = DoctorProfile(
                        uid = body.uid,
                        fullName = body.fullName,
                        email = body.email,
                        phone = body.phone ?: "",
                        qualification = body.qualification ?: "",
                        department = body.department ?: "",
                        specialization = body.specialization ?: "",
                        experienceYears = body.experienceYears ?: 0,
                        registrationAuthority = body.registrationAuthority ?: "",
                        registrationNumber = body.registrationNumber ?: "",
                        consultationFee = body.consultationFee ?: 0.0,
                        bio = body.bio ?: "",
                        profileImage = body.profileImage ?: "",
                        registrationCertificateUrl = body.registrationCertificateUrl ?: "",
                        verificationDocumentUrl = body.verificationDocumentUrl ?: "",
                        workingDays = body.workingDays ?: emptyList(),
                        consultationStartTime = body.consultationStartTime ?: "",
                        consultationEndTime = body.consultationEndTime ?: "",
                        lunchStartTime = body.lunchStartTime ?: "",
                        lunchEndTime = body.lunchEndTime ?: "",
                        breakStartTime = body.breakStartTime ?: "",
                        breakEndTime = body.breakEndTime ?: "",
                        slotDuration = body.slotDuration ?: 15,
                        verificationStatus = body.verificationStatus ?: "DRAFT"
                    )
                    onSuccess(profile)
                } else {
                    onSuccess(null)
                }
            }

            override fun onFailure(call: Call<DoctorProfileResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching profile")
            }
        })
    }

    /**
     * Fetches all VERIFIED doctors for a specific department.
     */
    fun getDoctorsByDepartment(
        department: String,
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getDoctors().enqueue(object : Callback<List<DoctorProfileResponse>> {
            override fun onResponse(call: Call<List<DoctorProfileResponse>>, response: Response<List<DoctorProfileResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        DoctorProfile(
                            uid = body.uid,
                            fullName = body.fullName,
                            email = body.email,
                            phone = body.phone ?: "",
                            qualification = body.qualification ?: "",
                            department = body.department ?: "",
                            specialization = body.specialization ?: "",
                            experienceYears = body.experienceYears ?: 0,
                            registrationAuthority = body.registrationAuthority ?: "",
                            registrationNumber = body.registrationNumber ?: "",
                            consultationFee = body.consultationFee ?: 0.0,
                            bio = body.bio ?: "",
                            profileImage = body.profileImage ?: "",
                            registrationCertificateUrl = body.registrationCertificateUrl ?: "",
                            verificationDocumentUrl = body.verificationDocumentUrl ?: "",
                            workingDays = body.workingDays ?: emptyList(),
                            consultationStartTime = body.consultationStartTime ?: "",
                            consultationEndTime = body.consultationEndTime ?: "",
                            lunchStartTime = body.lunchStartTime ?: "",
                            lunchEndTime = body.lunchEndTime ?: "",
                            breakStartTime = body.breakStartTime ?: "",
                            breakEndTime = body.breakEndTime ?: "",
                            slotDuration = body.slotDuration ?: 15,
                            verificationStatus = body.verificationStatus ?: "DRAFT"
                        )
                    }

                    val filtered = if (department.isNotBlank()) {
                        list.filter { it.department.equals(department, ignoreCase = true) }
                    } else {
                        list
                    }
                    onSuccess(filtered)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<DoctorProfileResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching doctors")
            }
        })
    }

    fun addDoctorFeedback(
        doctorId: String,
        rating: Int,
        feedback: String,
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        val request = FeedbackRequest(doctorId, currentUid, rating, feedback, appointmentId)
        apiService.submitFeedback(request).enqueue(object : Callback<FeedbackResponse> {
            override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to submit feedback")
            }

            override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error submitting feedback")
            }
        })
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
        apiService.getDoctorAppointments(doctorId).enqueue(object : Callback<List<AppointmentResponse>> {
            override fun onResponse(call: Call<List<AppointmentResponse>>, response: Response<List<AppointmentResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    val exists = list.any { it.date == date && it.time == time && it.status == "PENDING" }
                    onResult(exists)
                } else {
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<List<AppointmentResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error checking slot")
            }
        })
    }

    /**
     * Creates a new appointment.
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
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        val request = BookAppointmentRequest(
            patientId = currentUid,
            doctorId = doctorId,
            doctorName = doctorName,
            department = department,
            date = date,
            time = time,
            reason = "General consultation"
        )
        apiService.bookAppointment(request).enqueue(object : Callback<AppointmentResponse> {
            override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val appointment = Appointment(
                        appointmentId = body.id,
                        patientId = body.patientId,
                        patientName = body.patientName,
                        doctorId = body.doctorId,
                        doctorName = body.doctorName,
                        department = body.department,
                        date = body.date,
                        time = body.time,
                        status = body.status,
                        tokenNumber = body.tokenNumber,
                        createdAt = Timestamp.now()
                    )
                    onSuccess(appointment)
                } else {
                    onFailure(response.errorBody()?.string() ?: "Failed to book appointment")
                }
            }

            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error booking appointment")
            }
        })
    }

    /**
     * Helper sync fetch method to support Flow polling
     */
    private fun fetchDoctorQueueSync(date: String?): List<QueueItem> {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) return emptyList()

        return try {
            val response = apiService.getQueue(currentUid, date).execute()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { body ->
                    QueueItem(
                        queueId = body.id,
                        appointmentId = body.appointmentId,
                        doctorId = body.doctorId,
                        patientId = body.patientId,
                        patientName = body.patientName,
                        tokenNumber = body.tokenNumber,
                        status = body.status,
                        department = body.department,
                        date = body.date,
                        isActive = body.isActive,
                        estimatedWaitMinutes = body.estimatedWaitMinutes
                    )
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Listen for real-time full queue updates for the doctor (via Flow polling).
     */
    fun getDoctorQueueFlow(date: String? = null): Flow<List<QueueItem>> = flow {
        while (true) {
            emit(fetchDoctorQueueSync(date))
            delay(4000)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    /**
     * Fetches all active queue entries for the doctor.
     */
    fun getDoctorQueue(
        date: String? = null,
        onSuccess: (List<QueueItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getQueue(currentUid, date).enqueue(object : Callback<List<QueueResponse>> {
            override fun onResponse(call: Call<List<QueueResponse>>, response: Response<List<QueueResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        QueueItem(
                            queueId = body.id,
                            appointmentId = body.appointmentId,
                            doctorId = body.doctorId,
                            patientId = body.patientId,
                            patientName = body.patientName,
                            tokenNumber = body.tokenNumber,
                            status = body.status,
                            department = body.department,
                            date = body.date,
                            isActive = body.isActive,
                            estimatedWaitMinutes = body.estimatedWaitMinutes
                        )
                    }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<QueueResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching queue")
            }
        })
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
        val request = StatusRequest(newStatus)
        apiService.updateQueueStatus(queueId, request).enqueue(object : Callback<QueueResponse> {
            override fun onResponse(call: Call<QueueResponse>, response: Response<QueueResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to update queue status")
            }

            override fun onFailure(call: Call<QueueResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error updating queue status")
            }
        })
    }

    /**
     * Fetches all unique patients associated with the doctor through appointments.
     */
    fun getDoctorPatients(
        onSuccess: (List<User>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getDoctorPatients(currentUid).enqueue(object : Callback<List<UserResponse>> {
            override fun onResponse(call: Call<List<UserResponse>>, response: Response<List<UserResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        User(
                            uid = body.uid,
                            fullName = body.fullName,
                            email = body.email,
                            phone = body.phone,
                            role = body.role,
                            profileImage = body.profileImage ?: "",
                            status = body.status ?: "ACTIVE"
                        )
                    }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<UserResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching doctor patients")
            }
        })
    }

    /**
     * Updates doctor availability and practice details.
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
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        val request = DoctorProfileRequest(
            fullName = null,
            email = null,
            phone = null,
            qualification = null,
            department = null,
            specialization = null,
            experienceYears = null,
            registrationAuthority = null,
            registrationNumber = null,
            consultationFee = consultationFee,
            bio = null,
            profileImage = null,
            registrationCertificateUrl = null,
            verificationDocumentUrl = null,
            workingDays = workingDays,
            consultationStartTime = startTime,
            consultationEndTime = endTime,
            lunchStartTime = lunchStart,
            lunchEndTime = lunchEnd,
            breakStartTime = breakStart,
            breakEndTime = breakEnd,
            slotDuration = slotDuration,
            verificationStatus = null
        )

        apiService.updateDoctorProfile(currentUid, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to update practice details")
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error updating practice details")
            }
        })
    }

    fun rescheduleAppointment(
        appointmentId: String,
        doctorId: String,
        doctorName: String,
        department: String,
        date: String,
        time: String,
        onSuccess: (Appointment) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = RescheduleRequest(appointmentId, doctorId, doctorName, department, date, time, reason = "Rescheduling request")
        apiService.rescheduleAppointment(request).enqueue(object : Callback<AppointmentResponse> {
            override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val appointment = Appointment(
                        appointmentId = body.id,
                        patientId = body.patientId,
                        patientName = body.patientName,
                        doctorId = body.doctorId,
                        doctorName = body.doctorName,
                        department = body.department,
                        date = body.date,
                        time = body.time,
                        status = body.status,
                        tokenNumber = body.tokenNumber,
                        createdAt = Timestamp.now()
                    )
                    onSuccess(appointment)
                } else {
                    onFailure(response.errorBody()?.string() ?: "Failed to reschedule appointment")
                }
            }

            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error rescheduling appointment")
            }
        })
    }

    fun uploadDoctorDocument(
        fileUri: android.net.Uri,
        documentName: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                onSuccess("data:image/jpeg;base64,$base64Data")
            } else {
                onFailure("Failed to read document bytes")
            }
        } catch (e: Exception) {
            onFailure(e.message ?: "Failed to upload document")
        }
    }
}
