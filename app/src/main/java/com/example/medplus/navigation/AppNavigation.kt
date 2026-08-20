package com.example.medplus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medplus.ui.auth.ForgotPasswordScreen
import com.example.medplus.ui.auth.LoginScreen
import com.example.medplus.ui.auth.RegisterScreen
import com.example.medplus.ui.auth.RoleSelectionScreen
import com.example.medplus.ui.onboarding.OnboardingScreen
import com.example.medplus.ui.splash.SplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.auth.viewmodel.AuthViewModel
import com.example.medplus.ui.patient.MyAppointmentsScreen
import com.example.medplus.ui.patient.AppointmentDetailsScreen
import com.example.medplus.viewmodel.MyAppointmentsViewModel
import com.example.medplus.ui.doctor.DoctorProfileSetupScreen
import com.example.medplus.ui.doctor.DoctorVerificationDocumentsScreen
import com.example.medplus.ui.doctor.DoctorVerificationSummaryScreen
import com.example.medplus.ui.doctor.DoctorVerificationPendingScreen
import com.example.medplus.ui.doctor.DoctorRejectionScreen
import com.example.medplus.viewmodel.DoctorProfileViewModel
import com.example.medplus.ui.admin.AdminDashboardScreen
import com.example.medplus.ui.admin.PendingDoctorsScreen
import com.example.medplus.ui.admin.AdminDoctorVerificationScreen
import com.example.medplus.ui.admin.AdminDoctorReviewScreen
import com.example.medplus.viewmodel.AdminDoctorReviewViewModel
import com.example.medplus.ui.admin.AdminProfileScreen
import com.example.medplus.viewmodel.AdminProfileViewModel
import com.example.medplus.ui.admin.AdminNotificationsScreen
import com.example.medplus.viewmodel.AdminNotificationsViewModel
import com.example.medplus.ui.admin.AdminPatientsScreen
import com.example.medplus.viewmodel.AdminPatientsViewModel
import com.example.medplus.ui.doctor.DoctorDashboardScreen
import com.example.medplus.ui.doctor.DoctorAvailabilityScreen
import com.example.medplus.dashboard.viewmodel.DoctorDashboardViewModel
import com.example.medplus.viewmodel.DoctorAvailabilityViewModel
import com.example.medplus.ui.doctor.DoctorProfileScreen
import com.example.medplus.ui.doctor.DoctorNotificationsScreen
import com.example.medplus.ui.doctor.DoctorAppointmentsScreen
import com.example.medplus.ui.doctor.DoctorQueueScreen
import com.example.medplus.ui.doctor.DoctorPatientsScreen
import com.example.medplus.ui.doctor.DoctorPatientDetailsScreen
import com.example.medplus.viewmodel.DoctorNotificationsViewModel
import com.example.medplus.viewmodel.DoctorAppointmentsViewModel
import com.example.medplus.viewmodel.DoctorQueueViewModel
import com.example.medplus.viewmodel.DoctorPatientsViewModel
import com.example.medplus.viewmodel.DoctorPatientDetailsViewModel
import com.example.medplus.ui.home.HomeScreen
import com.example.medplus.ui.auth.MedPlusRole
import androidx.compose.runtime.*
import com.example.medplus.dashboard.viewmodel.DashboardViewModel
import com.example.medplus.ui.patient.PatientDashboardScreen
import com.example.medplus.ui.patient.BookAppointmentScreen
import com.example.medplus.ui.patient.QueueTrackingScreen
import com.example.medplus.ui.patient.ProfileScreen
import com.example.medplus.ui.patient.AppointmentDetailsScreen
import com.example.medplus.ui.patient.PatientMedicalRecordsScreen
import com.example.medplus.ui.patient.PatientMedicalRecordDetailsScreen
import com.example.medplus.ui.patient.PatientPrescriptionsScreen
import com.example.medplus.ui.patient.MedicineReminderScreen
import com.example.medplus.viewmodel.PatientMedicalRecordsViewModel
import com.example.medplus.viewmodel.MedicineReminderViewModel
import com.example.medplus.viewmodel.PatientNotificationsViewModel
import com.example.medplus.ui.patient.PatientNotificationsScreen
import com.example.medplus.viewmodel.BookAppointmentViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medplus.data.network.SessionManager
import com.example.medplus.repository.DoctorRepository
import com.example.medplus.ui.theme.Primary
import android.util.Log
import android.content.Context

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = viewModel()
    val doctorProfileViewModel: DoctorProfileViewModel = viewModel()
    
    // Independent loading states
    var isEmailLoginLoading by remember { mutableStateOf(false) }
    var isGoogleLoginLoading by remember { mutableStateOf(false) }
    var isGeneralLoading by remember { mutableStateOf(false) }

    val context = navController.context
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var onboardingCompleted by remember { mutableStateOf(prefs.getBoolean("onboarding_completed", false)) }
    val sessionManager = remember { SessionManager.getInstance(context) }

    // DEBUG: Set this to true once and run the app to reset onboarding for testing, then set back to false.
    val DEBUG_RESET_ONBOARDING = false 
    if (DEBUG_RESET_ONBOARDING) {
        LaunchedEffect(Unit) {
            prefs.edit().putBoolean("onboarding_completed", false).apply()
            onboardingCompleted = false
            Log.d("STARTUP_DEBUG", "Onboarding state RESET for testing")
        }
    }

    // Helper function for Auth navigation based on role and doctor status
    val handleAuthNavigation: (String, String?) -> Unit = { userRole, popUpToRoute ->
        val normalizedRole = userRole.trim().uppercase()
        val uid = sessionManager.getUserId()
        val targetPopUpTo = popUpToRoute ?: Screen.Login.route
        
        Log.d("STARTUP_DEBUG", "Authenticated user role = $normalizedRole")
        Log.d("AUTH_DEBUG", "Login UID: $uid")
        Log.d("AUTH_DEBUG", "User role: $normalizedRole")

        when (normalizedRole) {
            "ADMIN" -> {
                navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(targetPopUpTo) { inclusive = true }
                    launchSingleTop = true
                }
            }
            "PATIENT" -> {
                navController.navigate(Screen.PatientDashboard.route) {
                    popUpTo(targetPopUpTo) { inclusive = true }
                    launchSingleTop = true
                }
            }
            "DOCTOR" -> {
                if (uid != null) {
                    isGeneralLoading = true
                    val doctorRepository = DoctorRepository()
                    doctorRepository.getDoctorProfile(
                        uid = uid,
                        onSuccess = { doc ->
                            isGeneralLoading = false
                            val exists = doc != null
                            val rawStatus = doc?.verificationStatus?.trim()?.uppercase() ?: "DRAFT"
                            
                            Log.d("AUTH_DEBUG", "Doctor routing: exists=$exists, status=$rawStatus")

                            val destination = if (!exists || rawStatus == "DRAFT") {
                                Log.d("AUTH_DEBUG", "Doctor destination: Profile Setup")
                                Screen.DoctorProfileSetup.route
                            } else {
                                when (rawStatus) {
                                    "VERIFIED", "APPROVED" -> {
                                        Log.d("AUTH_DEBUG", "Doctor destination: Dashboard")
                                        Screen.DoctorDashboard.route
                                    }
                                    "PENDING" -> {
                                        Log.d("AUTH_DEBUG", "Doctor destination: Pending Screen")
                                        Screen.DoctorVerificationPending.route
                                    }
                                    "REJECTED" -> {
                                        Log.d("AUTH_DEBUG", "Doctor destination: Rejection Screen")
                                        Screen.DoctorRejection.route
                                    }
                                    else -> {
                                        Log.d("AUTH_DEBUG", "Doctor destination: Unknown status '$rawStatus' -> Profile Setup")
                                        Screen.DoctorProfileSetup.route
                                    }
                                }
                            }

                            doctorProfileViewModel.resetState()
                            navController.navigate(destination) {
                                popUpTo(targetPopUpTo) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onFailure = { error ->
                            isGeneralLoading = false
                            Log.e("AUTH_DEBUG", "Error fetching doctor profile: $error")
                            doctorProfileViewModel.resetState()
                            navController.navigate(Screen.DoctorProfileSetup.route) {
                                popUpTo(targetPopUpTo) { inclusive = true }
                            }
                        }
                    )
                } else {
                    isGeneralLoading = false
                }
            }
            else -> {
                Log.e("STARTUP_DEBUG", "Invalid or missing user role: $normalizedRole")
                navController.navigate(Screen.Login.createRoute()) {
                    popUpTo(targetPopUpTo) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // Splash Screen
        composable(Screen.Splash.route) {
            Log.d("STARTUP_DEBUG", "Splash started")
            val isUserLoggedIn = sessionManager.isLoggedIn()
            Log.d("STARTUP_DEBUG", "Onboarding completed = $onboardingCompleted")
            Log.d("STARTUP_DEBUG", "User is logged in = $isUserLoggedIn")

            SplashScreen(
                navController = navController,
                onSplashFinished = {
                    val destination = when {
                        !onboardingCompleted -> Screen.Onboarding.route
                        !isUserLoggedIn -> Screen.Login.createRoute()
                        else -> "AUTHENTICATED_USER"
                    }
                    Log.d("STARTUP_DEBUG", "Startup destination = $destination")

                    if (destination == "AUTHENTICATED_USER") {
                        val role = sessionManager.getRole() ?: "PATIENT"
                        handleAuthNavigation(role, Screen.Splash.route)
                    } else {
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }


        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                navController = navController,
                onSkip = {
                    Log.d("STARTUP_DEBUG", "Onboarding skipped, navigating to Login")
                    prefs.edit().putBoolean("onboarding_completed", true).apply()
                    onboardingCompleted = true
                    navController.navigate(Screen.Login.createRoute()) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onGetStarted = {
                    Log.d("STARTUP_DEBUG", "Onboarding completed, navigating to Login")
                    prefs.edit().putBoolean("onboarding_completed", true).apply()
                    onboardingCompleted = true
                    navController.navigate(Screen.Login.createRoute()) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) { backStackEntry ->
            val selectedRole = backStackEntry.arguments?.getString("role")

            LoginScreen(
                isEmailLoading = isEmailLoginLoading,
                isGoogleLoading = isGoogleLoginLoading,
                navController = navController,

                onLoginClick = { email, password, rememberMe ->
                    if (isEmailLoginLoading || isGoogleLoginLoading) return@LoginScreen
                    isEmailLoginLoading = true

                    authViewModel.loginUser(
                        email = email,
                        password = password,
                        onSuccess = { userRole ->
                            isEmailLoginLoading = false
                            handleAuthNavigation(userRole, Screen.Login.route)
                        },
                        onFailure = { error ->
                            isEmailLoginLoading = false
                            android.widget.Toast.makeText(
                                navController.context,
                                error,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                onGoogleLogin = { role ->
                    if (isEmailLoginLoading || isGoogleLoginLoading) return@LoginScreen
                    android.util.Log.d("GOOGLE_AUTH_DEBUG", "Google Sign-In button clicked with role: $role")
                    isGoogleLoginLoading = true
                    authViewModel.googleSignIn(
                        navController = navController,
                        selectedRole = role,
                        onSuccess = { userRole ->
                            isGoogleLoginLoading = false
                            handleAuthNavigation(userRole, Screen.Login.route)
                        },
                        onFailure = { error ->
                            isGoogleLoginLoading = false
                            if (error != "CANCELLED") {
                                android.util.Log.e("GOOGLE_AUTH_DEBUG", "Google Login Callback FAILURE: $error")
                                android.widget.Toast.makeText(
                                    navController.context,
                                    error,
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } else {
                                android.util.Log.d("GOOGLE_AUTH_DEBUG", "Google Sign-In cancelled by user")
                            }
                        }
                    )
                },

                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },

                onRegisterClick = {
                    navController.navigate(Screen.RoleSelection.route)
                }

            )

        }

        // Register Screen
        composable(Screen.Register.route) { backStackEntry ->
            val selectedRole = backStackEntry.arguments?.getString("role") ?: "PATIENT"

            RegisterScreen(
                navController = navController,
                isLoading = isGeneralLoading,
                onContinueClick = { fullName,
                                    email,
                                    phone,
                                    password,
                                    _ ->

                    android.util.Log.d("DOCTOR_REG_DEBUG", "Register button clicked for role: $selectedRole")

                    if (selectedRole == "ADMIN") {
                        android.widget.Toast.makeText(
                            navController.context,
                            "Administrator registration is not allowed. Please contact the system administrator.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        return@RegisterScreen
                    }

                    isGeneralLoading = true

                    authViewModel.registerUser(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        password = password,
                        role = selectedRole,

                        onSuccess = {
                            isGeneralLoading = false
                            Log.d("DOCTOR_REG_DEBUG", "Registration successful callback")

                            if (selectedRole.trim().uppercase() == "DOCTOR") {
                                Log.d("DOCTOR_REG_DEBUG", "Navigating to Doctor Profile Setup")
                                // Ensure state is reset for the new doctor
                                doctorProfileViewModel.resetState()
                                navController.navigate(Screen.DoctorProfileSetup.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                Log.d("REGISTRATION_DEBUG", "Patient registered. Signing out and navigating to Login")
                                authViewModel.logout()
                                android.widget.Toast.makeText(
                                    navController.context,
                                    "Registration successful. Please login.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                
                                navController.navigate(Screen.Login.createRoute()) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },

                        onFailure = { error ->
                            isGeneralLoading = false
                            android.widget.Toast.makeText(
                                navController.context,
                                error,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // Forgot Password
        composable(Screen.ForgotPassword.route) {

            ForgotPasswordScreen(

                navController = navController,

                onSendResetLink = { email ->

                    authViewModel.resetPassword(
                        email = email,

                        onSuccess = {

                            android.widget.Toast.makeText(
                                navController.context,
                                "Password reset link sent successfully.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()

                            navController.popBackStack()
                        },

                        onFailure = { error ->

                            android.widget.Toast.makeText(
                                navController.context,
                                error,
                                android.widget.Toast.LENGTH_LONG
                            ).show()

                        }
                    )
                },

                onBackToLogin = {
                    navController.popBackStack()
                }

            )

        }

        // Role Selection
        composable(Screen.RoleSelection.route) {

            RoleSelectionScreen(

                navController = navController,

                onContinueClick = { selectedRole ->
                    navController.navigate(Screen.Register.createRoute(selectedRole.name))
                }

            )

        }

        // Patient Dashboard
        composable(Screen.PatientDashboard.route) {

            val dashboardViewModel: DashboardViewModel = viewModel()
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        android.util.Log.d("PATIENT_DASHBOARD_DEBUG", "Dashboard resumed. Loading fresh data.")
                        dashboardViewModel.loadDashboardData()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            PatientDashboardScreen(
                uiState = uiState,
                onNotificationClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onBookAppointmentClick = {
                    navController.navigate(Screen.BookAppointment.createRoute())
                },
                onMyAppointmentsClick = {
                    navController.navigate(Screen.MyAppointments.route)
                },
                onQueueStatusClick = {
                    navController.navigate(Screen.QueueTracking.route)
                },
                onMedicalRecordsClick = {
                    navController.navigate(Screen.PatientMedicalRecords.route)
                },
                onPrescriptionsClick = {
                    navController.navigate(Screen.PatientPrescriptions.route)
                },
                onMedicineReminderClick = {
                    navController.navigate(Screen.MedicineReminders.route)
                },
                onViewAppointmentDetailsClick = { appointmentId ->
                    navController.navigate(Screen.AppointmentDetails.createRoute(appointmentId))
                },
                onRescheduleAppointmentClick = { appointmentId ->
                    navController.navigate(Screen.BookAppointment.createRoute(appointmentId))
                }
            )
        }

        // Appointment Details
        composable(Screen.AppointmentDetails.route) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            AppointmentDetailsScreen(
                appointmentId = appointmentId,
                onBackClick = { navController.popBackStack() },
                onViewLiveQueueClick = {
                    navController.navigate(Screen.QueueTracking.route)
                }
            )
        }

        // Queue Tracking
        composable(Screen.QueueTracking.route) {
            val dashboardViewModel: DashboardViewModel = viewModel()
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

            QueueTrackingScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            val dashboardViewModel: DashboardViewModel = viewModel()
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

            ProfileScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    doctorProfileViewModel.resetState()
                    navController.navigate(Screen.Login.createRoute()) {
                        popUpTo(Screen.PatientDashboard.route) { inclusive = true }
                    }
                },
                onSaveChanges = { fullName, phone ->
                    dashboardViewModel.updateUserProfile(fullName, phone) { success ->
                        if (success) {
                            android.widget.Toast.makeText(
                                navController.context,
                                "Profile updated successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            android.widget.Toast.makeText(
                                navController.context,
                                "Failed to update profile",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        // Patient Notifications
        composable(Screen.Notifications.route) {
            val patientNotificationsViewModel: PatientNotificationsViewModel = viewModel()
            PatientNotificationsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = patientNotificationsViewModel
            )
        }

        // Patient Medical Records
        composable(Screen.PatientMedicalRecords.route) {
            val patientMedicalRecordsViewModel: PatientMedicalRecordsViewModel = viewModel()
            PatientMedicalRecordsScreen(
                onBackClick = { navController.popBackStack() },
                onRecordClick = { recordId ->
                    navController.navigate(Screen.PatientMedicalRecordDetails.createRoute(recordId))
                },
                viewModel = patientMedicalRecordsViewModel
            )
        }

        // Patient Medical Record Details
        composable(Screen.PatientMedicalRecordDetails.route) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
            val patientMedicalRecordsViewModel: PatientMedicalRecordsViewModel = viewModel()
            PatientMedicalRecordDetailsScreen(
                recordId = recordId,
                onBackClick = { navController.popBackStack() },
                viewModel = patientMedicalRecordsViewModel
            )
        }

        // Patient Prescriptions
        composable(Screen.PatientPrescriptions.route) {
            val patientMedicalRecordsViewModel: PatientMedicalRecordsViewModel = viewModel()
            PatientPrescriptionsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = patientMedicalRecordsViewModel
            )
        }

        // Medicine Reminders
        composable(Screen.MedicineReminders.route) {
            val medicineReminderViewModel: MedicineReminderViewModel = viewModel()
            MedicineReminderScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = medicineReminderViewModel
            )
        }

        // Doctor Dashboard
        composable(Screen.DoctorDashboard.route) {
            val profileState by doctorProfileViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                doctorProfileViewModel.fetchProfile()
            }

            LaunchedEffect(profileState.profile, profileState.isLoading, profileState.isLoaded) {
                if (profileState.isLoaded && !profileState.isLoading) {
                    val profile = profileState.profile
                    val status = profile?.verificationStatus?.trim()?.uppercase() ?: "DRAFT"
                    
                    if (profile == null) {
                        navController.navigate(Screen.DoctorProfileSetup.route) {
                            popUpTo(Screen.DoctorDashboard.route) { inclusive = true }
                        }
                    } else {
                        when (status) {
                            "DRAFT" -> {
                                navController.navigate(Screen.DoctorProfileSetup.route) {
                                    popUpTo(Screen.DoctorDashboard.route) { inclusive = true }
                                }
                            }
                            "PENDING" -> {
                                navController.navigate(Screen.DoctorVerificationPending.route) {
                                    popUpTo(Screen.DoctorDashboard.route) { inclusive = true }
                                }
                            }
                            "REJECTED" -> {
                                navController.navigate(Screen.DoctorRejection.route) {
                                    popUpTo(Screen.DoctorDashboard.route) { inclusive = true }
                                }
                            }
                            "APPROVED" -> {
                                // Stay here
                            }
                        }
                    }
                }
            }

            if (profileState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (profileState.profile?.verificationStatus?.trim()?.uppercase() in listOf("APPROVED", "VERIFIED")) {
                val doctorDashboardViewModel: DoctorDashboardViewModel = viewModel()
                DoctorDashboardScreen(
                    viewModel = doctorDashboardViewModel,
                    onNotificationClick = {
                        navController.navigate(Screen.DoctorNotifications.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.DoctorProfile.route)
                    },
                    onAppointmentsClick = {
                        navController.navigate(Screen.DoctorAppointments.route)
                    },
                    onPatientsClick = {
                        navController.navigate(Screen.DoctorPatients.route)
                    },
                    onQueueClick = {
                        navController.navigate(Screen.DoctorQueue.route)
                    },
                    onAvailabilityClick = {
                        navController.navigate(Screen.DoctorAvailability.route)
                    },
                    onViewPatientClick = { patientId ->
                        navController.navigate(Screen.DoctorPatientDetails.createRoute(patientId))
                    }
                )
            }
        }

        // Doctor Profile Setup
        composable(Screen.DoctorProfileSetup.route) {
            DoctorProfileSetupScreen(
                onContinueToDocuments = {
                    navController.navigate(Screen.DoctorVerificationDocuments.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = doctorProfileViewModel
            )
        }

        // Doctor Verification Documents
        composable(Screen.DoctorVerificationDocuments.route) {
            DoctorVerificationDocumentsScreen(
                onContinue = {
                    navController.navigate(Screen.DoctorVerificationSummary.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmissionSuccess = {
                    // Not used here anymore as we go to summary
                },
                viewModel = doctorProfileViewModel
            )
        }

        // Doctor Verification Summary
        composable(Screen.DoctorVerificationSummary.route) {
            DoctorVerificationSummaryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmissionSuccess = {
                    navController.navigate(Screen.DoctorVerificationPending.route) {
                        popUpTo(Screen.DoctorProfileSetup.route) { inclusive = true }
                    }
                },
                viewModel = doctorProfileViewModel
            )
        }

        // Doctor Verification Pending
        composable(Screen.DoctorVerificationPending.route) {
            val profileState by doctorProfileViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(profileState.profile) {
                val status = profileState.profile?.verificationStatus
                if (status == "APPROVED" || status == "VERIFIED") {
                    navController.navigate(Screen.DoctorDashboard.route) {
                        popUpTo(Screen.DoctorVerificationPending.route) { inclusive = true }
                    }
                } else if (status == "REJECTED") {
                    navController.navigate(Screen.DoctorRejection.route) {
                        popUpTo(Screen.DoctorVerificationPending.route) { inclusive = true }
                    }
                }
            }

            DoctorVerificationPendingScreen(
                viewModel = doctorProfileViewModel,
                onLogout = {
                    authViewModel.logout()
                    doctorProfileViewModel.resetState()
                    navController.navigate(Screen.Login.createRoute()) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Doctor Rejection
        composable(Screen.DoctorRejection.route) {
            DoctorRejectionScreen(
                viewModel = doctorProfileViewModel,
                onEditProfile = {
                    navController.navigate(Screen.DoctorProfileSetup.route) {
                        popUpTo(Screen.DoctorRejection.route) { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    doctorProfileViewModel.resetState()
                    navController.navigate(Screen.Login.createRoute()) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Doctor Availability
        composable(Screen.DoctorAvailability.route) {
            val availabilityViewModel: DoctorAvailabilityViewModel = viewModel()
            DoctorAvailabilityScreen(
                viewModel = availabilityViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Doctor Profile
        composable(Screen.DoctorProfile.route) {
            DoctorProfileScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController,
                viewModel = doctorProfileViewModel
            )
        }

        // Doctor Notifications
        composable(Screen.DoctorNotifications.route) {
            val doctorNotificationsViewModel: DoctorNotificationsViewModel = viewModel()
            DoctorNotificationsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = doctorNotificationsViewModel
            )
        }

        // Doctor Appointments
        composable(Screen.DoctorAppointments.route) {
            val doctorAppointmentsViewModel: DoctorAppointmentsViewModel = viewModel()
            DoctorAppointmentsScreen(
                onBackClick = { navController.popBackStack() },
                onViewDetails = { appointmentId ->
                    navController.navigate(Screen.AppointmentDetails.createRoute(appointmentId))
                },
                viewModel = doctorAppointmentsViewModel
            )
        }

        // Doctor Queue
        composable(Screen.DoctorQueue.route) {
            val doctorQueueViewModel: DoctorQueueViewModel = viewModel()
            DoctorQueueScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = doctorQueueViewModel
            )
        }

        // Doctor Patients
        composable(Screen.DoctorPatients.route) {
            val doctorPatientsViewModel: DoctorPatientsViewModel = viewModel()
            DoctorPatientsScreen(
                onBackClick = { navController.popBackStack() },
                onPatientClick = { patientId ->
                    navController.navigate(Screen.DoctorPatientDetails.createRoute(patientId))
                },
                viewModel = doctorPatientsViewModel
            )
        }

        // Doctor Patient Details
        composable(Screen.DoctorPatientDetails.route) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            val doctorPatientDetailsViewModel: DoctorPatientDetailsViewModel = viewModel()
            DoctorPatientDetailsScreen(
                patientId = patientId,
                onBackClick = { navController.popBackStack() },
                viewModel = doctorPatientDetailsViewModel
            )
        }

        // Book Appointment
        composable(
            route = Screen.BookAppointment.route,
            arguments = listOf(
                androidx.navigation.navArgument("rescheduleId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val rescheduleId = backStackEntry.arguments?.getString("rescheduleId")?.takeIf { it != "{rescheduleId}" && it.isNotEmpty() }
            val bookAppointmentViewModel: BookAppointmentViewModel = viewModel()
            
            LaunchedEffect(rescheduleId) {
                rescheduleId?.let {
                    bookAppointmentViewModel.setRescheduleAppointmentId(it)
                }
            }

            BookAppointmentScreen(
                viewModel = bookAppointmentViewModel,
                onBackClick = { navController.popBackStack() },
                onViewAppointmentClick = { appointmentId ->
                    navController.navigate(Screen.AppointmentDetails.createRoute(appointmentId)) {
                        popUpTo(Screen.BookAppointment.route) { inclusive = true }
                    }
                },
                onMyAppointmentsClick = {
                    navController.navigate(Screen.MyAppointments.route) {
                        popUpTo(Screen.BookAppointment.route) { inclusive = true }
                    }
                }
            )
        }

        // My Appointments
        composable(Screen.MyAppointments.route) {
            val myAppointmentsViewModel: MyAppointmentsViewModel = viewModel()
            MyAppointmentsScreen(
                viewModel = myAppointmentsViewModel,
                onBackClick = { navController.popBackStack() },
                onBookAppointmentClick = {
                    navController.navigate(Screen.BookAppointment.createRoute())
                },
                onViewDetailsClick = { appointmentId ->
                    navController.navigate(Screen.AppointmentDetails.createRoute(appointmentId))
                }
            )
        }

        // Appointment Details
        composable(Screen.AppointmentDetails.route) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            AppointmentDetailsScreen(
                appointmentId = appointmentId,
                onBackClick = { navController.popBackStack() },
                onViewLiveQueueClick = {
                    navController.navigate(Screen.QueueTracking.route)
                }
            )
        }

        // Admin Dashboard
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onDoctorClick = { doctorUid ->
                    navController.navigate(Screen.AdminDoctorReview.createRoute(doctorUid))
                },
                onPendingClick = {
                    navController.navigate(Screen.AdminPendingDoctors.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.AdminProfile.route)
                },
                onNotificationClick = {
                    navController.navigate(Screen.AdminNotifications.route)
                },
                onPatientsClick = {
                    navController.navigate(Screen.AdminPatients.route)
                }
            )
        }

        // Admin Profile
        composable(Screen.AdminProfile.route) {
            val adminProfileViewModel: AdminProfileViewModel = viewModel()
            AdminProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    doctorProfileViewModel.resetState()
                    navController.navigate(Screen.Login.createRoute()) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = adminProfileViewModel
            )
        }

        // Admin Notifications
        composable(Screen.AdminNotifications.route) {
            val adminNotificationsViewModel: AdminNotificationsViewModel = viewModel()
            AdminNotificationsScreen(
                onBackClick = { navController.popBackStack() },
                onDoctorVerificationClick = { doctorUid ->
                    navController.navigate(Screen.AdminDoctorReview.createRoute(doctorUid))
                },
                viewModel = adminNotificationsViewModel
            )
        }

        // Admin Patients
        composable(Screen.AdminPatients.route) {
            val adminPatientsViewModel: AdminPatientsViewModel = viewModel()
            AdminPatientsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = adminPatientsViewModel
            )
        }

        // Admin Pending Doctors
        composable(Screen.AdminPendingDoctors.route) {
            PendingDoctorsScreen(
                onBackClick = { navController.popBackStack() },
                onReviewClick = { doctorUid ->
                    navController.navigate(Screen.AdminDoctorReview.createRoute(doctorUid))
                }
            )
        }

        // Admin Doctor Verification
        composable(Screen.AdminDoctorVerification.route) {
            AdminDoctorVerificationScreen(
                onBackClick = { navController.popBackStack() },
                onReviewClick = { doctorUid ->
                    navController.navigate(Screen.AdminDoctorReview.createRoute(doctorUid))
                }
            )
        }

        // Admin Doctor Review Details
        composable(Screen.AdminDoctorReview.route) { backStackEntry ->
            val doctorUid = backStackEntry.arguments?.getString("doctorUid") ?: ""
            AdminDoctorReviewScreen(
                doctorUid = doctorUid,
                onBackClick = { navController.popBackStack() },
                onActionSuccess = {
                    navController.popBackStack()
                }
            )
        }



    }

}
