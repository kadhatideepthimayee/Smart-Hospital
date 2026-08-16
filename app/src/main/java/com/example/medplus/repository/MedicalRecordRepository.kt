package com.example.medplus.repository

import com.example.medplus.model.MedicalRecord
import com.example.medplus.data.network.RetrofitClient
import com.example.medplus.data.network.CreateMedicalRecordRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicalRecordRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = RetrofitClient.getApiService(context)

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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = CreateMedicalRecordRequest(
                    appointmentId = appointmentId,
                    patientId = patientId,
                    diagnosis = diagnosis,
                    prescription = prescription,
                    notes = notes,
                    followUpDate = followUpDate
                )
                val response = apiService.createMedicalRecord(request)
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body()!!)
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to save medical record"
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to save medical record")
                }
            }
        }
    }

    fun getPatientMedicalRecords(
        onSuccess: (List<MedicalRecord>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getPatientMedicalRecords()
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body()!!)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to retrieve medical records")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to retrieve medical records")
                }
            }
        }
    }

    fun getMedicalRecordDetails(
        recordId: String,
        onSuccess: (MedicalRecord) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getMedicalRecordDetails(recordId)
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body()!!)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to retrieve medical record details")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to retrieve details")
                }
            }
        }
    }
}
