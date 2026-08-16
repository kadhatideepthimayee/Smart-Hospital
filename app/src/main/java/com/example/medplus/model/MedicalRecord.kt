package com.example.medplus.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MedicalRecord(
    @SerializedName("_id") val recordId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val appointmentId: String = "",
    val diagnosis: String = "",
    val prescription: String = "",
    val notes: String = "",
    val followUpDate: String = "",
    val createdAt: String = ""
) : Serializable
