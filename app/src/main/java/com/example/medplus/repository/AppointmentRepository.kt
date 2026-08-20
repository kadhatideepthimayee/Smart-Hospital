package com.example.medplus.repository

import com.example.medplus.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

/**
 * Repository to handle appointment-related data operations in Firestore.
 */
class AppointmentRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /**
     * Fetches all appointments for the currently authenticated patient.
     */
    fun getPatientAppointments(
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("appointments")
            .whereEqualTo("patientId", currentUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val appointments = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
                }
                onSuccess(appointments)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch patient appointments")
            }
    }

    /**
     * Cancels an existing appointment by updating its status.
     */
    fun cancelAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("appointments").document(appointmentId)
            .update("status", "CANCELLED")
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to cancel appointment")
            }
    }

    /**
     * Fetches all appointments for the currently authenticated doctor.
     */
    fun getDoctorAppointments(
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("appointments")
            .whereEqualTo("doctorId", currentUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val appointments = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
                }
                onSuccess(appointments)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch doctor appointments")
            }
    }

    fun updateAppointmentStatus(
        appointmentId: String,
        doctorId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("appointments").document(appointmentId)
            .update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update appointment status")
            }
    }

    /**
     * Observes all appointments for the currently authenticated doctor in real-time.
     */
    fun getDoctorAppointmentsFlow(): Flow<List<Appointment>> = callbackFlow {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("appointments")
            .whereEqualTo("doctorId", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
                    }
                    trySend(list)
                }
            }

        awaitClose {
            registration.remove()
        }
    }
}
