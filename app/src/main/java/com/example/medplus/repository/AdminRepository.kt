package com.example.medplus.repository

import com.example.medplus.model.AdminNotification
import com.example.medplus.model.DoctorProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

/**
 * Repository to handle admin-related data operations in Firestore.
 */
class AdminRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /**
     * Fetches admin notifications.
     */
    fun getAdminNotifications(
        onSuccess: (List<AdminNotification>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("admin_notifications")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(AdminNotification::class.java)?.copy(id = doc.id)
                }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch notifications")
            }
    }

    /**
     * Deletes a notification.
     */
    fun deleteNotification(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("admin_notifications").document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to delete notification")
            }
    }

    /**
     * Marks a notification as read.
     */
    fun markNotificationAsRead(id: String) {
        firestore.collection("admin_notifications").document(id)
            .update("isRead", true)
            .addOnFailureListener { e ->
                android.util.Log.e("AdminRepository", "Failed to mark notification read", e)
            }
    }

    /**
     * Gets the unread notification count.
     */
    fun getUnreadNotificationCount(
        onSuccess: (Int) -> Unit
    ) {
        firestore.collection("admin_notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onSuccess(querySnapshot.size())
            }
            .addOnFailureListener {
                onSuccess(0)
            }
    }

    /**
     * Fetches doctor profiles based on their verification status.
     */
    fun getDoctorsByStatus(
        status: String,
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("doctor_profiles")
            .whereEqualTo("verificationStatus", status)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(DoctorProfile::class.java)
                }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch doctors")
            }
    }

    /**
     * Fetches all doctor profiles.
     */
    fun getAllDoctorProfiles(
        onSuccess: (List<DoctorProfile>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("doctor_profiles")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(DoctorProfile::class.java)
                }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch doctors")
            }
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
        val updates = hashMapOf(
            "verificationStatus" to newStatus,
            "reviewedAt" to Timestamp.now(),
            "reviewedBy" to adminUid
        )
        if (rejectionReason != null) {
            updates["rejectionReason"] = rejectionReason
        }

        firestore.collection("doctor_profiles").document(uid)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                val title = "Verification Status Updated"
                val message = if (newStatus == "VERIFIED" || newStatus == "APPROVED") {
                    "Congratulations! Your professional profile has been verified by the administrator."
                } else {
                    "Your professional profile was rejected. Reason: ${rejectionReason ?: "Invalid credentials"}."
                }

                val notificationMap = hashMapOf(
                    "userId" to uid,
                    "title" to title,
                    "message" to message,
                    "type" to "VERIFICATION",
                    "isRead" to false,
                    "timestamp" to Timestamp.now()
                )

                firestore.collection("notifications")
                    .add(notificationMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onSuccess() }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update status")
            }
    }
}
