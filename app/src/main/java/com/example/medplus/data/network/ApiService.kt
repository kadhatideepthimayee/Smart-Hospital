package com.example.medplus.data.network

import com.example.medplus.auth.model.User
import com.example.medplus.model.*
import com.example.medplus.dashboard.model.UpcomingAppointment
import com.example.medplus.dashboard.model.LiveQueueInfo
import com.example.medplus.dashboard.model.ActivityItem
import retrofit2.Response
import retrofit2.http.*

// Request & Response DTOs
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String,
    val role: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class MessageResponse(
    val msg: String
)

data class UpdateProfileRequest(
    val fullName: String,
    val phone: String
)

data class AvailabilityRequest(
    val workingDays: List<String>,
    val consultationStartTime: String,
    val consultationEndTime: String,
    val lunchStartTime: String,
    val lunchEndTime: String,
    val breakStartTime: String,
    val breakEndTime: String,
    val slotDuration: Int,
    val consultationFee: Double
)

data class UpdateQueueStatusRequest(
    val newStatus: String
)

data class BookAppointmentRequest(
    val doctorId: String,
    val doctorName: String,
    val department: String,
    val date: String,
    val time: String,
    val reason: String
)

data class VerifyDoctorRequest(
    val doctorId: String,
    val newStatus: String,
    val rejectionReason: String? = null
)

data class UnreadCountResponse(
    val count: Int
)

data class ActivityLogRequest(
    val type: String,
    val title: String,
    val description: String
)

data class ActivityLogResponse(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val description: String,
    val timestamp: String
)

data class UpdateAppointmentStatusRequest(
    val status: String
)

data class CreateMedicalRecordRequest(
    val appointmentId: String,
    val patientId: String,
    val diagnosis: String,
    val prescription: String,
    val notes: String,
    val followUpDate: String
)

data class SubmitFeedbackRequest(
    val doctorId: String,
    val rating: Int,
    val feedback: String,
    val appointmentId: String
)

data class FeedbackExistsResponse(
    val exists: Boolean,
    val feedback: DoctorFeedback?
)

interface ApiService {
    // AUTH Endpoints
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<User>

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<User>

    // DOCTOR Endpoints
    @GET("api/doctors/profile")
    suspend fun getDoctorProfile(): Response<DoctorProfile>

    @GET("api/doctors/profile/{uid}")
    suspend fun getDoctorProfileByUid(@Path("uid") uid: String): Response<DoctorProfile>

    @POST("api/doctors/profile/setup")
    suspend fun setupDoctorProfile(@Body profile: DoctorProfile): Response<DoctorProfile>

    @GET("api/doctors/verified")
    suspend fun getVerifiedDoctors(): Response<List<DoctorProfile>>

    @POST("api/doctors/availability")
    suspend fun updateAvailability(@Body request: AvailabilityRequest): Response<DoctorProfile>

    @GET("api/doctors/queue")
    suspend fun getDoctorQueue(@Query("date") date: String? = null): Response<List<QueueItem>>

    @PUT("api/doctors/queue/{queueId}")
    suspend fun updateQueueStatus(@Path("queueId") queueId: String, @Body request: UpdateQueueStatusRequest): Response<QueueItem>

    @GET("api/doctors/patients")
    suspend fun getDoctorPatients(): Response<List<User>>

    // APPOINTMENT Endpoints
    @POST("api/appointments")
    suspend fun bookAppointment(@Body request: BookAppointmentRequest): Response<Appointment>

    @GET("api/appointments/patient")
    suspend fun getPatientAppointments(): Response<List<Appointment>>

    @GET("api/appointments/doctor")
    suspend fun getDoctorAppointments(): Response<List<Appointment>>

    @GET("api/appointments/doctor/{doctorId}")
    suspend fun getAppointmentsByDoctorId(@Path("doctorId") doctorId: String): Response<List<Appointment>>

    @GET("api/appointments/{id}")
    suspend fun getAppointmentDetails(@Path("id") id: String): Response<Appointment>

    @POST("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: String): Response<Appointment>

    // ADMIN Endpoints
    @GET("api/admin/doctors")
    suspend fun getDoctorsByStatus(@Query("status") status: String): Response<List<DoctorProfile>>

    @GET("api/admin/doctors/all")
    suspend fun getAllDoctorProfiles(): Response<List<DoctorProfile>>

    @POST("api/admin/verify-doctor")
    suspend fun verifyDoctor(@Body request: VerifyDoctorRequest): Response<MessageResponse>

    @GET("api/admin/notifications")
    suspend fun getAdminNotifications(): Response<List<AdminNotification>>

    @DELETE("api/admin/notifications/{id}")
    suspend fun deleteAdminNotification(@Path("id") id: String): Response<MessageResponse>

    @PUT("api/admin/notifications/{id}/read")
    suspend fun markAdminNotificationRead(@Path("id") id: String): Response<AdminNotification>

    @GET("api/admin/notifications/unread-count")
    suspend fun getAdminUnreadCount(): Response<UnreadCountResponse>

    // DASHBOARD Endpoints
    @GET("api/dashboard/notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @GET("api/dashboard/notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>

    @PUT("api/dashboard/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<Notification>

    @PUT("api/dashboard/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<MessageResponse>

    @DELETE("api/dashboard/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<MessageResponse>

    @GET("api/dashboard/upcoming-appointment")
    suspend fun getUpcomingAppointment(): Response<UpcomingAppointment>

    @GET("api/dashboard/live-queue")
    suspend fun getLiveQueue(@Query("appointmentId") appointmentId: String? = null): Response<LiveQueueInfo>

    @GET("api/dashboard/activities")
    suspend fun getRecentActivities(): Response<List<ActivityItem>>

    @POST("api/dashboard/activity")
    suspend fun logActivity(@Body activity: ActivityLogRequest): Response<ActivityLogResponse>

    @PUT("api/appointments/{id}/status")
    suspend fun updateAppointmentStatus(@Path("id") id: String, @Body request: UpdateAppointmentStatusRequest): Response<Appointment>

    @POST("api/medical-records")
    suspend fun createMedicalRecord(@Body request: CreateMedicalRecordRequest): Response<MedicalRecord>

    @GET("api/medical-records/patient")
    suspend fun getPatientMedicalRecords(): Response<List<MedicalRecord>>

    @GET("api/medical-records/{id}")
    suspend fun getMedicalRecordDetails(@Path("id") id: String): Response<MedicalRecord>

    @POST("api/feedback")
    suspend fun submitFeedback(@Body request: SubmitFeedbackRequest): Response<DoctorFeedback>

    @GET("api/feedback/doctor/{doctorId}")
    suspend fun getDoctorFeedback(@Path("doctorId") doctorId: String): Response<List<DoctorFeedback>>

    @GET("api/feedback/appointment/{appointmentId}")
    suspend fun checkFeedbackExists(@Path("appointmentId") appointmentId: String): Response<FeedbackExistsResponse>
}
