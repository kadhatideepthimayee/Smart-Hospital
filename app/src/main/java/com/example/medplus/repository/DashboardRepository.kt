package com.example.medplus.repository

import com.example.medplus.auth.model.User
import com.example.medplus.data.network.SessionManager
import com.example.medplus.dashboard.model.*
import com.example.medplus.model.Appointment
import com.example.medplus.model.Notification
import com.example.medplus.model.QueueItem
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = SessionManager.getInstance(context)

    fun getCurrentUser(
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("users").document(currentUid).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure("User details not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to get current user details")
            }
    }

    /**
     * Update user profile in Firestore
     */
    fun updateUserProfile(
        fullName: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("users").document(currentUid)
            .update(
                mapOf(
                    "fullName" to fullName,
                    "phone" to phone
                )
            )
            .addOnSuccessListener {
                sessionManager.updateProfile(fullName, phone)
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update profile details")
            }
    }

    /**
     * Get unread notification count for the current user
     */
    fun getUnreadNotificationCount(
        onSuccess: (Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("notifications")
            .whereEqualTo("userId", currentUid)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.size())
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch unread notification count")
            }
    }

    /**
     * Get all notifications for the current user
     */
    fun getNotifications(
        onSuccess: (List<Notification>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("notifications")
            .whereEqualTo("userId", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val notifications = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(id = doc.id)
                }
                onSuccess(notifications)
            }
            .addOnFailureListener { e ->
                // Fallback in case orderBy requires an index that isn't built yet
                firestore.collection("notifications")
                    .whereEqualTo("userId", currentUid)
                    .get()
                    .addOnSuccessListener { innerSnapshot ->
                        val notifications = innerSnapshot.documents.mapNotNull { doc ->
                            doc.toObject(Notification::class.java)?.copy(id = doc.id)
                        }.sortedByDescending { it.timestamp }
                        onSuccess(notifications)
                    }
                    .addOnFailureListener {
                        onFailure(e.message ?: "Failed to fetch notifications")
                    }
            }
    }

    /**
     * Mark a notification as read
     */
    fun markNotificationAsRead(id: String) {
        firestore.collection("notifications").document(id)
            .update(
                mapOf(
                    "read" to true,
                    "isRead" to true
                )
            )
            .addOnFailureListener { e ->
                android.util.Log.e("DashboardRepository", "Failed to mark notification read", e)
            }
    }

    /**
     * Delete a notification
     */
    fun deleteNotification(id: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("notifications").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to delete notification")
            }
    }

    /**
     * Mark all notifications as read for current user
     */
    fun markAllNotificationsAsRead(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("notifications")
            .whereEqualTo("userId", currentUid)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, mapOf("read" to true, "isRead" to true))
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e.message ?: "Failed to commit mark all read batch") }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch notifications to mark read")
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

    private fun parseAppointmentDateTime(dateStr: String, timeStr: String): Date? {
        val format = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.ENGLISH)
        try {
            return format.parse("$dateStr $timeStr")
        } catch (e: Exception) {
            val fallbackFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)
            try {
                return fallbackFormat.parse("$dateStr $timeStr")
            } catch (e2: Exception) {
                return null
            }
        }
    }

    private fun getUpcomingAppointmentSync(uid: String): UpcomingAppointment? {
        return try {
            val snapshot = Tasks.await(
                firestore.collection("appointments")
                    .whereEqualTo("patientId", uid)
                    .get()
            )
            
            // Filter non-cancelled appointments that are today or in the future
            val upcoming = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
            }.filter { appt ->
                val isCorrectStatus = appt.status == "UPCOMING" || appt.status == "IN_PROGRESS"
                if (!isCorrectStatus) return@filter false
                
                val apptDate = parseAppointmentDateTime(appt.date, appt.time)
                if (apptDate != null) {
                    val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
                    apptDate.time > twoHoursAgo
                } else {
                    true
                }
            }.sortedWith { appt1, appt2 ->
                val d1 = parseAppointmentDateTime(appt1.date, appt1.time)
                val d2 = parseAppointmentDateTime(appt2.date, appt2.time)
                when {
                    d1 == null && d2 == null -> 0
                    d1 == null -> 1
                    d2 == null -> -1
                    else -> d1.compareTo(d2)
                }
            }.firstOrNull()

            if (upcoming != null) {
                UpcomingAppointment(
                    appointmentId = upcoming.appointmentId,
                    doctorName = upcoming.doctorName,
                    department = upcoming.department,
                    status = upcoming.status,
                    date = upcoming.date,
                    time = upcoming.time
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getUpcomingAppointmentFlow(uid: String): Flow<Appointment?> = callbackFlow {
        val listener = firestore.collection("appointments")
            .whereEqualTo("patientId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("DashboardRepository", "Error listening to upcoming appointments flow", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val upcoming = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
                    }.filter { appt ->
                        val isCorrectStatus = appt.status == "UPCOMING" || appt.status == "IN_PROGRESS"
                        if (!isCorrectStatus) return@filter false
                        
                        val apptDate = parseAppointmentDateTime(appt.date, appt.time)
                        if (apptDate != null) {
                            val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
                            apptDate.time > twoHoursAgo
                        } else {
                            true
                        }
                    }.sortedWith { appt1, appt2 ->
                        val d1 = parseAppointmentDateTime(appt1.date, appt1.time)
                        val d2 = parseAppointmentDateTime(appt2.date, appt2.time)
                        when {
                            d1 == null && d2 == null -> 0
                            d1 == null -> 1
                            d2 == null -> -1
                            else -> d1.compareTo(d2)
                        }
                    }.firstOrNull()

                    trySend(upcoming)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Get the next upcoming appointment for the patient (Real-time updates via snapshot listeners)
     */
    fun getUpcomingAppointmentUpdates(): Flow<UpcomingAppointment?> {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) return kotlinx.coroutines.flow.flowOf(null)
        
        return getUpcomingAppointmentFlow(currentUid).map { appointment ->
            appointment?.let {
                UpcomingAppointment(
                    appointmentId = it.appointmentId,
                    doctorName = it.doctorName,
                    department = it.department,
                    status = it.status,
                    date = it.date,
                    time = it.time
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get the next upcoming appointment for the patient
     */
    fun getUpcomingAppointment(
        onSuccess: (UpcomingAppointment?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onSuccess(null)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val appt = getUpcomingAppointmentSync(currentUid)
            withContext(Dispatchers.Main) {
                onSuccess(appt)
            }
        }
    }

    private fun getLiveQueueSync(appointmentId: String?): LiveQueueInfo? {
        try {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (currentUid.isEmpty()) return null

            var activeQueueItem: QueueItem? = null

            if (!appointmentId.isNullOrEmpty()) {
                val snapshot = Tasks.await(
                    firestore.collection("queue")
                        .whereEqualTo("appointmentId", appointmentId)
                        .get()
                )
                val firstDoc = snapshot.documents.firstOrNull()
                activeQueueItem = firstDoc?.toObject(QueueItem::class.java)?.copy(queueId = firstDoc.id)
            } else {
                // Find next upcoming appointment
                val snapshotAppt = Tasks.await(
                    firestore.collection("appointments")
                        .whereEqualTo("patientId", currentUid)
                        .get()
                )
                val upcoming = snapshotAppt.documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
                }.filter { it.status == "UPCOMING" || it.status == "IN_PROGRESS" }
                 .sortedBy { it.date + " " + it.time }
                 .firstOrNull()

                if (upcoming != null) {
                    val snapshotQueue = Tasks.await(
                        firestore.collection("queue")
                            .whereEqualTo("appointmentId", upcoming.appointmentId)
                            .whereEqualTo("isActive", true)
                            .get()
                    )
                    val firstDoc = snapshotQueue.documents.firstOrNull()
                    activeQueueItem = firstDoc?.toObject(QueueItem::class.java)?.copy(queueId = firstDoc.id)
                }

                if (activeQueueItem == null) {
                    val snapshotQueueFallback = Tasks.await(
                        firestore.collection("queue")
                            .whereEqualTo("patientId", currentUid)
                            .whereEqualTo("isActive", true)
                            .get()
                    )
                    val firstDoc = snapshotQueueFallback.documents.firstOrNull()
                    activeQueueItem = firstDoc?.toObject(QueueItem::class.java)?.copy(queueId = firstDoc.id)
                }
            }

            if (activeQueueItem == null) return null

            val doctorId = activeQueueItem.doctorId
            val date = activeQueueItem.date

            // Fetch doctor profile
            val doctorDoc = Tasks.await(firestore.collection("doctor_profiles").document(doctorId).get())
            val defaultSlotDuration = if (doctorDoc.exists()) doctorDoc.getLong("slotDuration")?.toInt() ?: 15 else 15
            val consultationStartTimeStr = if (doctorDoc.exists()) doctorDoc.getString("consultationStartTime") ?: "09:00" else "09:00"

            // Fetch all appointments for doctor on date
            val apptSnapshot = Tasks.await(
                firestore.collection("appointments")
                    .whereEqualTo("doctorId", doctorId)
                    .whereEqualTo("date", date)
                    .get()
            )
            val appointments = apptSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
            }.filter { it.status != "CANCELLED" }
             .sortedWith(compareBy({ timeToMinutes(it.time) }, { it.createdAt.seconds }))

            // 1. Calculate average consultation duration
            val completedAppts = appointments.filter { it.status == "COMPLETED" && it.consultationStartedAt != null && it.consultationCompletedAt != null }
            var slotDuration = defaultSlotDuration
            if (completedAppts.isNotEmpty()) {
                var totalDuration = 0L
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                completedAppts.forEach { appt ->
                    try {
                        val start = sdf.parse(appt.consultationStartedAt!!)?.time ?: 0L
                        val end = sdf.parse(appt.consultationCompletedAt!!)?.time ?: 0L
                        totalDuration += (end - start) / 60000
                    } catch (e: Exception) {}
                }
                slotDuration = Math.max(5, Math.round(totalDuration.toDouble() / completedAppts.size).toInt())
            }

            // 2. Timeline simulation
            val calendar = Calendar.getInstance()
            val nowMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

            var timelineMin = timeToMinutes(consultationStartTimeStr)

            var targetEstimatedWait = 0
            var targetEstimatedDelay = 0
            var targetPatientsAhead = 0

            // Current serving token
            var currentServingToken = "0"
            val inProgressAppt = appointments.find { it.status == "IN_PROGRESS" }
            if (inProgressAppt != null) {
                currentServingToken = inProgressAppt.tokenNumber ?: "0"
            } else {
                val completedList = appointments.filter { it.status == "COMPLETED" }
                if (completedList.isNotEmpty()) {
                    currentServingToken = completedList.last().tokenNumber ?: "0"
                }
            }

            for (i in appointments.indices) {
                val appt = appointments[i]
                val scheduledStartMin = timeToMinutes(appt.time)

                var expectedStart = scheduledStartMin
                if (appt.status == "COMPLETED") {
                    val startMin = scheduledStartMin
                    val endMin = startMin + slotDuration
                    expectedStart = startMin
                    timelineMin = endMin
                } else if (appt.status == "IN_PROGRESS") {
                    val startMin = nowMin
                    val expectedEndMin = startMin + slotDuration
                    expectedStart = startMin
                    timelineMin = Math.max(expectedEndMin, nowMin)
                } else {
                    expectedStart = Math.max(timelineMin, scheduledStartMin)
                    timelineMin = expectedStart + slotDuration
                }

                val estimatedWait = Math.max(0, expectedStart - nowMin)
                val estimatedDelay = Math.max(0, expectedStart - scheduledStartMin)

                if (appt.appointmentId == activeQueueItem.appointmentId) {
                    targetEstimatedWait = estimatedWait
                    targetEstimatedDelay = estimatedDelay

                    for (j in 0 until i) {
                        val aheadAppt = appointments[j]
                        if (aheadAppt.status == "WAITING" || aheadAppt.status == "UPCOMING") {
                            targetPatientsAhead++
                        }
                    }
                    break
                }
            }

            var crowdLevel = CrowdLevel.LOW
            if (targetPatientsAhead > 10) crowdLevel = CrowdLevel.HIGH
            else if (targetPatientsAhead > 4) crowdLevel = CrowdLevel.MEDIUM

            var statusText = activeQueueItem.status
            if (targetEstimatedDelay >= 20 && (statusText == "WAITING" || statusText == "UPCOMING")) {
                statusText = "DOCTOR_RUNNING_LATE"
            }

            return LiveQueueInfo(
                isActive = activeQueueItem.isActive,
                queueNumber = activeQueueItem.tokenNumber ?: "0",
                currentServingToken = currentServingToken,
                status = statusText,
                patientsAhead = targetPatientsAhead,
                estimatedWaitMinutes = targetEstimatedWait,
                crowdLevel = crowdLevel,
                department = activeQueueItem.department
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Listen for real-time queue updates (via snapshot listeners)
     */
    fun getLiveQueueUpdates(appointmentId: String? = null): Flow<LiveQueueInfo?> = callbackFlow {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var doctorApptsListener: com.google.firebase.firestore.ListenerRegistration? = null
        var queueItemListener: com.google.firebase.firestore.ListenerRegistration? = null

        val processWithAppointment = { targetApptId: String, targetDoctorId: String, targetDate: String ->
            // Cancel previous sub-listeners before setting up new ones
            doctorApptsListener?.remove()
            doctorApptsListener = null
            queueItemListener?.remove()
            queueItemListener = null

            // Listen to the queue item to know if it's active and what its status is
            queueItemListener = firestore.collection("queue")
                .whereEqualTo("appointmentId", targetApptId)
                .addSnapshotListener { queueSnapshot, queueError ->
                    if (queueError != null) {
                        android.util.Log.e("DashboardRepository", "Error listening to queue status", queueError)
                        return@addSnapshotListener
                    }
                    val activeQueueItem = queueSnapshot?.documents?.firstOrNull()
                        ?.toObject(QueueItem::class.java)

                    if (activeQueueItem == null || !activeQueueItem.isActive) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    // Listen to all appointments for the doctor on that date
                    if (doctorApptsListener == null) {
                        doctorApptsListener = firestore.collection("appointments")
                            .whereEqualTo("doctorId", targetDoctorId)
                            .whereEqualTo("date", targetDate)
                            .addSnapshotListener { apptSnapshot, apptError ->
                                if (apptError != null) {
                                    android.util.Log.e("DashboardRepository", "Error listening to doctor appointments", apptError)
                                    return@addSnapshotListener
                                }

                                launch(Dispatchers.IO) {
                                    val appointments = apptSnapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Appointment::class.java)?.copy(appointmentId = doc.id)
                                    }?.filter { it.status != "CANCELLED" }
                                     ?.sortedWith(compareBy({ timeToMinutes(it.time) }, { it.createdAt.seconds }))
                                     ?: emptyList()

                                    // Fetch doctor profile to get default slot duration and startTime
                                    val doctorDoc = try {
                                        Tasks.await(firestore.collection("doctor_profiles").document(targetDoctorId).get())
                                    } catch (e: Exception) {
                                        null
                                    }

                                    val defaultSlotDuration = if (doctorDoc != null && doctorDoc.exists()) {
                                        doctorDoc.getLong("slotDuration")?.toInt() ?: 15
                                    } else 15

                                    val consultationStartTimeStr = if (doctorDoc != null && doctorDoc.exists()) {
                                        doctorDoc.getString("consultationStartTime") ?: "09:00"
                                    } else "09:00"

                                    // Calculate live queue info
                                    // 1. Calculate average consultation duration
                                    val completedAppts = appointments.filter { 
                                        it.status == "COMPLETED" && 
                                        it.consultationStartedAt != null && 
                                        it.consultationCompletedAt != null 
                                    }
                                    var slotDuration = defaultSlotDuration
                                    if (completedAppts.isNotEmpty()) {
                                        var totalDuration = 0L
                                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                        completedAppts.forEach { appt ->
                                            try {
                                                val start = sdf.parse(appt.consultationStartedAt!!)?.time ?: 0L
                                                val end = sdf.parse(appt.consultationCompletedAt!!)?.time ?: 0L
                                                totalDuration += (end - start) / 60000
                                            } catch (e: Exception) {}
                                        }
                                        slotDuration = Math.max(5, Math.round(totalDuration.toDouble() / completedAppts.size).toInt())
                                    }

                                    // 2. Timeline simulation
                                    val calendar = Calendar.getInstance()
                                    val nowMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

                                    var timelineMin = timeToMinutes(consultationStartTimeStr)

                                    var targetEstimatedWait = 0
                                    var targetEstimatedDelay = 0
                                    var targetPatientsAhead = 0

                                    // Current serving token
                                    var currentServingToken = "0"
                                    val inProgressAppt = appointments.find { it.status == "IN_PROGRESS" }
                                    if (inProgressAppt != null) {
                                        currentServingToken = inProgressAppt.tokenNumber ?: "0"
                                    } else {
                                        val completedList = appointments.filter { it.status == "COMPLETED" }
                                        if (completedList.isNotEmpty()) {
                                            currentServingToken = completedList.last().tokenNumber ?: "0"
                                        }
                                    }

                                    for (i in appointments.indices) {
                                        val appt = appointments[i]
                                        val scheduledStartMin = timeToMinutes(appt.time)

                                        var expectedStart = scheduledStartMin
                                        if (appt.status == "COMPLETED") {
                                            val startMin = scheduledStartMin
                                            val endMin = startMin + slotDuration
                                            expectedStart = startMin
                                            timelineMin = endMin
                                        } else if (appt.status == "IN_PROGRESS") {
                                            val startMin = nowMin
                                            val expectedEndMin = startMin + slotDuration
                                            expectedStart = startMin
                                            timelineMin = Math.max(expectedEndMin, nowMin)
                                        } else {
                                            expectedStart = Math.max(timelineMin, scheduledStartMin)
                                            timelineMin = expectedStart + slotDuration
                                        }

                                        val estimatedWait = Math.max(0, expectedStart - nowMin)
                                        val estimatedDelay = Math.max(0, expectedStart - scheduledStartMin)

                                        if (appt.appointmentId == activeQueueItem.appointmentId) {
                                            targetEstimatedWait = estimatedWait
                                            targetEstimatedDelay = estimatedDelay

                                            for (j in 0 until i) {
                                                val aheadAppt = appointments[j]
                                                if (aheadAppt.status == "WAITING" || aheadAppt.status == "UPCOMING") {
                                                    targetPatientsAhead++
                                                }
                                            }
                                            break
                                        }
                                    }

                                    var crowdLevel = CrowdLevel.LOW
                                    if (targetPatientsAhead > 10) crowdLevel = CrowdLevel.HIGH
                                    else if (targetPatientsAhead > 4) crowdLevel = CrowdLevel.MEDIUM

                                    var statusText = activeQueueItem.status
                                    if (targetEstimatedDelay >= 20 && (statusText == "WAITING" || statusText == "UPCOMING")) {
                                        statusText = "DOCTOR_RUNNING_LATE"
                                    }

                                    val liveQueueInfo = LiveQueueInfo(
                                        isActive = activeQueueItem.isActive,
                                        queueNumber = activeQueueItem.tokenNumber ?: "0",
                                        currentServingToken = currentServingToken,
                                        status = statusText,
                                        patientsAhead = targetPatientsAhead,
                                        estimatedWaitMinutes = targetEstimatedWait,
                                        crowdLevel = crowdLevel,
                                        department = activeQueueItem.department
                                    )
                                    trySend(liveQueueInfo)
                                }
                            }
                    }
                }
        }

        var upcomingJob: kotlinx.coroutines.Job? = null

        if (!appointmentId.isNullOrEmpty()) {
            // We have a direct appointment ID. We need to fetch the appointment first to know doctor and date.
            launch(Dispatchers.IO) {
                try {
                    val apptSnapshot = Tasks.await(firestore.collection("appointments").document(appointmentId).get())
                    val appt = apptSnapshot.toObject(Appointment::class.java)
                    if (appt != null) {
                        processWithAppointment(appointmentId, appt.doctorId, appt.date)
                    } else {
                        trySend(null)
                    }
                } catch (e: Exception) {
                    trySend(null)
                }
            }
        } else {
            // We don't have a direct appointment ID, listen to upcoming appointments.
            upcomingJob = launch {
                getUpcomingAppointmentFlow(currentUid).collect { upcomingAppt ->
                    if (upcomingAppt == null) {
                        trySend(null)
                    } else {
                        processWithAppointment(upcomingAppt.appointmentId, upcomingAppt.doctorId, upcomingAppt.date)
                    }
                }
            }
        }

        awaitClose {
            upcomingJob?.cancel()
            doctorApptsListener?.remove()
            queueItemListener?.remove()
        }
    }

    /**
     * Get recent activities
     */
    fun getRecentActivities(
        onSuccess: (List<ActivityItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        firestore.collection("activities")
            .whereEqualTo("userId", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val typeStr = doc.getString("type") ?: "GENERAL"
                    val type = try { ActivityType.valueOf(typeStr) } catch(e: Exception) { ActivityType.GENERAL }
                    ActivityItem(
                        id = doc.id,
                        type = type,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                // Fallback if index not built
                firestore.collection("activities")
                    .whereEqualTo("userId", currentUid)
                    .get()
                    .addOnSuccessListener { innerSnapshot ->
                        val list = innerSnapshot.documents.mapNotNull { doc ->
                            val typeStr = doc.getString("type") ?: "GENERAL"
                            val type = try { ActivityType.valueOf(typeStr) } catch(e: Exception) { ActivityType.GENERAL }
                            ActivityItem(
                                id = doc.id,
                                type = type,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                timestamp = doc.getString("timestamp") ?: ""
                            )
                        }.sortedByDescending { it.timestamp }
                        onSuccess(list.take(10))
                    }
                    .addOnFailureListener {
                        onFailure(e.message ?: "Failed to fetch activities")
                    }
            }
    }

    /**
     * Create a notification for a user
     */
    fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: String = "GENERAL",
        onComplete: (() -> Unit)? = null
    ) {
        val notificationData = hashMapOf(
            "userId" to userId,
            "title" to title,
            "message" to message,
            "type" to type,
            "read" to false,
            "isRead" to false,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("notifications").add(notificationData)
            .addOnSuccessListener { onComplete?.invoke() }
            .addOnFailureListener { onComplete?.invoke() }
    }

    /**
     * Create an activity log for a user
     */
    fun createActivityLog(
        userId: String,
        type: String,
        title: String,
        description: String
    ) {
        val timestampStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US).format(Date())

        val activityData = hashMapOf(
            "userId" to userId,
            "type" to type,
            "title" to title,
            "description" to description,
            "timestamp" to timestampStr
        )

        firestore.collection("activities").add(activityData)
            .addOnFailureListener { e ->
                android.util.Log.e("DashboardRepository", "Failed to log activity log", e)
            }
    }
}
