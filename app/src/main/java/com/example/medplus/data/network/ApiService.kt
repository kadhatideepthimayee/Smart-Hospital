package com.example.medplus.data.network

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/google")
    fun loginWithGoogle(@Body request: GoogleLoginRequest): Call<LoginResponse>

    @GET("api/auth/profile/{uid}")
    fun getUserProfile(@Path("uid") uid: String): Call<UserResponse>

    @PUT("api/auth/profile/{uid}")
    fun updateUserProfile(@Path("uid") uid: String, @Body request: UpdateProfileRequest): Call<MsgResponse>

    // Doctors
    @GET("api/doctors")
    fun getDoctors(): Call<List<DoctorProfileResponse>>

    @GET("api/doctors/{id}")
    fun getDoctorProfile(@Path("id") id: String): Call<DoctorProfileResponse>

    @POST("api/doctors/{id}/profile")
    fun updateDoctorProfile(@Path("id") id: String, @Body request: DoctorProfileRequest): Call<MsgResponse>

    @GET("api/doctors/patients/{doctorId}")
    fun getDoctorPatients(@Path("doctorId") doctorId: String): Call<List<UserResponse>>

    // Appointments
    @POST("api/appointments/book")
    fun bookAppointment(@Body request: BookAppointmentRequest): Call<AppointmentResponse>

    @GET("api/appointments/patient/{patientId}")
    fun getPatientAppointments(@Path("patientId") patientId: String): Call<List<AppointmentResponse>>

    @GET("api/appointments/doctor/{doctorId}")
    fun getDoctorAppointments(@Path("doctorId") doctorId: String): Call<List<AppointmentResponse>>

    @GET("api/appointments/{id}")
    fun getAppointmentDetails(@Path("id") id: String): Call<AppointmentResponse>

    @PUT("api/appointments/{id}/status")
    fun updateAppointmentStatus(@Path("id") id: String, @Body request: StatusRequest): Call<MsgResponse>

    @POST("api/appointments/reschedule")
    fun rescheduleAppointment(@Body request: RescheduleRequest): Call<AppointmentResponse>

    // Medical Records
    @POST("api/medical-records")
    fun createMedicalRecord(@Body request: MedicalRecordRequest): Call<MedicalRecordResponse>

    @GET("api/medical-records/patient/{patientId}")
    fun getPatientMedicalRecords(@Path("patientId") patientId: String): Call<List<MedicalRecordResponse>>

    @GET("api/medical-records/{id}")
    fun getMedicalRecordById(@Path("id") id: String): Call<MedicalRecordResponse>

    // Feedback
    @POST("api/feedback")
    fun submitFeedback(@Body request: FeedbackRequest): Call<FeedbackResponse>

    @GET("api/feedback/doctor/{doctorId}")
    fun getDoctorFeedback(@Path("doctorId") doctorId: String): Call<List<FeedbackResponse>>

    @GET("api/feedback/appointment/{appointmentId}")
    fun getFeedbackForAppointment(@Path("appointmentId") appointmentId: String): Call<FeedbackCheckResponse>

    // Dashboard Stats
    @GET("api/dashboard/patient/{patientId}")
    fun getPatientDashboardStats(@Path("patientId") patientId: String): Call<PatientDashboardStats>

    @GET("api/dashboard/doctor/{doctorId}")
    fun getDoctorDashboardStats(@Path("doctorId") doctorId: String): Call<DoctorDashboardStats>

    @GET("api/dashboard/admin")
    fun getAdminDashboardStats(): Call<AdminDashboardStats>

    // Admin
    @GET("api/admin/pending-doctors")
    fun getPendingDoctors(): Call<List<DoctorProfileResponse>>

    @PUT("api/admin/verify-doctor/{doctorId}")
    fun verifyDoctor(@Path("doctorId") doctorId: String, @Body request: VerifyDoctorRequest): Call<MsgResponse>

    @GET("api/admin/doctors")
    fun getAdminDoctors(): Call<List<DoctorProfileResponse>>

    @GET("api/admin/patients")
    fun getAdminPatients(): Call<List<UserResponse>>

    @GET("api/admin/appointments")
    fun getAdminAppointments(): Call<List<AppointmentResponse>>

    // Queue
    @GET("api/queue")
    fun getQueue(@Query("doctorId") doctorId: String, @Query("date") date: String?): Call<List<QueueResponse>>

    @PUT("api/queue/{id}/status")
    fun updateQueueStatus(@Path("id") id: String, @Body request: StatusRequest): Call<QueueResponse>

    // Notifications
    @GET("api/notifications/{userId}")
    fun getNotifications(@Path("userId") userId: String): Call<List<NotificationResponse>>

    @PUT("api/notifications/{id}/read")
    fun markNotificationAsRead(@Path("id") id: String): Call<MsgResponse>

    @DELETE("api/notifications/{id}")
    fun deleteNotification(@Path("id") id: String): Call<MsgResponse>
}

// Request and Response classes

data class GoogleLoginRequest(
    val idToken: String,
    val email: String,
    val fullName: String,
    val role: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)

data class RegisterResponse(
    val message: String,
    val uid: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val uid: String,
    val email: String,
    val role: String,
    val fullName: String,
    val phone: String,
    val profileImage: String
)

data class UserResponse(
    val uid: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val profileImage: String?,
    val status: String?,
    val createdAt: String?
)

data class UpdateProfileRequest(
    val fullName: String,
    val phone: String,
    val profileImage: String? = null
)

data class MsgResponse(
    val message: String
)

data class DoctorProfileResponse(
    val uid: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val qualification: String?,
    val department: String?,
    val specialization: String?,
    val experienceYears: Int?,
    val registrationAuthority: String?,
    val registrationNumber: String?,
    val consultationFee: Double?,
    val bio: String?,
    val profileImage: String?,
    val registrationCertificateUrl: String?,
    val verificationDocumentUrl: String?,
    val workingDays: List<String>?,
    val consultationStartTime: String?,
    val consultationEndTime: String?,
    val lunchStartTime: String?,
    val lunchEndTime: String?,
    val breakStartTime: String?,
    val breakEndTime: String?,
    val slotDuration: Int?,
    val verificationStatus: String?,
    val rejectionReason: String?,
    val submittedAt: String?,
    val reviewedAt: String?,
    val reviewedBy: String?
)

data class DoctorProfileRequest(
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val qualification: String?,
    val department: String?,
    val specialization: String?,
    val experienceYears: Int?,
    val registrationAuthority: String?,
    val registrationNumber: String?,
    val consultationFee: Double?,
    val bio: String?,
    val profileImage: String?,
    val registrationCertificateUrl: String?,
    val verificationDocumentUrl: String?,
    val workingDays: List<String>?,
    val consultationStartTime: String?,
    val consultationEndTime: String?,
    val lunchStartTime: String?,
    val lunchEndTime: String?,
    val breakStartTime: String?,
    val breakEndTime: String?,
    val slotDuration: Int?,
    val verificationStatus: String?,
    val rejectionReason: String? = null,
    val reviewedBy: String? = null
)

data class BookAppointmentRequest(
    val patientId: String,
    val doctorId: String,
    val doctorName: String,
    val department: String,
    val date: String,
    val time: String,
    val reason: String
)

data class AppointmentResponse(
    val id: String,
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val department: String,
    val date: String,
    val time: String,
    val reason: String?,
    val tokenNumber: String,
    val status: String,
    val appointmentTimestamp: Long,
    val createdAt: String
)

data class StatusRequest(
    val status: String
)

data class RescheduleRequest(
    val appointmentId: String,
    val doctorId: String,
    val doctorName: String,
    val department: String,
    val date: String,
    val time: String,
    val reason: String?
)

data class MedicalRecordRequest(
    val appointmentId: String,
    val patientId: String,
    val diagnosis: String,
    val prescription: String,
    val notes: String,
    val followUpDate: String,
    val fileUrl: String = ""
)

data class MedicalRecordResponse(
    val id: String,
    val patientId: String,
    val patientName: String?,
    val doctorId: String?,
    val doctorName: String?,
    val appointmentId: String?,
    val diagnosis: String?,
    val prescription: String?,
    val notes: String?,
    val followUpDate: String?,
    val fileUrl: String?,
    val createdAt: String?
)

data class FeedbackRequest(
    val doctorId: String,
    val patientId: String,
    val rating: Int,
    val feedback: String,
    val appointmentId: String
)

data class FeedbackResponse(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val patientName: String,
    val appointmentId: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

data class FeedbackCheckResponse(
    val exists: Boolean,
    val feedback: FeedbackResponse?
)

data class PatientDashboardStats(
    val totalAppointments: Int,
    val completedAppointments: Int,
    val totalMedicalRecords: Int,
    val upcomingAppointment: AppointmentResponse?
)

data class DoctorDashboardStats(
    val totalAppointments: Int,
    val pendingAppointments: Int,
    val completedAppointments: Int,
    val averageRating: Double
)

data class AdminDashboardStats(
    val totalPatients: Int,
    val totalDoctors: Int,
    val totalAppointments: Int,
    val pendingVerifications: Int
)

data class VerifyDoctorRequest(
    val status: String,
    val rejectionReason: String,
    val reviewedBy: String
)

data class QueueResponse(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val patientName: String,
    val tokenNumber: String,
    val status: String,
    val department: String,
    val date: String,
    val isActive: Boolean,
    val estimatedWaitMinutes: Int,
    val createdAt: String
)

data class NotificationResponse(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val read: Int,
    val type: String?,
    val createdAt: String,
    val doctorId: String? = null
)
