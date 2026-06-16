package com.example.smarthospitalqueue.ui.navigation

import androidx.compose.runtime.Composable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthospitalqueue.ui.screens.auth.LoginScreen
import com.example.smarthospitalqueue.ui.screens.auth.SplashScreen
import com.example.smarthospitalqueue.ui.screens.auth.RegisterScreen
import com.example.smarthospitalqueue.ui.screens.auth.ForgotPasswordScreen
import com.example.smarthospitalqueue.ui.screens.patient.DashboardScreen
import com.example.smarthospitalqueue.ui.screens.appointment.BookAppointmentScreen
import com.example.smarthospitalqueue.ui.screens.queue.QueueTrackingScreen
import com.example.smarthospitalqueue.ui.screens.queue.AIPredictionScreen
import com.example.smarthospitalqueue.ui.screens.queue.DigitalTokenScreen
import com.example.smarthospitalqueue.ui.screens.patient.NotificationsScreen
import com.example.smarthospitalqueue.ui.screens.patient.MedicalRecordsScreen
import com.example.smarthospitalqueue.ui.screens.patient.PaymentsScreen
import com.example.smarthospitalqueue.ui.screens.patient.ProfileScreen
import com.example.smarthospitalqueue.ui.screens.appointment.AppointmentConfirmationScreen
import com.example.smarthospitalqueue.ui.screens.chat.ChatScreen
import com.example.smarthospitalqueue.data.BookingRepository
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // Splash Screen
        composable(Screen.Splash.route) {

            SplashScreen(
                onSplashFinished = {

                    navController.navigate(Screen.Login.route) {

                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {

            LoginScreen(

                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {

                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },

                onCreateAccount = {
                    navController.navigate(Screen.Register.route)
                },

                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(

                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },

                onRegisterSuccess = {

                    navController.navigate(Screen.Login.route) {

                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                },

                onGoogleSignIn = {

                    // Temporary direct navigation
                    navController.navigate(Screen.Dashboard.route)
                }
            )
        }

        // Forgot Password Screen
        composable(Screen.ForgotPassword.route) {

            ForgotPasswordScreen(

                onBackToLogin = {
                    navController.navigate(Screen.Login.route)
                },

                onSendResetLink = {

                    // Optional action
                }
            )
        }

        // Dashboard Screen
        val onNavigate: (String) -> Unit = { route ->
            when (route) {
                "book_appointment" -> navController.navigate(Screen.Appointment.route)
                "queue_tracking" -> navController.navigate(Screen.Queue.route)
                "ai_prediction" -> navController.navigate(Screen.AIPrediction.route)
                "digital_token" -> navController.navigate(Screen.DigitalToken.route)
                "notifications" -> navController.navigate(Screen.Notifications.route)
                "payments" -> navController.navigate(Screen.Payments.route)
                "medical_records" -> navController.navigate(Screen.MedicalRecords.route)
                "profile" -> navController.navigate(Screen.Profile.route)
                "ai_chat" -> navController.navigate("ai_chat")
                "settings" -> {} // Add settings route if needed
            }
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onNavigate = onNavigate
            )
        }
        // Book Appointment
        composable(Screen.Appointment.route) {
            BookAppointmentScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAppointmentBooked = { doctor, date, slot ->
                    val dateStr = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                    val feeStr = "₹${doctor.consultationFee}"
                    
                    // Update Repository
                    BookingRepository.bookedDoctorName = doctor.name
                    BookingRepository.bookedSpecialization = doctor.specialization
                    BookingRepository.bookedDepartment = doctor.department
                    BookingRepository.bookedHospital = doctor.hospital
                    BookingRepository.bookedDate = dateStr
                    BookingRepository.bookedTime = slot.time
                    BookingRepository.bookedFee = feeStr
                    BookingRepository.hasActiveBooking = true
                    BookingRepository.bookedToken = "T-${(10..99).random()}" // Generating a random token as requested
                    
                    navController.currentBackStackEntry?.savedStateHandle?.set("doctorName", doctor.name)
                    navController.currentBackStackEntry?.savedStateHandle?.set("specialization", doctor.specialization)
                    navController.currentBackStackEntry?.savedStateHandle?.set("department", doctor.department)
                    navController.currentBackStackEntry?.savedStateHandle?.set("hospital", doctor.hospital)
                    navController.currentBackStackEntry?.savedStateHandle?.set("date", dateStr)
                    navController.currentBackStackEntry?.savedStateHandle?.set("time", slot.time)
                    navController.currentBackStackEntry?.savedStateHandle?.set("fee", feeStr)
                    navController.currentBackStackEntry?.savedStateHandle?.set("token", BookingRepository.bookedToken)
                    
                    navController.navigate(Screen.AppointmentConfirmation.route)
                }
            )
        }

// Appointment Confirmation
        composable(Screen.AppointmentConfirmation.route) {
            val doctorName = navController.previousBackStackEntry?.savedStateHandle?.get<String>("doctorName")
            val specialization = navController.previousBackStackEntry?.savedStateHandle?.get<String>("specialization")
            val department = navController.previousBackStackEntry?.savedStateHandle?.get<String>("department")
            val hospital = navController.previousBackStackEntry?.savedStateHandle?.get<String>("hospital")
            val date = navController.previousBackStackEntry?.savedStateHandle?.get<String>("date")
            val time = navController.previousBackStackEntry?.savedStateHandle?.get<String>("time")
            val fee = navController.previousBackStackEntry?.savedStateHandle?.get<String>("fee")

            AppointmentConfirmationScreen(
                doctorName = doctorName ?: "",
                specialization = specialization ?: "",
                department = department ?: "",
                hospital = hospital ?: "",
                appointmentDate = date ?: "",
                appointmentTime = time ?: "",
                consultationFee = fee ?: "",
                onBackToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigate = onNavigate
            )
        }

// Queue Tracking
        composable(Screen.Queue.route) {
            QueueTrackingScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToAIPrediction = {
                    navController.navigate(Screen.AIPrediction.route)
                }
            )
        }

// AI Prediction
        composable(Screen.AIPrediction.route) {
            AIPredictionScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

// Digital Token
        composable(Screen.DigitalToken.route) {
            DigitalTokenScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

// Notifications
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

// Medical Records
        composable(Screen.MedicalRecords.route) {
            MedicalRecordsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

// Payments
        composable(Screen.Payments.route) {
            PaymentsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

// Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigate = onNavigate
            )
        }

        // Chat Screen
        composable("ai_chat") {
            ChatScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
