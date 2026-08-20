package com.example.medplus.repository

import android.net.Uri
import com.example.medplus.model.Appointment
import com.example.medplus.model.DoctorFeedback
import com.example.medplus.model.DoctorProfile
import com.example.medplus.model.QueueItem
import com.example.medplus.auth.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository to handle doctor-related data operations in Firestore.
 */
class DoctorRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /**
     * Converts a document Uri to a Base64 string with Data URI prefix.
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
        firestore.collection("doctor_profiles").document(uid)
            .update("verificationStatus", "PENDING")
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to submit verification request")
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
        firestore.collection("doctor_profiles").document(profile.uid).set(profile, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to save doctor profile")
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
        firestore.collection("doctor_profiles").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(doc.toObject(DoctorProfile::class.java))
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch profile")
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
        firestore.collection("doctor_profiles")
            .whereEqualTo("verificationStatus", "VERIFIED")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val doctors = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(DoctorProfile::class.java)
                }
                val filtered = if (department.isNotBlank()) {
                    doctors.filter { it.department.equals(department, ignoreCase = true) }
                } else {
                    doctors
                }
                onSuccess(filtered)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch verified doctors")
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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val feedbackMap = hashMapOf(
            "doctorId" to doctorId,
            "patientId" to currentUid,
            "rating" to rating,
            "feedback" to feedback,
            "appointmentId" to appointmentId,
            "createdAt" to Timestamp.now()
        )

        firestore.collection("feedback")
            .add(feedbackMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to submit feedback")
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
        firestore.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", date)
            .whereEqualTo("time", time)
            .whereEqualTo("status", "UPCOMING")
            .get()
            .addOnSuccessListener { querySnapshot ->
                onResult(!querySnapshot.isEmpty)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to verify slot availability")
            }
    }

    /**
     * Creates a new appointment in Firestore.
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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        // Fetch patient details first
        firestore.collection("users").document(currentUid).get()
            .addOnSuccessListener { patientDoc ->
                val patientName = patientDoc.getString("fullName") ?: "Patient"

                // Fetch doctor profile to get consultationStartTime and slotDuration
                firestore.collection("doctor_profiles").document(doctorId).get()
                    .addOnSuccessListener { doctorDoc ->
                        val startTimeStr = doctorDoc.getString("consultationStartTime") ?: "09:00"
                        val slotDuration = doctorDoc.getLong("slotDuration")?.toInt() ?: 15

                        // Query existing appointments for doctor on this date to count bookings for the hour
                        firestore.collection("appointments")
                            .whereEqualTo("doctorId", doctorId)
                            .whereEqualTo("date", date)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val dayBookings = querySnapshot.documents
                                val existingBookings = dayBookings.count { it.getString("time") == time }

                                val diffMinutes = timeToMinutes(time) - timeToMinutes(startTimeStr)
                                val baseToken = (diffMinutes / slotDuration).coerceAtLeast(1)
                                val tokenNumber = (baseToken + existingBookings).toString()

                                val newAppointmentRef = firestore.collection("appointments").document()
                                val newAppointment = Appointment(
                                    appointmentId = newAppointmentRef.id,
                                    patientId = currentUid,
                                    patientName = patientName,
                                    doctorId = doctorId,
                                    doctorName = doctorName,
                                    department = department,
                                    date = date,
                                    time = time,
                                    status = "UPCOMING",
                                    tokenNumber = tokenNumber,
                                    createdAt = Timestamp.now()
                                )

                                newAppointmentRef.set(newAppointment)
                                    .addOnSuccessListener {
                                        // Also create a queue item for this appointment
                                        val queueRef = firestore.collection("queue").document()
                                        val queueItem = QueueItem(
                                            queueId = queueRef.id,
                                            appointmentId = newAppointmentRef.id,
                                            patientId = currentUid,
                                            patientName = patientName,
                                            doctorId = doctorId,
                                            tokenNumber = tokenNumber,
                                            status = "WAITING",
                                            isActive = true,
                                            date = date
                                        )
                                        queueRef.set(queueItem)
                                            .addOnSuccessListener {
                                                // 1. Create a notification for the patient
                                                val notificationRef = firestore.collection("notifications").document()
                                                val notificationData = hashMapOf(
                                                    "userId" to currentUid,
                                                    "title" to "Appointment Booked",
                                                    "message" to "Your appointment with $doctorName ($department) is confirmed for $date at $time.",
                                                    "type" to "APPOINTMENT",
                                                    "read" to false,
                                                    "isRead" to false,
                                                    "timestamp" to com.google.firebase.Timestamp.now()
                                                )
                                                notificationRef.set(notificationData)

                                                // 2. Create a notification for the doctor
                                                val doctorNotificationRef = firestore.collection("notifications").document()
                                                val doctorNotificationData = hashMapOf(
                                                    "userId" to doctorId,
                                                    "title" to "New Appointment Booked",
                                                    "message" to "New appointment booked by $patientName for $date at $time.",
                                                    "type" to "APPOINTMENT",
                                                    "read" to false,
                                                    "isRead" to false,
                                                    "timestamp" to com.google.firebase.Timestamp.now()
                                                )
                                                doctorNotificationRef.set(doctorNotificationData)

                                                // 3. Create an activity log for the patient
                                                val activityRef = firestore.collection("activities").document()
                                                val timestampStr = java.text.SimpleDateFormat("MMM d, yyyy h:mm a", java.util.Locale.US).format(java.util.Date())
                                                val activityData = hashMapOf(
                                                    "userId" to currentUid,
                                                    "type" to "APPOINTMENT",
                                                    "title" to "Booked Appointment",
                                                    "description" to "Booked with $doctorName for $date at $time.",
                                                    "timestamp" to timestampStr
                                                )
                                                activityRef.set(activityData)

                                                onSuccess(newAppointment)
                                            }
                                            .addOnFailureListener { e ->
                                                onFailure(e.message ?: "Failed to initialize queue item")
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        onFailure(e.message ?: "Failed to book appointment")
                                    }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Failed to count appointments")
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to fetch doctor profile details")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch patient information")
            }
    }

    /**
     * Listen for real-time full queue updates for the doctor (via Firestore snapshots).
     */
    fun getDoctorQueueFlow(date: String? = null): Flow<List<QueueItem>> = callbackFlow {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var query = firestore.collection("queue")
            .whereEqualTo("doctorId", currentUid)
            .whereEqualTo("isActive", true)

        if (date != null) {
            query = query.whereEqualTo("date", date)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(QueueItem::class.java)?.copy(queueId = doc.id)
                }
                trySend(list)
            }
        }

        awaitClose {
            registration.remove()
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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        var query = firestore.collection("queue")
            .whereEqualTo("doctorId", currentUid)

        if (date != null) {
            query = query.whereEqualTo("date", date)
        }

        query.get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(QueueItem::class.java)?.copy(queueId = doc.id)
                }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch doctor queue")
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
        firestore.collection("queue").document(queueId)
            .update("status", newStatus)
            .addOnSuccessListener {
                val isActive = !(newStatus == "COMPLETED" || newStatus == "CANCELLED")
                firestore.collection("queue").document(queueId)
                    .update("isActive", isActive)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onSuccess() }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update queue status")
            }
    }

    /**
     * Fetches all unique patients associated with the doctor through appointments.
     */
    fun getDoctorPatients(
        onSuccess: (List<User>) -> Unit,
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
                val patientIds = querySnapshot.documents.mapNotNull { it.getString("patientId") }.distinct()
                if (patientIds.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val userTasks = patientIds.chunked(10).map { chunk ->
                    firestore.collection("users")
                        .whereIn("uid", chunk)
                        .get()
                }

                val users = mutableListOf<User>()
                var completedCount = 0
                
                userTasks.forEach { task ->
                    task.addOnSuccessListener { snapshot ->
                        users.addAll(snapshot.documents.mapNotNull { it.toObject(User::class.java) })
                        completedCount++
                        if (completedCount == userTasks.size) {
                            onSuccess(users)
                        }
                    }.addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to fetch patient users")
                    }
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load doctor appointments")
            }
    }

    /**
     * Updates doctor availability and practice details in Firestore.
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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        val updates = hashMapOf(
            "consultationFee" to consultationFee,
            "workingDays" to workingDays,
            "consultationStartTime" to startTime,
            "consultationEndTime" to endTime,
            "lunchStartTime" to lunchStart,
            "lunchEndTime" to lunchEnd,
            "breakStartTime" to breakStart,
            "breakEndTime" to breakEnd,
            "slotDuration" to slotDuration
        )

        firestore.collection("doctor_profiles").document(currentUid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update availability")
            }
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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        // Fetch patient details first
        firestore.collection("users").document(currentUid).get()
            .addOnSuccessListener { patientDoc ->
                val patientName = patientDoc.getString("fullName") ?: "Patient"

                // Fetch doctor profile to get consultationStartTime and slotDuration
                firestore.collection("doctor_profiles").document(doctorId).get()
                    .addOnSuccessListener { doctorDoc ->
                        val startTimeStr = doctorDoc.getString("consultationStartTime") ?: "09:00"
                        val slotDuration = doctorDoc.getLong("slotDuration")?.toInt() ?: 15

                        // Query existing appointments for doctor on this date to count bookings for the hour
                        firestore.collection("appointments")
                            .whereEqualTo("doctorId", doctorId)
                            .whereEqualTo("date", date)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val dayBookings = querySnapshot.documents
                                val existingBookings = dayBookings.count { it.getString("time") == time }

                                val diffMinutes = timeToMinutes(time) - timeToMinutes(startTimeStr)
                                val baseToken = (diffMinutes / slotDuration).coerceAtLeast(1)
                                val tokenNumber = (baseToken + existingBookings).toString()

                                val appointmentRef = firestore.collection("appointments").document(appointmentId)
                                
                                val updatedAppointment = Appointment(
                                    appointmentId = appointmentId,
                                    patientId = currentUid,
                                    patientName = patientName,
                                    doctorId = doctorId,
                                    doctorName = doctorName,
                                    department = department,
                                    date = date,
                                    time = time,
                                    status = "UPCOMING",
                                    tokenNumber = tokenNumber,
                                    createdAt = Timestamp.now()
                                )

                                appointmentRef.set(updatedAppointment, SetOptions.merge())
                                    .addOnSuccessListener {
                                        // Find existing queue item for this appointment
                                        firestore.collection("queue")
                                            .whereEqualTo("appointmentId", appointmentId)
                                            .get()
                                            .addOnSuccessListener { queueSnapshot ->
                                                val queueDocs = queueSnapshot.documents
                                                if (queueDocs.isNotEmpty()) {
                                                    val queueItemDoc = queueDocs[0]
                                                    firestore.collection("queue").document(queueItemDoc.id)
                                                        .update(
                                                            mapOf(
                                                                "date" to date,
                                                                "tokenNumber" to tokenNumber,
                                                                "status" to "WAITING",
                                                                "isActive" to true
                                                            )
                                                        )
                                                        .addOnSuccessListener {
                                                            createRescheduleNotifications(currentUid, patientName, doctorId, doctorName, department, date, time, onSuccess, onFailure, updatedAppointment)
                                                        }
                                                        .addOnFailureListener { e ->
                                                            onFailure(e.message ?: "Failed to update queue item")
                                                        }
                                                } else {
                                                    // Create new queue item
                                                    val queueRef = firestore.collection("queue").document()
                                                    val queueItem = QueueItem(
                                                        queueId = queueRef.id,
                                                        appointmentId = appointmentId,
                                                        patientId = currentUid,
                                                        patientName = patientName,
                                                        doctorId = doctorId,
                                                        tokenNumber = tokenNumber,
                                                        status = "WAITING",
                                                        isActive = true,
                                                        date = date
                                                    )
                                                    queueRef.set(queueItem)
                                                        .addOnSuccessListener {
                                                            createRescheduleNotifications(currentUid, patientName, doctorId, doctorName, department, date, time, onSuccess, onFailure, updatedAppointment)
                                                        }
                                                        .addOnFailureListener { e ->
                                                            onFailure(e.message ?: "Failed to initialize queue item")
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                onFailure(e.message ?: "Failed to query existing queue item")
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        onFailure(e.message ?: "Failed to reschedule appointment")
                                    }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Failed to count appointments")
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to fetch doctor profile details")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch patient information")
            }
    }

    private fun createRescheduleNotifications(
        currentUid: String,
        patientName: String,
        doctorId: String,
        doctorName: String,
        department: String,
        date: String,
        time: String,
        onSuccess: (Appointment) -> Unit,
        onFailure: (String) -> Unit,
        appointment: Appointment
    ) {
        // 1. Create a notification for the patient
        val notificationRef = firestore.collection("notifications").document()
        val notificationData = hashMapOf(
            "userId" to currentUid,
            "title" to "Appointment Rescheduled",
            "message" to "Your appointment with $doctorName ($department) has been rescheduled to $date at $time.",
            "type" to "APPOINTMENT",
            "read" to false,
            "isRead" to false,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        notificationRef.set(notificationData)

        // 2. Create a notification for the doctor
        val doctorNotificationRef = firestore.collection("notifications").document()
        val doctorNotificationData = hashMapOf(
            "userId" to doctorId,
            "title" to "Appointment Rescheduled by Patient",
            "message" to "Appointment has been rescheduled by $patientName to $date at $time.",
            "type" to "APPOINTMENT",
            "read" to false,
            "isRead" to false,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        doctorNotificationRef.set(doctorNotificationData)

        // 3. Create an activity log for the patient
        val activityRef = firestore.collection("activities").document()
        val timestampStr = java.text.SimpleDateFormat("MMM d, yyyy h:mm a", java.util.Locale.US).format(java.util.Date())
        val activityData = hashMapOf(
            "userId" to currentUid,
            "type" to "APPOINTMENT",
            "title" to "Rescheduled Appointment",
            "description" to "Rescheduled with $doctorName to $date at $time.",
            "timestamp" to timestampStr
        )
        activityRef.set(activityData)
            .addOnSuccessListener {
                onSuccess(appointment)
            }
            .addOnFailureListener {
                onSuccess(appointment)
            }
    }

    private fun timeToMinutes(timeStr: String?): Int {
        if (timeStr.isNullOrEmpty()) return 0
        val cleanTime = timeStr.trim()
        val locales = listOf(java.util.Locale.US, java.util.Locale.ENGLISH, java.util.Locale.getDefault())
        val formats = listOf("hh:mm a", "h:mm a", "HH:mm", "H:mm")
        
        for (locale in locales) {
            for (formatStr in formats) {
                try {
                    val sdf = java.text.SimpleDateFormat(formatStr, locale)
                    val date = sdf.parse(cleanTime)
                    if (date != null) {
                        val cal = java.util.Calendar.getInstance()
                        cal.time = date
                        return cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
                    }
                } catch (_: Exception) {}
            }
        }
        
        return try {
            val timeParts = cleanTime.split(" ")
            val hm = timeParts[0].split(":")
            var hour = hm[0].toInt()
            val minute = hm[1].toInt()
            if (timeParts.size > 1) {
                val ampm = timeParts[1].uppercase(java.util.Locale.ROOT)
                if (ampm.contains("PM") && hour < 12) hour += 12
                if (ampm.contains("AM") && hour == 12) hour = 0
            }
            hour * 60 + minute
        } catch (e: Exception) {
            0
        }
    }
}
