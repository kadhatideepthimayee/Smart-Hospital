package com.example.smarthospitalqueue.ui.screens.appointment

// ════════════════════════════════════════════════════════════════════════════
//  AppointmentConfirmationScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Firestore-backed: real data loaded via AppointmentConfirmationViewModel
// ════════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.google.firebase.auth.FirebaseAuth
import com.example.smarthospitalqueue.ui.viewmodel.AppointmentConfirmationViewModel
import kotlinx.coroutines.delay

// ─── Mock / Fallback Data ────────────────────────────────────────────────────

private object ConfirmationMockData {
    val currentUser     = FirebaseAuth.getInstance().currentUser
    val patientName     = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")
        ?: "Patient"
    val patientId       = "APOL-PAT-20241182"

    val doctorName      = "Dr. Arjun Mehta"
    val specialization  = "Senior Cardiologist"
    val department      = "Cardiology"
    val hospital        = "Apollo Smart Hospital"
    val roomNumber      = "Room 204, Floor 2"
    val consultationFee = "₹800"

    val appointmentDate = "Tuesday, 3 June 2025"
    val appointmentTime = "10:00 AM"

    val tokenNumber     = "T-052"
    val queuePosition   = 6
    val currentServing  = 46
    val estimatedWait   = 28

    val predictedWait   = "26–30 min"
    val bestArrival     = "9:45 AM"
    val queueTrend      = "Moderate"
    val confidence      = 91

    val appointmentId   = "APT-2024-1182"
}

// ─── Color Palette ───────────────────────────────────────────────────────────

private object ConfColors {
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
    val SuccessLight      = Color(0xFF4CAF50)
    val SuccessContainer  = Color(0xFFE8F5E9)
    val Warning           = Color(0xFFF57F17)
    val WarningContainer  = Color(0xFFFFF8E1)
    val GradientStart     = Color(0xFF0B3D91)
    val GradientEnd       = Color(0xFF1976D2)
    val AiAccent          = Color(0xFF7C4DFF)
    val AiContainer       = Color(0xFFF3EEFF)
    val StarGold          = Color(0xFFFFC107)
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentConfirmationScreen(
    doctorName: String      = ConfirmationMockData.doctorName,
    specialization: String  = ConfirmationMockData.specialization,
    department: String      = ConfirmationMockData.department,
    hospital: String        = ConfirmationMockData.hospital,
    appointmentDate: String = ConfirmationMockData.appointmentDate,
    appointmentTime: String = ConfirmationMockData.appointmentTime,
    consultationFee: String = ConfirmationMockData.consultationFee,
    onBackToDashboard: () -> Unit = {},
    onNavigate: (String) -> Unit  = {},
    viewModel: AppointmentConfirmationViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // ── Collect Firestore state ───────────────────────────────────────────
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLatestAppointment()
    }

    // ── Resolve display values: Firestore first, passed params as fallback ─
    val displayDoctorName     = uiState.appointment?.doctorName     ?: doctorName
    val displaySpecialization = uiState.appointment?.specialization ?: specialization
    val displayDepartment     = uiState.appointment?.department     ?: department
    val displayHospital       = uiState.appointment?.hospital       ?: hospital
    val displayDate           = uiState.appointment?.date           ?: appointmentDate
    val displayTime           = uiState.appointment?.time           ?: appointmentTime
    val displayFee            = uiState.appointment?.consultationFee
        ?.let { "₹$it" } ?: consultationFee
    val displayToken          = uiState.appointment?.tokenNumber
        ?: ConfirmationMockData.tokenNumber
    val displayPosition       = uiState.appointment?.queuePosition
        ?: ConfirmationMockData.queuePosition
    val displayPatientName    = uiState.appointment?.patientName
        ?: ConfirmationMockData.patientName
    val displayPatientId      = uiState.appointment?.patientId
        ?: ConfirmationMockData.patientId
    val displayAppointmentId  = uiState.appointment?.appointmentId
        ?: ConfirmationMockData.appointmentId

    // ── Staggered reveal animation ────────────────────────────────────────
    var phase by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(200); phase = 1
        delay(300); phase = 2
        delay(250); phase = 3
        delay(200); phase = 4
        delay(200); phase = 5
        delay(200); phase = 6
    }

    Scaffold(
        topBar          = { ConfirmationTopBar(onBack = onBackToDashboard) },
        containerColor  = ConfColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Loading indicator ─────────────────────────────────────────
            if (uiState.isLoading) {
                Box(
                    modifier        = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ConfColors.Primary)
                }
            }

            // ── Error banner ──────────────────────────────────────────────
            uiState.error?.let { errorMsg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint               = Color(0xFFB71C1C),
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        "Could not load latest data. Showing cached info.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB71C1C)
                        )
                    )
                }
            }

            // ── Hero header ───────────────────────────────────────────────
            HeroGradientHeader(
                visible       = phase >= 1,
                appointmentId = displayAppointmentId
            )

            Spacer(Modifier.height(16.dp))

            // ── Appointment details ───────────────────────────────────────
            RevealSection(visible = phase >= 2, delayMs = 0) {
                AppointmentDetailsCard(
                    doctorName      = displayDoctorName,
                    specialization  = displaySpecialization,
                    department      = displayDepartment,
                    hospital        = displayHospital,
                    appointmentDate = displayDate,
                    appointmentTime = displayTime,
                    consultationFee = displayFee,
                    patientName     = displayPatientName,
                    patientId       = displayPatientId,
                    modifier        = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Digital token card ────────────────────────────────────────
            RevealSection(visible = phase >= 3, delayMs = 0) {
                DigitalTokenCard(
                    tokenNumber   = displayToken,
                    queuePosition = displayPosition,
                    appointmentId = displayAppointmentId,
                    modifier      = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── AI Prediction ─────────────────────────────────────────────
            RevealSection(visible = phase >= 4, delayMs = 0) {
                AiPredictionCard(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Success timeline ──────────────────────────────────────────
            RevealSection(visible = phase >= 5, delayMs = 0) {
                SuccessTimeline(
                    doctorName    = displayDoctorName,
                    tokenNumber   = displayToken,
                    queuePosition = displayPosition,
                    modifier      = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Action buttons ────────────────────────────────────────────
            RevealSection(visible = phase >= 6, delayMs = 0) {
                ActionButtonsSection(
                    onNavigate        = onNavigate,
                    onBackToDashboard = onBackToDashboard,
                    modifier          = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}   // ← end AppointmentConfirmationScreen

// ════════════════════════════════════════════════════════════════════════════
//  TOP APP BAR
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(ConfColors.GradientStart, ConfColors.GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Booking Confirmed",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            fontSize   = 20.sp
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
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  HERO GRADIENT HEADER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeroGradientHeader(visible: Boolean, appointmentId: String) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(ConfColors.GradientEnd, ConfColors.Background),
                        startY = 0f, endY = 600f
                    )
                )
                .padding(top = 28.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PulsingSuccessIcon()

                Text(
                    "Appointment Confirmed Successfully",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    ),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 24.dp)
                )

                Text(
                    "Your appointment has been scheduled and your\ndigital token has been generated.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 32.dp)
                )

                // Appointment ID chip — now uses real appointmentId
                Surface(
                    shape  = RoundedCornerShape(50),
                    color  = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier            = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ConfirmationNumber,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            appointmentId,   // ← real Firestore value
                            style = MaterialTheme.typography.labelMedium.copy(
                                color        = Color.White,
                                fontWeight   = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingSuccessIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val outerScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue  = 1.1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "outer_pulse"
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(100.dp).scale(outerScale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        )
        Box(
            modifier = Modifier
                .size(80.dp).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp).clip(CircleShape)
                    .background(ConfColors.Success),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint     = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  APPOINTMENT DETAILS CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AppointmentDetailsCard(
    doctorName: String,
    specialization: String,
    department: String,
    hospital: String,
    appointmentDate: String,
    appointmentTime: String,
    consultationFee: String,
    patientName: String = ConfirmationMockData.patientName,
    patientId: String   = ConfirmationMockData.patientId,
    modifier: Modifier  = Modifier
) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon    = Icons.Outlined.EventNote,
            title   = "Appointment Details",
            iconBg  = ConfColors.PrimaryContainer,
            iconTint = ConfColors.Primary
        )

        Spacer(Modifier.height(16.dp))

        // Status pill
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(
                shape  = RoundedCornerShape(50),
                color  = ConfColors.SuccessContainer,
                border = BorderStroke(1.dp, ConfColors.Success.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp).clip(CircleShape)
                            .background(ConfColors.Success)
                    )
                    Text(
                        "Confirmed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color      = ConfColors.Success,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Detail rows — all real values
        val details = listOf(
            Triple(Icons.Outlined.Person,          "Patient Name",   patientName),
            Triple(Icons.Outlined.Badge,           "Patient ID",     patientId),
            Triple(Icons.Outlined.LocalHospital,   "Doctor",         doctorName),
            Triple(Icons.Outlined.MedicalServices, "Specialization", specialization),
            Triple(Icons.Outlined.Business,        "Department",     department),
            Triple(Icons.Outlined.AccountBalance,  "Hospital",       hospital),
            Triple(Icons.Outlined.CalendarMonth,   "Date",           appointmentDate),
            Triple(Icons.Outlined.Schedule,        "Time",           appointmentTime),
            Triple(Icons.Outlined.MeetingRoom,     "Room",           ConfirmationMockData.roomNumber),
            Triple(Icons.Outlined.CurrencyRupee,   "Fee",            consultationFee)
        )

        details.forEach { (icon, label, value) ->
            DetailRow(icon = icon, label = label, value = value)
            if (details.last().second != label) {
                HorizontalDivider(
                    color    = ConfColors.Outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp).clip(RoundedCornerShape(9.dp))
                .background(ConfColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ConfColors.Primary, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = ConfColors.TextSecondary))
            Text(
                value,
                style    = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color      = ConfColors.OnSurface
                ),
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  DIGITAL TOKEN CARD  — fully wired to real Firestore values
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun DigitalTokenCard(
    tokenNumber: String   = ConfirmationMockData.tokenNumber,
    queuePosition: Int    = ConfirmationMockData.queuePosition,
    appointmentId: String = ConfirmationMockData.appointmentId,
    modifier: Modifier    = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = ConfColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(240.dp).offset(x = 140.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .size(160.dp).offset(x = (-40).dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            Column(
                modifier            = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.QrCode2, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(22.dp))
                        Text(
                            "Digital Token",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White, fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                        Text(
                            "ACTIVE",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelSmall.copy(
                                color         = Color.White,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                // Large token number — REAL value
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "YOUR TOKEN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color         = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 3.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tokenNumber,    // ← REAL Firestore token
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight    = FontWeight.ExtraBold,
                            color         = Color.White,
                            fontSize      = 72.sp,
                            letterSpacing = 2.sp
                        )
                    )
                }

                // Stat boxes — REAL queue position, mock for now serving & wait
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TokenStatBox(
                        label    = "Your Position",
                        value    = "#$queuePosition",   // ← REAL
                        modifier = Modifier.weight(1f)
                    )
                    TokenStatBox(
                        label    = "Now Serving",
                        value    = "T-0${ConfirmationMockData.currentServing}",
                        modifier = Modifier.weight(1f)
                    )
                    TokenStatBox(
                        label    = "Est. Wait",
                        value    = "${ConfirmationMockData.estimatedWait} min",
                        modifier = Modifier.weight(1f)
                    )
                }

                // QR row — REAL appointmentId
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.QrCode, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Scan at Reception",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White, fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            "Show this token to the front desk or\nscan at the self-check-in kiosk.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                        Text(
                            appointmentId,   // ← REAL Firestore appointmentId
                            style = MaterialTheme.typography.labelSmall.copy(
                                color         = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold, color = Color.White))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.7f)), textAlign = TextAlign.Center)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  AI PREDICTION CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiPredictionCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier, borderColor = ConfColors.AiAccent.copy(alpha = 0.35f)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp).clip(RoundedCornerShape(11.dp))
                        .background(Brush.linearGradient(listOf(ConfColors.AiAccent, Color(0xFF5E35B1)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("AI Queue Prediction", style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold, color = ConfColors.OnSurface))
                    Text("Powered by predictive analytics", style = MaterialTheme.typography.labelSmall.copy(
                        color = ConfColors.AiAccent))
                }
            }
            Surface(
                shape  = RoundedCornerShape(50),
                color  = ConfColors.AiContainer,
                border = BorderStroke(1.dp, ConfColors.AiAccent.copy(alpha = 0.4f))
            ) {
                Text(
                    "${ConfirmationMockData.confidence}% Confidence",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style    = MaterialTheme.typography.labelSmall.copy(
                        color = ConfColors.AiAccent, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AiMetricCard(Icons.Outlined.Timer,       "Predicted Wait", ConfirmationMockData.predictedWait, ConfColors.Primary,  Modifier.weight(1f))
            AiMetricCard(Icons.Outlined.AlarmOn,     "Best Arrival",   ConfirmationMockData.bestArrival,   ConfColors.Success,  Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AiMetricCard(Icons.Outlined.TrendingUp,  "Queue Trend",    ConfirmationMockData.queueTrend,    ConfColors.Warning,  Modifier.weight(1f))
            AiMetricCard(Icons.Outlined.VerifiedUser,"Accuracy",       "${ConfirmationMockData.confidence}%", ConfColors.AiAccent, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ConfColors.WarningContainer)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Lightbulb, contentDescription = null,
                tint = ConfColors.Warning, modifier = Modifier.size(18.dp))
            Text(
                "AI recommends arriving by ${ConfirmationMockData.bestArrival} to avoid peak queue hours.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF5D4037), fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun AiMetricCard(
    icon: ImageVector, label: String, value: String,
    iconColor: Color, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ConfColors.SurfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold, color = ConfColors.OnSurface))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = ConfColors.TextSecondary))
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SUCCESS TIMELINE — fully wired to real values
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun SuccessTimeline(
    doctorName: String,
    tokenNumber: String = ConfirmationMockData.tokenNumber,
    queuePosition: Int  = ConfirmationMockData.queuePosition,
    modifier: Modifier  = Modifier
) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon     = Icons.Outlined.Timeline,
            title    = "Booking Progress",
            iconBg   = ConfColors.SuccessContainer,
            iconTint = ConfColors.Success
        )

        Spacer(Modifier.height(20.dp))

        val steps = listOf(
            "Appointment Booked" to "Your slot has been reserved with $doctorName.",
            "Token Generated"    to "Digital token $tokenNumber issued successfully.",   // ← REAL
            "Queue Assigned"     to "You are placed at position #$queuePosition in the queue.", // ← REAL
            "Ready For Visit"    to "Please arrive by ${ConfirmationMockData.bestArrival}. Have a smooth visit!"
        )

        steps.forEachIndexed { index, (title, desc) ->
            TimelineStep(
                step        = index + 1,
                title       = title,
                description = desc,
                isLast      = index == steps.lastIndex
            )
        }
    }
}

@Composable
private fun TimelineStep(step: Int, title: String, description: String, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ConfColors.Success, ConfColors.SuccessLight))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(17.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp).height(42.dp)
                        .background(Brush.verticalGradient(
                            listOf(ConfColors.Success.copy(alpha = 0.6f), ConfColors.Outline)
                        ))
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 14.dp else 0.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold, color = ConfColors.OnSurface))
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodySmall.copy(
                color = ConfColors.TextSecondary))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  ACTION BUTTONS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun ActionButtonsSection(
    onNavigate: (String) -> Unit,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick   = { onNavigate("queue_tracking") },
            modifier  = Modifier.fillMaxWidth().height(56.dp),
            shape     = RoundedCornerShape(16.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = ConfColors.Primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Outlined.TrackChanges, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Track Live Queue",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }

        Button(
            onClick   = { onNavigate("digital_token") },
            modifier  = Modifier.fillMaxWidth().height(56.dp),
            shape     = RoundedCornerShape(16.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = ConfColors.Secondary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Outlined.QrCode2, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("View Digital Token",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick  = {},
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.5.dp, ConfColors.Outline)
            ) {
                Icon(Icons.Outlined.Download, contentDescription = null,
                    modifier = Modifier.size(18.dp), tint = ConfColors.Primary)
                Spacer(Modifier.width(6.dp))
                Text("Download Slip", style = MaterialTheme.typography.labelMedium.copy(
                    color = ConfColors.Primary, fontWeight = FontWeight.SemiBold))
            }
            OutlinedButton(
                onClick  = onBackToDashboard,
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.5.dp, ConfColors.Outline)
            ) {
                Icon(Icons.Outlined.Dashboard, contentDescription = null,
                    modifier = Modifier.size(18.dp), tint = ConfColors.TextSecondary)
                Spacer(Modifier.width(6.dp))
                Text("Dashboard", style = MaterialTheme.typography.labelMedium.copy(
                    color = ConfColors.TextSecondary, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SHARED UTILITY COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumCard(
    modifier: Modifier     = Modifier,
    borderColor: Color     = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .border(
                width  = if (borderColor == Color.Transparent) 0.dp else 1.5.dp,
                color  = borderColor,
                shape  = RoundedCornerShape(24.dp)
            ),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = ConfColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun CardSectionHeader(
    icon: ImageVector,
    title: String,
    iconBg: Color,
    iconTint: Color
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp).clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(title, style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold, color = ConfColors.OnSurface))
    }
}

@Composable
private fun RevealSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400, delayMs)) + slideInVertically(tween(400, delayMs)) { it / 3 }
    ) {
        content()
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true, name = "Appointment Confirmation")
@Composable
private fun AppointmentConfirmationScreenPreview() {
    MaterialTheme {
        AppointmentConfirmationScreen(
            onBackToDashboard = {},
            onNavigate        = {}
        )
    }
}