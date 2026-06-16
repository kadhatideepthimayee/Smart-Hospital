package com.example.smarthospitalqueue.ui.navigation

sealed class Screen(val route: String) {

    object Splash : Screen("splash")

    object Login : Screen("login")

    object Register : Screen("register")

    object ForgotPassword : Screen("forgot_password")

    // MAIN DASHBOARD
    object Dashboard : Screen("dashboard")

    // APPOINTMENTS
    object Appointment : Screen("appointment")
    object AppointmentConfirmation : Screen("appointment_confirmation")

    // QUEUE
    object Queue : Screen("queue")
    object QueueTracking : Screen("queue_tracking")

    // AI
    object AIPrediction : Screen("ai_prediction")

    // TOKEN
    object DigitalToken : Screen("digital_token")

    // NOTIFICATIONS
    object Notifications : Screen("notifications")

    // RECORDS
    object MedicalRecords : Screen("medical_records")

    // PAYMENTS
    object Payments : Screen("payments")

    // PROFILE
    object Profile : Screen("profile")
}

// ── Navigation Argument Keys ──────────────────────────────────────────────────

const val ARG_PHONE_NUMBER    = "phoneNumber"
const val ARG_DEPARTMENT_ID   = "departmentId"
const val ARG_QUEUE_ID        = "queueId"
const val ARG_APPOINTMENT_ID  = "appointmentId"

// ── Named Navigation Graphs ───────────────────────────────────────────────────

/** Nested graph route for the authentication flow. */
const val AUTH_GRAPH_ROUTE = "auth_graph"

/** Nested graph route for the main (post-login) flow. */
const val MAIN_GRAPH_ROUTE = "main_graph"
