package com.example.medplus.ui.doctor

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.auth.viewmodel.AuthViewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.DoctorProfileViewModel
import com.example.medplus.data.network.SessionManager
import com.example.medplus.navigation.Screen
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    onBackClick: () -> Unit,
    navController: NavController,
    viewModel: DoctorProfileViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val uid = SessionManager.getInstance(context).getUserId()

    var isEditMode by remember { mutableStateOf(false) }
    
    // Form State
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var consultationFee by remember { mutableStateOf("") }
    var slotDuration by remember { mutableStateOf("") }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                try {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.createRoute()) {
                        popUpTo(0) { inclusive = true }
                    }
                } catch (e: Exception) {
                    Log.e("LOGOUT_ERROR", "Firebase signOut failed", e)
                    Toast.makeText(context, "Logout failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            fullName = profile.fullName
            phone = profile.phone
            specialization = profile.specialization
            department = profile.department
            qualification = profile.qualification
            experienceYears = profile.experienceYears.toString()
            consultationFee = profile.consultationFee.toString()
            slotDuration = profile.slotDuration.toString()
            Log.d("DOCTOR_PROFILE_DEBUG", "Profile loaded successfully")
        }
    }

    LaunchedEffect(uid) {
        Log.d("DOCTOR_PROFILE_DEBUG", "Loading profile for UID: $uid")
        viewModel.fetchProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditMode && uiState.profile != null) {
                        TextButton(onClick = { isEditMode = true }) {
                            Text("Edit", color = Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading && uiState.profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.errorMessage != null && uiState.profile == null) {
            ErrorState(
                message = "Unable to load your profile.",
                onRetry = { viewModel.fetchProfile() },
                modifier = Modifier.padding(padding)
            )
        } else if (uiState.profile != null) {
            val profile = uiState.profile!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile Header Card
                ProfileHeaderCard(profile)

                // Sections
                ProfileSection(title = "Personal Information") {
                    ProfileField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.Person
                    )
                    ProfileField(
                        label = "Email",
                        value = profile.email,
                        onValueChange = {},
                        isEditable = false,
                        icon = Icons.Outlined.Email
                    )
                    ProfileField(
                        label = "Phone Number",
                        value = phone,
                        onValueChange = { phone = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                }

                ProfileSection(title = "Professional Information") {
                    ProfileField(
                        label = "Department",
                        value = department,
                        onValueChange = { department = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.AccountTree
                    )
                    ProfileField(
                        label = "Specialization",
                        value = specialization,
                        onValueChange = { specialization = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.WorkspacePremium
                    )
                    ProfileField(
                        label = "Qualification",
                        value = qualification,
                        onValueChange = { qualification = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.School
                    )
                    ProfileField(
                        label = "Medical Registration Number",
                        value = profile.registrationNumber,
                        onValueChange = {},
                        isEditable = false,
                        icon = Icons.Outlined.Badge
                    )
                    ProfileField(
                        label = "Registration Authority",
                        value = profile.registrationAuthority,
                        onValueChange = {},
                        isEditable = false,
                        icon = Icons.Outlined.AccountBalance
                    )
                    ProfileField(
                        label = "Experience Years",
                        value = experienceYears,
                        onValueChange = { experienceYears = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.History,
                        keyboardType = KeyboardType.Number
                    )
                    ProfileField(
                        label = "Consultation Fee (₹)",
                        value = consultationFee,
                        onValueChange = { consultationFee = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.Payments,
                        keyboardType = KeyboardType.Number
                    )
                    ProfileField(
                        label = "Slot Duration (mins)",
                        value = slotDuration,
                        onValueChange = { slotDuration = it },
                        isEditable = isEditMode,
                        icon = Icons.Outlined.Timer,
                        keyboardType = KeyboardType.Number
                    )
                }

                ProfileSection(title = "Availability") {
                    ProfileInfoRow(Icons.Outlined.Today, "Working Days", profile.workingDays.joinToString(", "))
                    ProfileInfoRow(Icons.Outlined.Schedule, "Consultation Hours", "${profile.consultationStartTime} - ${profile.consultationEndTime}")
                }

                if (isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditMode = false },
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.medium
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val updatedProfile = profile.copy(
                                    fullName = fullName,
                                    phone = phone,
                                    department = department,
                                    specialization = specialization,
                                    qualification = qualification,
                                    experienceYears = experienceYears.toIntOrNull() ?: profile.experienceYears,
                                    consultationFee = consultationFee.toDoubleOrNull() ?: profile.consultationFee,
                                    slotDuration = slotDuration.toIntOrNull() ?: profile.slotDuration
                                )
                                viewModel.submitProfile(updatedProfile)
                                isEditMode = false
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Save")
                        }
                    }
                }

                // Logout Option
                if (!isEditMode) {
                    Spacer(Modifier.height(8.dp))
                    LogoutButton(onClick = { showLogoutDialog = true })
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Error.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Error, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "Logout",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Error
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = SecondaryText.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "Are you sure you want to logout?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text("Logout", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PrimaryText)
            }
        },
        containerColor = Surface,
        shape = AppShapes.large
    )
}

@Composable
fun ProfileHeaderCard(profile: DoctorProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Dr. ${profile.fullName}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryText
            )
            Text(
                profile.specialization.ifBlank { profile.department },
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
            Spacer(Modifier.height(12.dp))
            VerificationBadge(profile.verificationStatus)
        }
    }
}

@Composable
fun VerificationBadge(status: String) {
    val normalizedStatus = status.trim().uppercase()
    val (color, text, icon) = when (normalizedStatus) {
        "APPROVED" -> Triple(Success, "✓ Verified Doctor", Icons.Default.CheckCircle)
        "PENDING" -> Triple(Warning, "Verification Pending", Icons.Default.HourglassEmpty)
        "REJECTED" -> Triple(Error, "Verification Rejected", Icons.Default.Cancel)
        else -> Triple(SecondaryText, "Profile Incomplete", Icons.Default.Info)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryText,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditable: Boolean,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    if (isEditable) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Outline
            )
        )
    } else {
        ProfileInfoRow(icon, label, value)
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Primary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            Text(
                value.ifBlank { "Not set" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = PrimaryText
            )
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = PrimaryText)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
