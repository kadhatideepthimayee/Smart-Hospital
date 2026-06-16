package com.example.smarthospitalqueue.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AppointmentModel(
    @DocumentId
    val appointmentId: String = "",

    val patientId: String = "",
    val patientName: String = "",

    val doctorId: String = "",
    val doctorName: String = "",
    val specialization: String = "",
    val department: String = "",
    val hospital: String = "",
    val roomNumber: String = "",
    val consultationFee: Int = 0,

    val date: String = "",           // "Tuesday, 3 June 2025"
    val dateRaw: String = "",        // "2025-06-03" for sorting/querying
    val time: String = "",           // "10:00 AM"

    val tokenNumber: String = "",    // "T-052"
    val queuePosition: Int = 0,
    val status: String = "Confirmed", // Confirmed / Cancelled / Completed

    @ServerTimestamp
    val createdAt: Date? = null
)

data class QueueModel(
    @DocumentId
    val queueId: String = "",

    val appointmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val department: String = "",
    val roomNumber: String = "",

    val tokenNumber: String = "",
    val tokenNumberInt: Int = 0,
    val currentServing: Int = 0,
    val totalAhead: Int = 0,
    val estimatedWaitMinutes: Int = 0,

    val status: String = "In Queue",  // In Queue / Next / Consulting / Done
    val isActive: Boolean = true,

    @ServerTimestamp
    val updatedAt: Date? = null
)