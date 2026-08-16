package com.example.medplus.ui.doctor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.DoctorProfileViewModel
import com.example.medplus.data.network.SessionManager
import com.google.firebase.Timestamp
import com.example.medplus.utils.HospitalDepartments
import android.util.Log

/**
 * Doctor Profile Setup / Verification screen.
 * Handles both initial setup and correction of rejected profiles.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoctorProfileSetupScreen(
    onContinueToDocuments: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: DoctorProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionManager = remember { SessionManager.getInstance(com.google.firebase.FirebaseApp.getInstance().applicationContext) }
    val currentUid = sessionManager.getUserId()

    val scrollState = rememberScrollState()

    // Form State
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    var qualification by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var departmentExpanded by remember { mutableStateOf(false) }
    var experienceYears by remember { mutableStateOf("") }
    var registrationAuthority by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }

    // Trigger fetch on UID change or screen entry
    LaunchedEffect(currentUid) {
        if (currentUid != null) {
            Log.d("DOCTOR_PROFILE_DEBUG", "Profile screen opened for UID: $currentUid")
            viewModel.fetchProfile()
            
            // Set initial values from Auth if profile hasn't loaded yet
            fullName = sessionManager.getName() ?: ""
            email = sessionManager.getEmail() ?: ""
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            android.widget.Toast.makeText(
                com.google.firebase.FirebaseApp.getInstance().applicationContext,
                error,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // Update form when profile data is received from Firestore
    LaunchedEffect(uiState.profile) {
        val p = uiState.profile
        if (p != null && p.uid == currentUid) {
            Log.d("DOCTOR_PROFILE_DEBUG", "Profile found for current UID - populating form")
            fullName = p.fullName
            email = p.email
            phone = p.phone
            qualification = p.qualification
            specialization = p.specialization
            department = p.department
            experienceYears = if (p.experienceYears > 0) p.experienceYears.toString() else ""
            registrationAuthority = p.registrationAuthority
            registrationNumber = p.registrationNumber
        } else if (p == null) {
            Log.d("DOCTOR_PROFILE_DEBUG", "No profile found for current UID - initializing empty/default form")
            // Reset form to defaults (plus Auth info)
            fullName = sessionManager.getName() ?: ""
            email = sessionManager.getEmail() ?: ""
            phone = ""
            qualification = ""
            specialization = ""
            department = ""
            experienceYears = ""
            registrationAuthority = ""
            registrationNumber = ""
        }
    }

    val authorities = listOf("State Medical Council", "National Medical Commission")
    var authorityExpanded by remember { mutableStateOf(false) }

    // Validation state
    var attemptedSubmit by remember { mutableStateOf(false) }

    val isFullNameValid = fullName.isNotBlank()
    val isPhoneValid = phone.isNotBlank() && phone.length >= 10
    val isQualificationValid = qualification.isNotBlank()
    val isSpecializationValid = specialization.isNotBlank()
    val isDepartmentValid = department.isNotBlank()
    val isExperienceValid = experienceYears.isNotBlank() && experienceYears.toIntOrNull() != null
    
    val regNumTrimmed = registrationNumber.trim()
    val isRegNumFormatValid = regNumTrimmed.length in 5..30 && 
            regNumTrimmed.any { it.isLetterOrDigit() } &&
            regNumTrimmed.all { it.isLetterOrDigit() || it == '-' || it == '/' || it == ' ' }
    
    val isAuthorityValid = registrationAuthority.isNotBlank()

    val isFormValid = isFullNameValid && isPhoneValid && isQualificationValid && 
            isSpecializationValid && isDepartmentValid && isExperienceValid && 
            isRegNumFormatValid && isAuthorityValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Doctor Profile",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Text(
                            "Complete your professional profile",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading && uiState.profile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Rejection Alert
                if (uiState.profile?.verificationStatus == "REJECTED") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFC62828))
                            Column {
                                Text(
                                    "Profile Needs Correction",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                )
                                Text(
                                    uiState.profile?.rejectionReason ?: "Please review and resubmit.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryText
                                )
                            }
                        }
                    }
                }

                // Section 1: Personal Information
                ProfileSectionCard(title = "PERSONAL INFORMATION") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfessionalTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "Full Name",
                            icon = Icons.Outlined.Person,
                            error = if (attemptedSubmit && !isFullNameValid) "Full name is required" else null
                        )
                        
                        ProfessionalTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address",
                            icon = Icons.Outlined.Email,
                            enabled = false
                        )
                        
                        ProfessionalTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Phone Number",
                            icon = Icons.Outlined.Phone,
                            keyboardType = KeyboardType.Phone,
                            error = if (attemptedSubmit && !isPhoneValid) "Valid phone number is required" else null
                        )
                    }
                }

                // Section 2: Professional Information
                ProfileSectionCard(title = "PROFESSIONAL INFORMATION") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfessionalTextField(
                            value = qualification,
                            onValueChange = { qualification = it },
                            label = "Medical Qualification (e.g. MBBS, MD)",
                            icon = Icons.Outlined.School,
                            error = if (attemptedSubmit && !isQualificationValid) "Qualification is required" else null
                        )
                        ProfessionalTextField(
                            value = specialization,
                            onValueChange = { specialization = it },
                            label = "Specialization",
                            icon = Icons.Outlined.WorkspacePremium,
                            error = if (attemptedSubmit && !isSpecializationValid) "Specialization is required" else null
                        )

                        // Department Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = departmentExpanded,
                                onExpandedChange = { departmentExpanded = !departmentExpanded }
                            ) {
                                OutlinedTextField(
                                    value = department,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Department") },
                                    leadingIcon = { Icon(Icons.Outlined.AccountTree, null, tint = Primary) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = AppShapes.medium,
                                    isError = attemptedSubmit && !isDepartmentValid,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Outline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = departmentExpanded,
                                    onDismissRequest = { departmentExpanded = false }
                                ) {
                                    HospitalDepartments.departments.forEach { dept ->
                                        DropdownMenuItem(
                                            text = { Text(dept) },
                                            onClick = {
                                                department = dept
                                                departmentExpanded = false
                                                Log.d("DEPARTMENT_DEBUG", "Doctor selected department: $dept")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (attemptedSubmit && !isDepartmentValid) {
                            Text(
                                "Please select a department.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }

                        ProfessionalTextField(
                            value = experienceYears,
                            onValueChange = { experienceYears = it },
                            label = "Years of Experience",
                            icon = Icons.Outlined.History,
                            keyboardType = KeyboardType.Number,
                            error = if (attemptedSubmit && !isExperienceValid) "Please enter valid years" else null
                        )
                        ProfessionalTextField(
                            value = registrationNumber,
                            onValueChange = { registrationNumber = it },
                            label = "Medical Registration Number",
                            icon = Icons.Outlined.Badge,
                            error = if (attemptedSubmit && !isRegNumFormatValid) "Enter a valid medical registration number." else null
                        )
                        
                        Text(
                            "Enter the registration number issued by your medical registration authority.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        // Registration Authority Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = authorityExpanded,
                                onExpandedChange = { authorityExpanded = !authorityExpanded }
                            ) {
                                OutlinedTextField(
                                    value = registrationAuthority,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Registration Authority") },
                                    leadingIcon = { Icon(Icons.Outlined.AccountBalance, null, tint = Primary) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = authorityExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = AppShapes.medium,
                                    isError = attemptedSubmit && !isAuthorityValid,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Outline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = authorityExpanded,
                                    onDismissRequest = { authorityExpanded = false }
                                ) {
                                    authorities.forEach { authority ->
                                        DropdownMenuItem(
                                            text = { Text(authority) },
                                            onClick = {
                                                registrationAuthority = authority
                                                authorityExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (attemptedSubmit && !isAuthorityValid) {
                            Text(
                                "Please select a registration authority.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        attemptedSubmit = true
                        if (isFormValid) {
                            val profile = DoctorProfile(
                                uid = currentUid ?: "",
                                fullName = fullName.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                qualification = qualification.trim(),
                                specialization = specialization.trim(),
                                department = department.trim(),
                                experienceYears = experienceYears.toIntOrNull() ?: 0,
                                registrationAuthority = registrationAuthority.trim(),
                                registrationNumber = regNumTrimmed,
                                verificationStatus = "DRAFT",
                                submittedAt = Timestamp.now(),
                                // Preserving registration and verification docs if they exist
                                registrationCertificateUrl = uiState.profile?.registrationCertificateUrl ?: "",
                                verificationDocumentUrl = uiState.profile?.verificationDocumentUrl ?: ""
                            )
                            viewModel.updateProfile(profile)
                            onContinueToDocuments()
                        } else {
                            val missing = mutableListOf<String>()
                            if (!isFullNameValid) missing.add("Full Name")
                            if (!isPhoneValid) missing.add("Phone Number (10 digits)")
                            if (!isQualificationValid) missing.add("Qualification")
                            if (!isSpecializationValid) missing.add("Specialization")
                            if (!isDepartmentValid) missing.add("Department")
                            if (!isExperienceValid) missing.add("Experience Years")
                            if (!isRegNumFormatValid) missing.add("Reg Number (5-30 chars)")
                            if (!isAuthorityValid) missing.add("Reg Authority")
                            
                            android.widget.Toast.makeText(
                                com.google.firebase.FirebaseApp.getInstance().applicationContext,
                                "Please complete: ${missing.joinToString(", ")}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = AppShapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        "Continue to Verification",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 1.sp
                )
            )
            content()
        }
    }
}

@Composable
fun ProfessionalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    error: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Primary) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            minLines = minLines,
            shape = AppShapes.medium,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Outline,
                disabledBorderColor = Outline,
                disabledLabelColor = SecondaryText,
                disabledTextColor = PrimaryText,
                errorBorderColor = Color.Red
            )
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
