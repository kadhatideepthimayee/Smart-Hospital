package com.example.medplus.navigation

/**
 * All navigation routes used in the MedPlus application.
 */
sealed class Screen(val route: String) {

    // Splash & Onboarding
    object Splash : Screen(route = "splash")
    object Onboarding : Screen(route = "onboarding")

    // Authentication
    object Login : Screen(route = "login?role={role}") {
        fun createRoute(role: String? = null) = if (role != null) "login?role=$role" else "login"
    }
    object Register : Screen(route = "register/{role}") {
        fun createRoute(role: String) = "register/$role"
    }

    object Home : Screen("home")
    object ForgotPassword : Screen(route = "forgot_password")
    object RoleSelection : Screen(route = "role_selection")

    // Patient
    object PatientDashboard : Screen(route = "patient_dashboard")
    object BookAppointment : Screen(route = "book_appointment")
    object MyAppointments : Screen(route = "my_appointments")
    object AppointmentDetails : Screen(route = "appointment_details/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment_details/$appointmentId"
    }
    object QueueStatus : Screen(route = "queue_status")
    object QueueTracking : Screen(route = "queue_tracking")
    object Notifications : Screen(route = "notifications")
    object Profile : Screen(route = "profile")
    object PatientMedicalRecords : Screen(route = "patient_medical_records")
    object PatientMedicalRecordDetails : Screen(route = "patient_medical_record_details/{recordId}") {
        fun createRoute(recordId: String) = "patient_medical_record_details/$recordId"
    }
    object PatientPrescriptions : Screen(route = "patient_prescriptions")
    object MedicineReminders : Screen(route = "medicine_reminders")

    // Doctor
    object DoctorDashboard : Screen(route = "doctor_dashboard")
    object DoctorProfileSetup : Screen(route = "doctor_profile_setup")
    object DoctorVerificationDocuments : Screen(route = "doctor_verification_documents")
    object DoctorVerificationSummary : Screen(route = "doctor_verification_summary")
    object DoctorVerificationPending : Screen(route = "doctor_verification_pending")
    object DoctorRejection : Screen(route = "doctor_rejection")
    object DoctorAvailability : Screen(route = "doctor_availability")
    object DoctorProfile : Screen(route = "doctor_profile")
    object DoctorNotifications : Screen(route = "doctor_notifications")
    object DoctorAppointments : Screen(route = "doctor_appointments")
    object DoctorQueue : Screen(route = "doctor_queue")
    object DoctorPatients : Screen(route = "doctor_patients")
    object DoctorPatientDetails : Screen(route = "doctor_patient_details/{patientId}") {
        fun createRoute(patientId: String) = "doctor_patient_details/$patientId"
    }

    // Admin
    object AdminDashboard : Screen(route = "admin_dashboard")
    object AdminProfile : Screen(route = "admin_profile")
    object AdminNotifications : Screen(route = "admin_notifications")
    object AdminPatients : Screen(route = "admin_patients")
    object AdminDoctorVerification : Screen(route = "admin_doctor_verification")
    object AdminPendingDoctors : Screen(route = "admin_pending_doctors")
    object AdminDoctorReview : Screen(route = "admin_doctor_review/{doctorUid}") {
        fun createRoute(doctorUid: String) = "admin_doctor_review/$doctorUid"
    }
}