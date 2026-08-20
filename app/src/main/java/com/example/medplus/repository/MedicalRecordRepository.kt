package com.example.medplus.repository

import android.content.Context
import com.example.medplus.model.MedicalRecord
import com.example.medplus.data.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MedicalRecordRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = SessionManager.getInstance(context)
    private val apiService: ApiService get() = RetrofitClient.getClient(context)

    fun createMedicalRecord(
        appointmentId: String,
        patientId: String,
        diagnosis: String,
        prescription: String,
        notes: String,
        followUpDate: String,
        onSuccess: (MedicalRecord) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = MedicalRecordRequest(appointmentId, patientId, diagnosis, prescription, notes, followUpDate)
        apiService.createMedicalRecord(request).enqueue(object : Callback<MedicalRecordResponse> {
            override fun onResponse(call: Call<MedicalRecordResponse>, response: Response<MedicalRecordResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val record = MedicalRecord(
                        recordId = body.id,
                        patientId = body.patientId,
                        patientName = body.patientName ?: "",
                        doctorId = body.doctorId ?: "",
                        doctorName = body.doctorName ?: "",
                        appointmentId = body.appointmentId ?: "",
                        diagnosis = body.diagnosis ?: "",
                        prescription = body.prescription ?: "",
                        notes = body.notes ?: "",
                        followUpDate = body.followUpDate ?: "",
                        createdAt = body.createdAt ?: ""
                    )
                    onSuccess(record)
                } else {
                    onFailure(response.errorBody()?.string() ?: "Failed to save medical record")
                }
            }

            override fun onFailure(call: Call<MedicalRecordResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error saving medical record")
            }
        })
    }

    fun getPatientMedicalRecords(
        onSuccess: (List<MedicalRecord>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getPatientMedicalRecords(currentUid).enqueue(object : Callback<List<MedicalRecordResponse>> {
            override fun onResponse(call: Call<List<MedicalRecordResponse>>, response: Response<List<MedicalRecordResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        MedicalRecord(
                            recordId = body.id,
                            patientId = body.patientId,
                            patientName = body.patientName ?: "",
                            doctorId = body.doctorId ?: "",
                            doctorName = body.doctorName ?: "",
                            appointmentId = body.appointmentId ?: "",
                            diagnosis = body.diagnosis ?: "",
                            prescription = body.prescription ?: "",
                            notes = body.notes ?: "",
                            followUpDate = body.followUpDate ?: "",
                            createdAt = body.createdAt ?: ""
                        )
                    }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<MedicalRecordResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching medical records")
            }
        })
    }

    fun getMedicalRecordDetails(
        recordId: String,
        onSuccess: (MedicalRecord) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getMedicalRecordById(recordId).enqueue(object : Callback<MedicalRecordResponse> {
            override fun onResponse(call: Call<MedicalRecordResponse>, response: Response<MedicalRecordResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val record = MedicalRecord(
                        recordId = body.id,
                        patientId = body.patientId,
                        patientName = body.patientName ?: "",
                        doctorId = body.doctorId ?: "",
                        doctorName = body.doctorName ?: "",
                        appointmentId = body.appointmentId ?: "",
                        diagnosis = body.diagnosis ?: "",
                        prescription = body.prescription ?: "",
                        notes = body.notes ?: "",
                        followUpDate = body.followUpDate ?: "",
                        createdAt = body.createdAt ?: ""
                    )
                    onSuccess(record)
                } else {
                    onFailure("Medical record not found")
                }
            }

            override fun onFailure(call: Call<MedicalRecordResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching medical record details")
            }
        })
    }
}
