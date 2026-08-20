package com.example.medplus.repository

import com.example.medplus.model.MedicalRecord
import com.example.medplus.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicalRecordRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        // 1. Fetch appointment details
        firestore.collection("appointments").document(appointmentId).get()
            .addOnSuccessListener { apptDoc ->
                val appointment = apptDoc.toObject(Appointment::class.java)
                if (appointment == null) {
                    onFailure("Appointment not found")
                    return@addOnSuccessListener
                }

                // Format current time
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val currentDateStr = sdf.format(Date())

                // 2. Prepare MedicalRecord object
                val recordData = hashMapOf(
                    "patientId" to appointment.patientId,
                    "patientName" to appointment.patientName,
                    "doctorId" to currentUid,
                    "doctorName" to appointment.doctorName,
                    "appointmentId" to appointmentId,
                    "diagnosis" to diagnosis,
                    "prescription" to prescription,
                    "notes" to notes,
                    "followUpDate" to followUpDate,
                    "createdAt" to currentDateStr
                )

                // 3. Save to firestore
                firestore.collection("medical_records").add(recordData)
                    .addOnSuccessListener { ref ->
                        val record = MedicalRecord(
                            recordId = ref.id,
                            patientId = appointment.patientId,
                            patientName = appointment.patientName,
                            doctorId = currentUid,
                            doctorName = appointment.doctorName,
                            appointmentId = appointmentId,
                            diagnosis = diagnosis,
                            prescription = prescription,
                            notes = notes,
                            followUpDate = followUpDate,
                            createdAt = currentDateStr
                        )

                        // 4. Send notification to patient
                        val notificationData = hashMapOf(
                            "userId" to appointment.patientId,
                            "title" to "New Medical Record Available",
                            "message" to "Dr. ${appointment.doctorName} has added a medical record for your consultation.",
                            "type" to "MEDICAL_RECORD",
                            "read" to false,
                            "isRead" to false,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        
                        firestore.collection("notifications").add(notificationData)
                            .addOnSuccessListener {
                                onSuccess(record)
                            }
                            .addOnFailureListener {
                                // Still return success since the record itself was saved successfully
                                onSuccess(record)
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to save medical record")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to retrieve appointment details")
            }
    }

    fun getPatientMedicalRecords(
        onSuccess: (List<MedicalRecord>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("medical_records")
            .whereEqualTo("patientId", currentUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val records = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(MedicalRecord::class.java)?.copy(recordId = doc.id)
                }
                onSuccess(records)
            }
            .addOnFailureListener { e ->
                // Fallback: try fetching without ordering in case index is not built yet
                firestore.collection("medical_records")
                    .whereEqualTo("patientId", currentUid)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val records = querySnapshot.documents.mapNotNull { doc ->
                            doc.toObject(MedicalRecord::class.java)?.copy(recordId = doc.id)
                        }.sortedByDescending { it.createdAt }
                        onSuccess(records)
                    }
                    .addOnFailureListener {
                        onFailure(e.message ?: "Failed to retrieve medical records")
                    }
            }
    }

    fun getMedicalRecordDetails(
        recordId: String,
        onSuccess: (MedicalRecord) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("medical_records").document(recordId).get()
            .addOnSuccessListener { doc ->
                val record = doc.toObject(MedicalRecord::class.java)?.copy(recordId = doc.id)
                if (record != null) {
                    onSuccess(record)
                } else {
                    onFailure("Medical record not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to retrieve details")
            }
    }
}
