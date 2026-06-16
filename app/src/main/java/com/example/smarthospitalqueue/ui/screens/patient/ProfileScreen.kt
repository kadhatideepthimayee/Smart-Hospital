package com.example.smarthospitalqueue.ui.screens.patient

// ════════════════════════════════════════════════════════════════════════════
//  ProfileScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Self-contained mock data · Aligned with all preceding screens
//  Patient: Arjun Raghavan · APOL-PAT-20241182
// ════════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

// ─── Color Palette (shared across all screens) ────────────────────────────────

private object PColors {
    val Primary           = Color(0xFF0B3D91)
    val PrimaryLight      = Color(0xFF1565C0)
    val PrimaryContainer  = Color(0xFFDCE8FF)
    val Secondary         = Color(0xFF00897B)
    val SecondaryContainer= Color(0xFFB2DFDB)
    val Background        = Color(0xFFF4F7FF)
    val Surface           = Color(0xFFFFFFFF)
    val SurfaceVariant    = Color(0xFFF0F4FF)
    val OnSurface         = Color(0xFF1A2744)
    val TextSecondary     = Color(0xFF5A6A8A)
    val Outline           = Color(0xFFCED8F5)
    val Success           = Color(0xFF2E7D32)
    val SuccessContainer  = Color(0xFFE8F5E9)
    val Warning           = Color(0xFFF57F17)
    val WarningContainer  = Color(0xFFFFF8E1)
    val Error             = Color(0xFFC62828)
    val ErrorContainer    = Color(0xFFFFEBEE)
    val GradientStart     = Color(0xFF0B3D91)
    val GradientEnd       = Color(0xFF1976D2)
    val BloodRed          = Color(0xFFC62828)
}

// ─── Mock Data — locked fields match Arjun Raghavan narrative ─────────────────

private object ProfileMockData {
    // ── LOCKED (registered at account creation) ────────────────────────────
    val name            = "Arjun Raghavan"
    val patientId       = "APOL-PAT-20241182"
    val email           = "arjun.raghavan@email.com"   // registered email — read-only
    val initials        = "AR"
    val avatarColor     = Color(0xFF1565C0)

    // ── EDITABLE ──────────────────────────────────────────────────────────
    var phone           = "+91 98765 43210"
    var dateOfBirth     = "12 March 1988"
    var age             = "37"
    var gender          = "Male"
    var bloodGroup      = "B+"
    var address         = "14, Rajiv Gandhi Nagar, Avadi, Chennai – 600071, Tamil Nadu"
    var emergencyName   = "Meena Raghavan (Spouse)"
    var emergencyPhone  = "+91 97654 32109"
    var allergies       = "Penicillin, Sulfa drugs"
    var chronicConditions= "Hyperlipidaemia, Borderline Hypertension"
    var insuranceId     = "HDFC-HEALTH-2024-9871"
    var insuranceExpiry = "31 Dec 2025"

    // ── Quick stats ────────────────────────────────────────────────────────
    val totalVisits     = 6
    val totalRecords    = 7
    val totalPaid       = "₹5,750"
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val registeredName = currentUser?.displayName ?: ProfileMockData.name
    val registeredEmail = currentUser?.email ?: ProfileMockData.email
    val initials = registeredName.split(" ").filter { it.isNotEmpty() }.let {
        if (it.size >= 2) "${it[0].first()}${it[1].first()}" else if (it.isNotEmpty()) "${it[0].first()}" else "P"
    }.uppercase()

    // Editable field states — locked fields (name, patientId, email) are not state
    var phone               by remember { mutableStateOf(ProfileMockData.phone) }
    var dateOfBirth         by remember { mutableStateOf(ProfileMockData.dateOfBirth) }
    var gender              by remember { mutableStateOf(ProfileMockData.gender) }
    var bloodGroup          by remember { mutableStateOf(ProfileMockData.bloodGroup) }
    var address             by remember { mutableStateOf(ProfileMockData.address) }
    var emergencyName       by remember { mutableStateOf(ProfileMockData.emergencyName) }
    var emergencyPhone      by remember { mutableStateOf(ProfileMockData.emergencyPhone) }
    var allergies           by remember { mutableStateOf(ProfileMockData.allergies) }
    var chronicConditions   by remember { mutableStateOf(ProfileMockData.chronicConditions) }
    var insuranceId         by remember { mutableStateOf(ProfileMockData.insuranceId) }
    var insuranceExpiry     by remember { mutableStateOf(ProfileMockData.insuranceExpiry) }

    var isEditMode          by remember { mutableStateOf(false) }
    var showLogoutDialog    by remember { mutableStateOf(false) }
    var showSaveSnack       by remember { mutableStateOf(false) }
    var headerVisible       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); headerVisible = true }
    LaunchedEffect(showSaveSnack) {
        if (showSaveSnack) { delay(2500); showSaveSnack = false }
    }

    // ── Logout dialog ───────────────────────────────────────────────────────
    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss  = { showLogoutDialog = false },
            onConfirm  = { showLogoutDialog = false }
        )
    }

    Scaffold(
        topBar = {
            ProfileTopBar(
                isEditMode = isEditMode,
                onBackClick = onBackClick,
                onEditToggle = {
                    if (isEditMode) showSaveSnack = true
                    isEditMode = !isEditMode
                }
            )
        },
        containerColor = PColors.Background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Hero avatar header ──────────────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 3 }
                ) {
                    ProfileHeroHeader(
                        isEditMode = isEditMode,
                        name = registeredName,
                        email = registeredEmail,
                        initials = initials,
                        bloodGroup = bloodGroup,
                        gender = gender
                    )
                }

                // ── Quick stats row ─────────────────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 100))
                ) {
                    QuickStatsRow(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }

                // ── Personal information card ───────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 150))
                ) {
                    PersonalInfoCard(
                        name            = registeredName,
                        email           = registeredEmail,
                        phone           = phone,
                        dateOfBirth     = dateOfBirth,
                        gender          = gender,
                        bloodGroup      = bloodGroup,
                        address         = address,
                        isEditMode      = isEditMode,
                        onPhoneChange   = { phone = it },
                        onDobChange     = { dateOfBirth = it },
                        onGenderChange  = { gender = it },
                        onBloodChange   = { bloodGroup = it },
                        onAddressChange = { address = it },
                        modifier        = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                // ── Medical information card ────────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 200))
                ) {
                    MedicalInfoCard(
                        allergies           = allergies,
                        chronicConditions   = chronicConditions,
                        isEditMode          = isEditMode,
                        onAllergiesChange   = { allergies = it },
                        onConditionsChange  = { chronicConditions = it },
                        modifier            = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }

                // ── Emergency contact card ──────────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 250))
                ) {
                    EmergencyContactCard(
                        name            = emergencyName,
                        phone           = emergencyPhone,
                        isEditMode      = isEditMode,
                        onNameChange    = { emergencyName = it },
                        onPhoneChange   = { emergencyPhone = it },
                        modifier        = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                // ── Insurance card ──────────────────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 300))
                ) {
                    InsuranceCard(
                        insuranceId     = insuranceId,
                        expiry          = insuranceExpiry,
                        isEditMode      = isEditMode,
                        onIdChange      = { insuranceId = it },
                        onExpiryChange  = { insuranceExpiry = it },
                        modifier        = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }

                // ── Quick links ─────────────────────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 350))
                ) {
                    QuickLinksCard(
                        onNavigate = onNavigate,
                        modifier   = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                // ── Log out button ──────────────────────────────────────────
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 400))
                ) {
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, PColors.Error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PColors.Error)
                    ) {
                        Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Log Out",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }

            // ── Save snackbar ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = showSaveSnack,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PColors.Success,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(
                            "Profile updated successfully!",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  TOP APP BAR
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    isEditMode: Boolean,
    onBackClick: () -> Unit,
    onEditToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(PColors.GradientStart, PColors.GradientEnd))
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "My Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        "Apollo Smart Hospital",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                // Edit / Save toggle
                Surface(
                    onClick = onEditToggle,
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Check else Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            if (isEditMode) "Save" else "Edit",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PROFILE HERO HEADER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun ProfileHeroHeader(
    isEditMode: Boolean,
    name: String,
    email: String,
    initials: String,
    bloodGroup: String,
    gender: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(PColors.GradientEnd, PColors.Background),
                    startY = 0f, endY = 600f
                )
            )
            .padding(top = 24.dp, bottom = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar with camera badge
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ProfileMockData.avatarColor, PColors.PrimaryLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 36.sp
                        )
                    )
                }
                // Camera badge
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(if (isEditMode) PColors.Primary else PColors.Surface)
                        .border(2.dp, PColors.Surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = if (isEditMode) Color.White else PColors.TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Name — LOCKED (registered field)
            Text(
                name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )

            // Patient ID chip
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Text(
                        ProfileMockData.patientId,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            // Info chips row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(
                    label      = bloodGroup,
                    icon       = Icons.Outlined.Bloodtype,
                    chipColor  = Color.White.copy(0.25f),
                    textColor  = Color.White
                )
                InfoChip(
                    label      = "${ProfileMockData.age} yrs",
                    icon       = Icons.Outlined.Cake,
                    chipColor  = Color.White.copy(0.25f),
                    textColor  = Color.White
                )
                InfoChip(
                    label      = gender,
                    icon       = Icons.Outlined.Person,
                    chipColor  = Color.White.copy(0.25f),
                    textColor  = Color.White
                )
            }

            // Locked email — shown as read-only chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    email,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.75f)
                    )
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, icon: ImageVector, chipColor: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = chipColor) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = textColor)
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = textColor, fontWeight = FontWeight.SemiBold))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  QUICK STATS ROW
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuickStatsRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ProfileStatBox("${ProfileMockData.totalVisits}", "Total Visits",  Icons.Outlined.LocalHospital, PColors.Primary,  PColors.PrimaryContainer,  Modifier.weight(1f))
        ProfileStatBox("${ProfileMockData.totalRecords}", "Records",      Icons.Outlined.FolderOpen,    PColors.Secondary, PColors.SecondaryContainer, Modifier.weight(1f))
        ProfileStatBox(ProfileMockData.totalPaid, "Amount Paid",          Icons.Outlined.Receipt,       PColors.Success,   PColors.SuccessContainer,   Modifier.weight(1f))
    }
}

@Composable
private fun ProfileStatBox(
    value: String, label: String,
    icon: ImageVector, color: Color, bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PColors.Surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(bgColor),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp)) }
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = PColors.OnSurface))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = PColors.TextSecondary), textAlign = TextAlign.Center)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PERSONAL INFORMATION CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PersonalInfoCard(
    name: String, email: String, phone: String, dateOfBirth: String, gender: String,
    bloodGroup: String, address: String, isEditMode: Boolean,
    onPhoneChange: (String) -> Unit, onDobChange: (String) -> Unit,
    onGenderChange: (String) -> Unit, onBloodChange: (String) -> Unit,
    onAddressChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon    = Icons.Outlined.Person,
            title   = "Personal Information",
            iconBg  = PColors.PrimaryContainer,
            iconTint= PColors.Primary
        )

        Spacer(Modifier.height(14.dp))

        // Locked fields — name & email
        LockedInfoRow(icon = Icons.Outlined.AccountCircle, label = "Full Name",          value = name)
        FieldDivider()
        LockedInfoRow(icon = Icons.Outlined.Email,         label = "Registered Email",   value = email)
        FieldDivider()
        LockedInfoRow(icon = Icons.Outlined.Badge,         label = "Patient ID",         value = ProfileMockData.patientId)

        FieldDivider()

        // Editable fields
        EditableInfoRow(icon = Icons.Outlined.Phone,       label = "Phone Number",       value = phone,       isEditMode = isEditMode, onValueChange = onPhoneChange)
        FieldDivider()
        EditableInfoRow(icon = Icons.Outlined.Cake,        label = "Date of Birth",      value = dateOfBirth, isEditMode = isEditMode, onValueChange = onDobChange)
        FieldDivider()
        EditableInfoRow(icon = Icons.Outlined.Person,      label = "Gender",             value = gender,      isEditMode = isEditMode, onValueChange = onGenderChange)
        FieldDivider()
        EditableInfoRow(icon = Icons.Outlined.Bloodtype,   label = "Blood Group",        value = bloodGroup,  isEditMode = isEditMode, onValueChange = onBloodChange)
        FieldDivider()
        EditableInfoRow(icon = Icons.Outlined.LocationOn,  label = "Address",            value = address,     isEditMode = isEditMode, onValueChange = onAddressChange, singleLine = false)
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  MEDICAL INFORMATION CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun MedicalInfoCard(
    allergies: String, chronicConditions: String, isEditMode: Boolean,
    onAllergiesChange: (String) -> Unit, onConditionsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon    = Icons.Outlined.HealthAndSafety,
            title   = "Medical Information",
            iconBg  = PColors.ErrorContainer,
            iconTint= PColors.Error
        )
        Spacer(Modifier.height(14.dp))
        EditableInfoRow(icon = Icons.Outlined.Warning,        label = "Known Allergies",      value = allergies,         isEditMode = isEditMode, onValueChange = onAllergiesChange,  singleLine = false)
        FieldDivider()
        EditableInfoRow(icon = Icons.Outlined.MonitorHeart,   label = "Chronic Conditions",   value = chronicConditions, isEditMode = isEditMode, onValueChange = onConditionsChange, singleLine = false)
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  EMERGENCY CONTACT CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmergencyContactCard(
    name: String, phone: String, isEditMode: Boolean,
    onNameChange: (String) -> Unit, onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier, borderColor = PColors.Error.copy(alpha = 0.25f)) {
        CardSectionHeader(
            icon    = Icons.Outlined.EmergencyShare,
            title   = "Emergency Contact",
            iconBg  = PColors.ErrorContainer,
            iconTint= PColors.Error
        )
        Spacer(Modifier.height(14.dp))
        EditableInfoRow(icon = Icons.Outlined.Person, label = "Contact Name",  value = name,  isEditMode = isEditMode, onValueChange = onNameChange)
        FieldDivider()
        EditableInfoRow(icon = Icons.Outlined.Phone,  label = "Phone Number",  value = phone, isEditMode = isEditMode, onValueChange = onPhoneChange)
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  INSURANCE CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun InsuranceCard(
    insuranceId: String, expiry: String, isEditMode: Boolean,
    onIdChange: (String) -> Unit, onExpiryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier, borderColor = PColors.Secondary.copy(alpha = 0.3f)) {
        CardSectionHeader(
            icon    = Icons.Outlined.HealthAndSafety,
            title   = "Insurance Details",
            iconBg  = PColors.SecondaryContainer,
            iconTint= PColors.Secondary
        )
        Spacer(Modifier.height(14.dp))
        EditableInfoRow(icon = Icons.Outlined.Shield,         label = "Policy / Insurance ID",  value = insuranceId, isEditMode = isEditMode, onValueChange = onIdChange)
        FieldDivider()
        EditableInfoRow(icon = Icons.Outlined.CalendarMonth,  label = "Valid Until",            value = expiry,      isEditMode = isEditMode, onValueChange = onExpiryChange)
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  QUICK LINKS CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuickLinksCard(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon    = Icons.Outlined.GridView,
            title   = "Quick Links",
            iconBg  = PColors.PrimaryContainer,
            iconTint= PColors.Primary
        )
        Spacer(Modifier.height(8.dp))

        val links = listOf(
            Triple(Icons.Outlined.Notifications, "Notifications",   "notifications"),
            Triple(Icons.Outlined.FolderOpen,    "Medical Records", "medical_records"),
            Triple(Icons.Outlined.Receipt,       "Payments",        "payments"),
            Triple(Icons.Outlined.QueueMusic,    "Queue Tracking",  "queue_tracking"),
            Triple(Icons.Outlined.Settings,      "Settings",        "settings"),
        )

        links.forEachIndexed { index, (icon, label, route) ->
            Surface(
                onClick = { onNavigate(route) },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PColors.SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = PColors.Primary, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = PColors.OnSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = PColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            if (index < links.lastIndex) {
                HorizontalDivider(color = PColors.Outline.copy(alpha = 0.5f))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  LOGOUT DIALOG
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun LogoutDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PColors.Surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PColors.ErrorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, tint = PColors.Error, modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text(
                "Log Out",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PColors.OnSurface
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Are you sure you want to log out of your Apollo Smart Hospital account?",
                style = MaterialTheme.typography.bodySmall.copy(color = PColors.TextSecondary),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PColors.Error)
            ) {
                Text("Log Out", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PColors.Outline)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge.copy(color = PColors.TextSecondary))
            }
        }
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  FIELD ROW COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

/** Read-only row for name, email, patient ID — these can never be edited. */
@Composable
private fun LockedInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PColors.SurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(PColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PColors.Primary, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = PColors.TextSecondary))
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = PColors.OnSurface))
        }
        Icon(Icons.Outlined.Lock, contentDescription = "Locked", tint = PColors.Outline, modifier = Modifier.size(14.dp))
    }
}

/** Editable row — shows plain text in view mode, `OutlinedTextField` in edit mode. */
@Composable
private fun EditableInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEditMode: Boolean,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = if (!singleLine) 10.dp else 0.dp)
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    if (isEditMode) PColors.PrimaryContainer else PColors.SurfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PColors.Primary, modifier = Modifier.size(16.dp))
        }

        AnimatedContent(
            targetState = isEditMode,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "field_$label",
            modifier = Modifier.weight(1f)
        ) { editing ->
            if (editing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    singleLine = singleLine,
                    maxLines = if (singleLine) 1 else 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PColors.Primary,
                        unfocusedBorderColor = PColors.Outline,
                        focusedContainerColor   = PColors.Surface,
                        unfocusedContainerColor = PColors.Surface
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
            } else {
                Column {
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(color = PColors.TextSecondary))
                    Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = PColors.OnSurface))
                }
            }
        }
    }
}

@Composable
private fun FieldDivider() {
    HorizontalDivider(color = PColors.Outline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
}

// ════════════════════════════════════════════════════════════════════════════
//  SHARED UTILITY COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(1.5.dp, borderColor, RoundedCornerShape(24.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun CardSectionHeader(icon: ImageVector, title: String, iconBg: Color, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PColors.OnSurface))
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true, name = "Profile Screen")
@Composable
private fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen(onBackClick = {}, onNavigate = {})
    }
}
