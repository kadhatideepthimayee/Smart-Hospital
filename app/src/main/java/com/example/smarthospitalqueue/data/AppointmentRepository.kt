package com.example.smarthospitalqueue.data

import com.example.smarthospitalqueue.data.model.AppointmentModel
import com.example.smarthospitalqueue.data.model.QueueModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object AppointmentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val uid get() = auth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val patientName get() = auth.currentUser?.displayName
        ?: auth.currentUser?.email?.substringBefore("@")
        ?: "Patient"

    // ── Generate token number ─────────────────────────────────────────────
    // Counts existing appointments for that doctor+date and assigns next token
    private suspend fun generateToken(doctorId: String, date: String): Pair<String, Int> {
        val snapshot = db.collectionGroup("appointments")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("dateRaw", date)
            .whereEqualTo("status", "Confirmed")
            .get()
            .await()

        val position = snapshot.size() + 1
        val token = "T-${position.toString().padStart(3, '0')}"
        return Pair(token, position)
    }

    // ── Save appointment + create queue entry ─────────────────────────────
    suspend fun bookAppointment(
        doctorId: String,
        doctorName: String,
        specialization: String,
        department: String,
        hospital: String,
        roomNumber: String,
        consultationFee: Int,
        date: String,        // "Tuesday, 3 June 2025"
        dateRaw: String,     // "2025-06-03"
        time: String
    ): Result<AppointmentModel> {
        return try {
            val (token, position) = generateToken(doctorId, dateRaw)

            val appointment = AppointmentModel(
                patientId       = uid,
                patientName     = patientName,
                doctorId        = doctorId,
                doctorName      = doctorName,
                specialization  = specialization,
                department      = department,
                hospital        = hospital,
                roomNumber      = roomNumber,
                consultationFee = consultationFee,
                date            = date,
                dateRaw         = dateRaw,
                time            = time,
                tokenNumber     = token,
                queuePosition   = position,
                status          = "Confirmed"
            )

            // Save to users/{uid}/appointments/
            val ref = db.collection("users")
                .document(uid)
                .collection("appointments")
                .add(appointment)
                .await()

            // Also save to global appointments/ for admin & queue management
            db.collection("appointments")
                .document(ref.id)
                .set(appointment.copy(appointmentId = ref.id))
                .await()

            // Create queue entry
            val queue = QueueModel(
                appointmentId       = ref.id,
                patientId           = uid,
                doctorId            = doctorId,
                doctorName          = doctorName,
                department          = department,
                roomNumber          = roomNumber,
                tokenNumber         = token,
                tokenNumberInt      = position,
                currentServing      = maxOf(1, position - 5),
                totalAhead          = maxOf(0, position - maxOf(1, position - 5)),
                estimatedWaitMinutes = position * 5,
                status              = "In Queue",
                isActive            = true
            )

            db.collection("users")
                .document(uid)
                .collection("queue_status")
                .document(ref.id)
                .set(queue)
                .await()

            // Update BookingRepository so other screens work immediately
            BookingRepository.hasActiveBooking    = true
            BookingRepository.bookedToken         = token
            BookingRepository.bookedDoctorName    = doctorName
            BookingRepository.bookedSpecialization = specialization
            BookingRepository.bookedDepartment    = department
            BookingRepository.bookedDate          = date
            BookingRepository.bookedTime          = time
            BookingRepository.bookedHospital      = hospital
            BookingRepository.bookedFee           = "₹$consultationFee"

            Result.success(appointment.copy(appointmentId = ref.id))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Fetch upcoming appointments ───────────────────────────────────────
    suspend fun getUpcomingAppointments(): List<AppointmentModel> {
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("appointments")
                .whereEqualTo("status", "Confirmed")
                .orderBy("dateRaw", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(AppointmentModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Fetch latest appointment ──────────────────────────────────────────
    suspend fun getLatestAppointment(): AppointmentModel? {
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("appointments")
                .whereEqualTo("status", "Confirmed")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            snapshot.toObjects(AppointmentModel::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}