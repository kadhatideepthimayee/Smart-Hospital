package com.example.medplus.repository

import android.content.Context
import com.example.medplus.model.Appointment
import com.example.medplus.data.network.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = SessionManager.getInstance(context)
    private val apiService: ApiService get() = RetrofitClient.getClient(context)

    /**
     * Fetches all appointments for the currently authenticated patient.
     */
    fun getPatientAppointments(
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getPatientAppointments(currentUid).enqueue(object : Callback<List<AppointmentResponse>> {
            override fun onResponse(call: Call<List<AppointmentResponse>>, response: Response<List<AppointmentResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        Appointment(
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
                    }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<AppointmentResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching appointments")
            }
        })
    }

    /**
     * Cancels an existing appointment by updating its status.
     */
    fun cancelAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = StatusRequest("CANCELLED")
        apiService.updateAppointmentStatus(appointmentId, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to cancel appointment")
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error cancelling appointment")
            }
        })
    }

    /**
     * Fetches all appointments for the currently authenticated doctor.
     */
    fun getDoctorAppointments(
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getDoctorAppointments(currentUid).enqueue(object : Callback<List<AppointmentResponse>> {
            override fun onResponse(call: Call<List<AppointmentResponse>>, response: Response<List<AppointmentResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        Appointment(
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
                    }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<AppointmentResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching doctor appointments")
            }
        })
    }

    fun updateAppointmentStatus(
        appointmentId: String,
        doctorId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = StatusRequest(newStatus)
        apiService.updateAppointmentStatus(appointmentId, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to update appointment status")
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error updating status")
            }
        })
    }

    private fun fetchDoctorAppointmentsSync(): List<Appointment> {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) return emptyList()
        return try {
            val response = apiService.getDoctorAppointments(currentUid).execute()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { body ->
                    Appointment(
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
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Observes all appointments for the currently authenticated doctor in real-time.
     */
    fun getDoctorAppointmentsFlow(): Flow<List<Appointment>> = flow {
        while (true) {
            emit(fetchDoctorAppointmentsSync())
            delay(5000)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    fun getAppointment(
        appointmentId: String,
        onSuccess: (Appointment) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getAppointmentDetails(appointmentId).enqueue(object : Callback<AppointmentResponse> {
            override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val appt = Appointment(
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
                    onSuccess(appt)
                } else {
                    onFailure("Failed to get appointment details")
                }
            }

            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching appointment details")
            }
        })
    }

    fun getFeedbackForAppointment(
        appointmentId: String,
        onSuccess: (FeedbackCheckResponse) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getFeedbackForAppointment(appointmentId).enqueue(object : Callback<FeedbackCheckResponse> {
            override fun onResponse(call: Call<FeedbackCheckResponse>, response: Response<FeedbackCheckResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!)
                } else {
                    onFailure("Failed to check feedback status")
                }
            }

            override fun onFailure(call: Call<FeedbackCheckResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error checking feedback status")
            }
        })
    }
}
