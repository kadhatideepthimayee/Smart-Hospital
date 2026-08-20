package com.example.medplus.repository

import android.content.Context
import com.example.medplus.model.AdminNotification
import com.example.medplus.model.DoctorProfile
import com.example.medplus.data.network.*
import com.google.firebase.Timestamp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService: ApiService get() = RetrofitClient.getClient(context)

    /**
     * Fetches admin notifications.
     */
    fun getAdminNotifications(
        onSuccess: (List<AdminNotification>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getNotifications("ADMIN").enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, response: Response<List<NotificationResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        AdminNotification(
                            id = body.id,
                            doctorId = body.doctorId ?: "",
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
                onFailure(t.message ?: "Network error fetching admin notifications")
            }
        })
    }

    /**
     * Deletes a notification.
     */
    fun deleteNotification(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
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
     * Marks a notification as read.
     */
    fun markNotificationAsRead(id: String) {
        apiService.markNotificationAsRead(id).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {}
            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {}
        })
    }

    /**
     * Gets the unread notification count.
     */
    fun getUnreadNotificationCount(
        onSuccess: (Int) -> Unit
    ) {
        apiService.getNotifications("ADMIN").enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, response: Response<List<NotificationResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val count = response.body()!!.count { it.read == 0 }
                    onSuccess(count)
                } else {
                    onSuccess(0)
                }
            }

            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                onSuccess(0)
            }
        })
    }

    /**
     * Fetches doctor profiles based on their verification status.
     */
    fun getDoctorsByStatus(
        status: String,
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getPendingDoctors().enqueue(object : Callback<List<DoctorProfileResponse>> {
            override fun onResponse(call: Call<List<DoctorProfileResponse>>, response: Response<List<DoctorProfileResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        DoctorProfile(
                            uid = body.uid,
                            fullName = body.fullName,
                            email = body.email,
                            phone = body.phone ?: "",
                            qualification = body.qualification ?: "",
                            department = body.department ?: "",
                            specialization = body.specialization ?: "",
                            experienceYears = body.experienceYears ?: 0,
                            registrationAuthority = body.registrationAuthority ?: "",
                            registrationNumber = body.registrationNumber ?: "",
                            consultationFee = body.consultationFee ?: 0.0,
                            bio = body.bio ?: "",
                            profileImage = body.profileImage ?: "",
                            registrationCertificateUrl = body.registrationCertificateUrl ?: "",
                            verificationDocumentUrl = body.verificationDocumentUrl ?: "",
                            workingDays = body.workingDays ?: emptyList(),
                            consultationStartTime = body.consultationStartTime ?: "",
                            consultationEndTime = body.consultationEndTime ?: "",
                            lunchStartTime = body.lunchStartTime ?: "",
                            lunchEndTime = body.lunchEndTime ?: "",
                            breakStartTime = body.breakStartTime ?: "",
                            breakEndTime = body.breakEndTime ?: "",
                            slotDuration = body.slotDuration ?: 15,
                            verificationStatus = body.verificationStatus ?: "PENDING"
                        )
                    }.filter { it.verificationStatus == status }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<DoctorProfileResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching doctors")
            }
        })
    }

    /**
     * Fetches all doctor profiles.
     */
    fun getAllDoctorProfiles(
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        apiService.getAdminDoctors().enqueue(object : Callback<List<DoctorProfileResponse>> {
            override fun onResponse(call: Call<List<DoctorProfileResponse>>, response: Response<List<DoctorProfileResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { body ->
                        DoctorProfile(
                            uid = body.uid,
                            fullName = body.fullName,
                            email = body.email,
                            phone = body.phone ?: "",
                            qualification = body.qualification ?: "",
                            department = body.department ?: "",
                            specialization = body.specialization ?: "",
                            experienceYears = body.experienceYears ?: 0,
                            registrationAuthority = body.registrationAuthority ?: "",
                            registrationNumber = body.registrationNumber ?: "",
                            consultationFee = body.consultationFee ?: 0.0,
                            bio = body.bio ?: "",
                            profileImage = body.profileImage ?: "",
                            registrationCertificateUrl = body.registrationCertificateUrl ?: "",
                            verificationDocumentUrl = body.verificationDocumentUrl ?: "",
                            workingDays = body.workingDays ?: emptyList(),
                            consultationStartTime = body.consultationStartTime ?: "",
                            consultationEndTime = body.consultationEndTime ?: "",
                            lunchStartTime = body.lunchStartTime ?: "",
                            lunchEndTime = body.lunchEndTime ?: "",
                            breakStartTime = body.breakStartTime ?: "",
                            breakEndTime = body.breakEndTime ?: "",
                            slotDuration = body.slotDuration ?: 15,
                            verificationStatus = body.verificationStatus ?: "PENDING"
                        )
                    }
                    onSuccess(list)
                } else {
                    onSuccess(emptyList())
                }
            }

            override fun onFailure(call: Call<List<DoctorProfileResponse>>, t: Throwable) {
                onFailure(t.message ?: "Network error fetching all doctor profiles")
            }
        })
    }

    /**
     * Updates the verification status of a doctor with review metadata.
     */
    fun updateDoctorVerificationStatus(
        uid: String,
        adminUid: String,
        newStatus: String,
        rejectionReason: String? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = VerifyDoctorRequest(
            status = newStatus,
            rejectionReason = rejectionReason ?: "",
            reviewedBy = adminUid
        )

        apiService.verifyDoctor(uid, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) onSuccess()
                else onFailure(response.errorBody()?.string() ?: "Failed to update verification status")
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error updating verification status")
            }
        })
    }
}
