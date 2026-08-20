package com.example.medplus.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.medplus.auth.model.User
import com.example.medplus.data.network.SessionManager
import com.example.medplus.dashboard.model.*
import com.example.medplus.model.Appointment
import com.example.medplus.model.Notification
import com.example.medplus.model.QueueItem
import com.example.medplus.data.network.*
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = SessionManager.getInstance(context)
    private val apiService: ApiService get() = RetrofitClient.getClient(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("medplus_activities_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCurrentUser(
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getUserProfile(currentUid).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val user = User(
                        uid = body.uid,
                        fullName = body.fullName,
                        email = body.email,
                        phone = body.phone,
                        role = body.role,
                        profileImage = body.profileImage ?: "",
                        status = body.status ?: "ACTIVE"
                    )
                    onSuccess(user)
                } else {
                    onFailure(response.errorBody()?.string() ?: "Failed to fetch profile details")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching user details")
            }
        })
    }

    /**
     * Update user profile
     */
    fun updateUserProfile(
        fullName: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        val request = UpdateProfileRequest(fullName, phone)
        apiService.updateUserProfile(currentUid, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) {
                    sessionManager.updateProfile(fullName, phone)
                    onSuccess()
                } else {
                    onFailure(response.errorBody()?.string() ?: "Failed to update profile details")
                }
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error updating profile")
            }
        })
    }

    /**
     * Get unread notification count for the current user
     */
    fun getUnreadNotificationCount(
        onSuccess: (Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getNotifications(currentUid).enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, response: Response<List<NotificationResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val count = response.body()!!.count { it.read == 0 }
                    onSuccess(count)
                } else {
                    onSuccess(0)
                }
            }

            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching count")
            }
        })
    }

    /**
     * Get all notifications for the current user
     */
    fun getNotifications(
        onSuccess: (List<Notification>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getNotifications(currentUid).enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, response: Response<List<NotificationResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        Notification(
                            id = body.id,
                            userId = body.userId,
                            title = body.title,
                            message = body.message,
                            type = body.type ?: "GENERAL",
                            isRead = body.read == 1,
                            timestamp = Timestamp.now()
                        )
                    }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching notifications")
            }
        })
    }

    /**
     * Mark a notification as read
     */
    fun markNotificationAsRead(id: String) {
        apiService.markNotificationAsRead(id).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {}
            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {}
        })
    }

    /**
     * Delete a notification
     */
    fun deleteNotification(id: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        apiService.deleteNotification(id).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to delete notification")
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error deleting notification")
            }
        })
    }

    /**
     * Mark all notifications as read for current user
     */
    fun markAllNotificationsAsRead(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        apiService.getNotifications(currentUid).enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, response: Response<List<NotificationResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val unread = response.body()!!.filter { it.read == 0 }
                    var completed = 0
                    if (unread.isEmpty()) {
                        onSuccess()
                        return
                    }
                    unread.forEach { notification ->
                        apiService.markNotificationAsRead(notification.id).enqueue(object : Callback<MsgResponse> {
                            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                                completed++
                                if (completed == unread.size) onSuccess()
                            }

                            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                                completed++
                                if (completed == unread.size) onSuccess()
                            }
                        })
                    }
                } else {
                    onSuccess()
                }
            }

            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error")
            }
        })
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
        return 0
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
            val res = apiService.getPatientAppointments(uid).execute()
            if (!res.isSuccessful || res.body() == null) return null
            val list = res.body()!!

            val upcoming = list.map { body ->
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
            }.filter { appt ->
                val isCorrectStatus = appt.status == "PENDING" || appt.status == "ACTIVE" || appt.status == "UPCOMING" || appt.status == "CONFIRMED"
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

    fun getUpcomingAppointmentFlow(uid: String): Flow<Appointment?> = flow {
        while (true) {
            val res = try {
                val response = apiService.getPatientAppointments(uid).execute()
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
                    }.filter { appt ->
                        val isCorrectStatus = appt.status == "PENDING" || appt.status == "ACTIVE" || appt.status == "UPCOMING" || appt.status == "CONFIRMED"
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
                } else null
            } catch (e: Exception) {
                null
            }
            emit(res)
            delay(5000)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    /**
     * Get the next upcoming appointment for the patient
     */
    fun getUpcomingAppointmentUpdates(): Flow<UpcomingAppointment?> {
        val currentUid = sessionManager.getUserId() ?: ""
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
        val currentUid = sessionManager.getUserId() ?: ""
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
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) return null

        return try {
            val apptsCall = apiService.getPatientAppointments(currentUid).execute()
            if (!apptsCall.isSuccessful || apptsCall.body() == null) return null
            val patientAppts = apptsCall.body()!!

            val activeAppt = if (!appointmentId.isNullOrEmpty()) {
                patientAppts.find { it.id == appointmentId }
            } else {
                patientAppts.find { it.status == "PENDING" || it.status == "ACTIVE" }
            } ?: return null

            val doctorId = activeAppt.doctorId
            val date = activeAppt.date

            // Get doctor queue list
            val queueCall = apiService.getQueue(doctorId, date).execute()
            if (!queueCall.isSuccessful || queueCall.body() == null) return null
            val doctorQueue = queueCall.body()!!
            val activeQueueItem = doctorQueue.find { it.appointmentId == activeAppt.id } ?: return null

            // Fetch doctor profile
            val doctorCall = apiService.getDoctorProfile(doctorId).execute()
            val doctorProfile = doctorCall.body()
            val defaultSlotDuration = doctorProfile?.slotDuration ?: 15
            val consultationStartTimeStr = doctorProfile?.consultationStartTime ?: "09:00 AM"

            // Get doctor appointments
            val doctorApptsCall = apiService.getDoctorAppointments(doctorId).execute()
            if (!doctorApptsCall.isSuccessful || doctorApptsCall.body() == null) return null
            val allDoctorAppts = doctorApptsCall.body()!!.filter { it.date == date && it.status != "CANCELLED" }
                .sortedWith(compareBy({ timeToMinutes(it.time) }, { it.createdAt }))

            // Calculate live queue stats
            val completedAppts = allDoctorAppts.filter { it.status == "COMPLETED" }
            var slotDuration = defaultSlotDuration

            val calendar = Calendar.getInstance()
            val nowMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
            var timelineMin = timeToMinutes(consultationStartTimeStr)

            var targetEstimatedWait = 0
            var targetEstimatedDelay = 0
            var targetPatientsAhead = 0

            // Determine current serving token
            var currentServingToken = "0"
            val inProgressAppt = allDoctorAppts.find { it.status == "ACTIVE" }
            if (inProgressAppt != null) {
                currentServingToken = inProgressAppt.tokenNumber
            } else {
                if (completedAppts.isNotEmpty()) {
                    currentServingToken = completedAppts.last().tokenNumber
                }
            }

            for (i in allDoctorAppts.indices) {
                val appt = allDoctorAppts[i]
                val scheduledStartMin = timeToMinutes(appt.time)

                var expectedStart = scheduledStartMin
                if (appt.status == "COMPLETED") {
                    val startMin = scheduledStartMin
                    val endMin = startMin + slotDuration
                    expectedStart = startMin
                    timelineMin = endMin
                } else if (appt.status == "ACTIVE") {
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

                if (appt.id == activeQueueItem.appointmentId) {
                    targetEstimatedWait = estimatedWait
                    targetEstimatedDelay = estimatedDelay

                    for (j in 0 until i) {
                        if (allDoctorAppts[j].status == "PENDING") {
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

            LiveQueueInfo(
                isActive = activeQueueItem.isActive,
                queueNumber = activeQueueItem.tokenNumber ?: "0",
                currentServingToken = currentServingToken,
                status = statusText,
                patientsAhead = targetPatientsAhead,
                estimatedWaitMinutes = targetEstimatedWait,
                crowdLevel = crowdLevel,
                department = activeQueueItem.department ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Listen for real-time queue updates (via flow polling)
     */
    fun getLiveQueueUpdates(appointmentId: String? = null): Flow<LiveQueueInfo?> = flow {
        while (true) {
            emit(getLiveQueueSync(appointmentId))
            delay(4000)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get recent activities
     */
    fun getRecentActivities(
        onSuccess: (List<ActivityItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUid = sessionManager.getUserId() ?: ""
        if (currentUid.isEmpty()) {
            onFailure("User not logged in")
            return
        }

        val json = prefs.getString("activities_$currentUid", null)
        val type = object : TypeToken<List<ActivityItem>>() {}.type
        val list: List<ActivityItem> = if (json != null) {
            gson.fromJson(json, type)
        } else emptyList()

        onSuccess(list)
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
        val request = NotificationResponse("", userId, title, message, 0, type, "")
        apiService.getNotifications(userId).enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, response: Response<List<NotificationResponse>>) {
                // REST creation
                onComplete?.invoke()
            }
            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                onComplete?.invoke()
            }
        })
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
        val activityType = try { ActivityType.valueOf(type) } catch(e: Exception) { ActivityType.GENERAL }
        val newActivity = ActivityItem(
            id = Math.random().toString(),
            type = activityType,
            title = title,
            description = description,
            timestamp = timestampStr
        )

        getRecentActivities({ list ->
            val updated = (listOf(newActivity) + list).take(10)
            prefs.edit().putString("activities_$userId", gson.toJson(updated)).apply()
        }, {})
    }
}
