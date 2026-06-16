package com.example.smarthospitalqueue.ui.screens.queue

// ════════════════════════════════════════════════════════════════════════════
//  DigitalTokenScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Self-contained mock data · Aligned with all preceding screens
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.smarthospitalqueue.data.BookingRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

// ─── Color Palette (shared across all screens) ────────────────────────────────

private object TokenColors {
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
    val GradientStart     = Color(0xFF0B3D91)
    val GradientEnd       = Color(0xFF1976D2)
    val AiAccent          = Color(0xFF7C4DFF)
    val Gold              = Color(0xFFFFC107)
    val GoldLight         = Color(0xFFFFE082)
}

// ─── Mock Data (aligned with all preceding screens) ───────────────────────────

private object TokenMockData {
    // Patient — consistent across all screens
    val patientName     = "Arjun Raghavan"
    val patientId       = "APOL-PAT-20241182"

    // Token — from BookAppointmentScreen selection
    val tokenNumber     = "T-052"
    val appointmentId   = "APT-2024-1182"
    val tokenStatus     = "ACTIVE"

    // Doctor — Dr. Arjun Mehta, Cardiology
    val doctorName      = "Dr. Arjun Mehta"
    val specialization  = "Senior Cardiologist"
    val department      = "Cardiology"
    val roomNumber      = "Room 204, Floor 2"
    val hospital        = "Apollo Smart Hospital"

    // Slot — from BookAppointmentScreen 10:00 AM selection
    val appointmentDate = "Tuesday, 3 June 2025"
    val appointmentTime = "10:00 AM"
    val consultationFee = "₹800"

    // Queue state
    val currentServing  = 46
    val queuePosition   = 6
    val peopleAhead     = 6
    val estimatedWait   = 28       // minutes

    // Instructions
    val instructions = listOf(
        Icons.Outlined.Badge         to "Present this digital token at the reception desk on arrival.",
        Icons.Outlined.Fingerprint   to "Carry a valid government-issued ID proof for verification.",
        Icons.Outlined.AlarmOn       to "Arrive at least 10 minutes before your scheduled time slot.",
        Icons.Outlined.EventBusy     to "This token is valid only for the booked date and time.",
        Icons.Outlined.Phone         to "For queries, contact the hospital at +91-44-2345-6789.",
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalTokenScreen(
    onBackClick: () -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val registeredPatientName = currentUser?.displayName 
        ?: currentUser?.email?.substringBefore("@") 
        ?: TokenMockData.patientName

    val doctorName = if (BookingRepository.hasActiveBooking) BookingRepository.bookedDoctorName else TokenMockData.doctorName
    val specialization = if (BookingRepository.hasActiveBooking) BookingRepository.bookedSpecialization else TokenMockData.specialization
    val department = if (BookingRepository.hasActiveBooking) BookingRepository.bookedDepartment else TokenMockData.department
    val hospital = if (BookingRepository.hasActiveBooking) BookingRepository.bookedHospital else TokenMockData.hospital
    val appointmentDate = if (BookingRepository.hasActiveBooking) BookingRepository.bookedDate else TokenMockData.appointmentDate
    val appointmentTime = if (BookingRepository.hasActiveBooking) BookingRepository.bookedTime else TokenMockData.appointmentTime
    val consultationFee = if (BookingRepository.hasActiveBooking) BookingRepository.bookedFee else TokenMockData.consultationFee
    val tokenNumber = if (BookingRepository.hasActiveBooking) BookingRepository.bookedToken else TokenMockData.tokenNumber

    // Staggered reveal — identical pattern to all screens in the project
    var phase by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(150); phase = 1
        delay(300); phase = 2
        delay(250); phase = 3
        delay(200); phase = 4
        delay(200); phase = 5
    }

    Scaffold(
        topBar = { DigitalTokenTopBar(department = department, onBackClick = onBackClick) },
        containerColor = TokenColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Main token card (hero) ──────────────────────────────────────
            RevealSection(visible = phase >= 1) {
                MainTokenCard(
                    tokenNumber = tokenNumber,
                    patientName = registeredPatientName,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            // ── Queue status row ────────────────────────────────────────────
            RevealSection(visible = phase >= 2) {
                QueueStatusRow(
                    tokenNumber = tokenNumber,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // ── Appointment details card ────────────────────────────────────
            RevealSection(visible = phase >= 3) {
                AppointmentDetailsCard(
                    patientName = registeredPatientName,
                    doctorName = doctorName,
                    specialization = specialization,
                    department = department,
                    appointmentDate = appointmentDate,
                    appointmentTime = appointmentTime,
                    consultationFee = consultationFee,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            // ── Navigation guide ────────────────────────────────────────────
            RevealSection(visible = phase >= 4) {
                NavigationGuideCard(
                    department = department,
                    roomNumber = if (BookingRepository.hasActiveBooking) "Room 204, Floor 2" else TokenMockData.roomNumber,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // ── Instructions card ───────────────────────────────────────────
            RevealSection(visible = phase >= 5) {
                InstructionsCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            // ── Action buttons ──────────────────────────────────────────────
            RevealSection(visible = phase >= 5) {
                ActionButtonsRow(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  TOP APP BAR
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DigitalTokenTopBar(department: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(TokenColors.GradientStart, TokenColors.GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Digital Token",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        "Apollo Smart Hospital · $department",
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
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Download, contentDescription = "Download", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  MAIN TOKEN CARD (Hero)
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun MainTokenCard(tokenNumber: String, patientName: String, modifier: Modifier = Modifier) {
    // Animated pulsing border
    val infiniteTransition = rememberInfiniteTransition(label = "token_border")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "border_alpha"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = TokenColors.Primary.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = TokenColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Hospital header ─────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Logo circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(TokenColors.GradientStart, TokenColors.GradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.LocalHospital,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    TokenMockData.hospital.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TokenColors.TextSecondary,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    "DIGITAL APPOINTMENT TOKEN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TokenColors.Primary,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(16.dp))
            DashedDivider()
            Spacer(Modifier.height(16.dp))

            // ── Token number ────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "YOUR TOKEN NUMBER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TokenColors.TextSecondary,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    tokenNumber,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = TokenColors.Primary,
                        fontSize = 80.sp,
                        letterSpacing = 2.sp
                    )
                )

                // Status pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = TokenColors.SuccessContainer,
                    border = BorderStroke(1.dp, TokenColors.Success.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(TokenColors.Success)
                        )
                        Text(
                            TokenMockData.tokenStatus,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TokenColors.Success,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── QR Code placeholder ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(glowScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TokenColors.SurfaceVariant)
                    .border(
                        1.5.dp,
                        TokenColors.Primary.copy(alpha = 0.25f),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Corner decorations
                QrCornerDecoration(Alignment.TopStart)
                QrCornerDecoration(Alignment.TopEnd)
                QrCornerDecoration(Alignment.BottomStart)
                QrCornerDecoration(Alignment.BottomEnd)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = TokenColors.Primary
                    )
                    Text(
                        "Scan at Reception",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TokenColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            DashedDivider()
            Spacer(Modifier.height(14.dp))

            // ── Bottom meta row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "PATIENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TokenColors.TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        patientName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TokenColors.OnSurface
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "APPOINTMENT ID",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TokenColors.TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        TokenMockData.appointmentId,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TokenColors.Primary
                        )
                    )
                }
            }
        }
    }
}

// Decorative QR corner bracket
@Composable
private fun BoxScope.QrCornerDecoration(alignment: Alignment) {
    val size = 18.dp
    val thickness = 3.dp
    Box(
        modifier = Modifier
            .align(alignment)
            .padding(10.dp)
            .size(size)
    ) {
        // Horizontal line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .clip(RoundedCornerShape(1.dp))
                .background(TokenColors.Primary.copy(alpha = 0.5f))
                .align(
                    if (alignment == Alignment.TopStart || alignment == Alignment.TopEnd)
                        Alignment.TopStart else Alignment.BottomStart
                )
        )
        // Vertical line
        Box(
            modifier = Modifier
                .width(thickness)
                .fillMaxHeight()
                .clip(RoundedCornerShape(1.dp))
                .background(TokenColors.Primary.copy(alpha = 0.5f))
                .align(
                    if (alignment == Alignment.TopStart || alignment == Alignment.BottomStart)
                        Alignment.TopStart else Alignment.TopEnd
                )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  QUEUE STATUS ROW — FOUR CHIPS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun QueueStatusRow(tokenNumber: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QueueChip(
            label = "Now Serving",
            value = "T-0${TokenMockData.currentServing}",
            valueColor = TokenColors.Primary,
            bgColor = TokenColors.PrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        QueueChip(
            label = "Your Token",
            value = tokenNumber,
            valueColor = TokenColors.Success,
            bgColor = TokenColors.SuccessContainer,
            modifier = Modifier.weight(1f)
        )
        QueueChip(
            label = "Ahead",
            value = "${TokenMockData.peopleAhead}",
            valueColor = TokenColors.Secondary,
            bgColor = TokenColors.SecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        QueueChip(
            label = "Est. Wait",
            value = "~${TokenMockData.estimatedWait}m",
            valueColor = TokenColors.Warning,
            bgColor = TokenColors.WarningContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QueueChip(
    label: String,
    value: String,
    valueColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, valueColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = valueColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = valueColor.copy(alpha = 0.75f),
                    fontSize = 9.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  APPOINTMENT DETAILS CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AppointmentDetailsCard(
    patientName: String,
    doctorName: String,
    specialization: String,
    department: String,
    appointmentDate: String,
    appointmentTime: String,
    consultationFee: String,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon = Icons.Outlined.EventNote,
            title = "Appointment Details",
            iconBg = TokenColors.PrimaryContainer,
            iconTint = TokenColors.Primary
        )

        Spacer(Modifier.height(16.dp))

        val rows = listOf(
            Triple(Icons.Outlined.Person,          "Patient",      patientName),
            Triple(Icons.Outlined.Badge,            "Patient ID",   TokenMockData.patientId),
            Triple(Icons.Outlined.LocalHospital,    "Doctor",       doctorName),
            Triple(Icons.Outlined.MedicalServices,  "Specialization",specialization),
            Triple(Icons.Outlined.Business,         "Department",   department),
            Triple(Icons.Outlined.MeetingRoom,      "Room",         TokenMockData.roomNumber),
            Triple(Icons.Outlined.CalendarMonth,    "Date",         appointmentDate),
            Triple(Icons.Outlined.Schedule,         "Time",         appointmentTime),
            Triple(Icons.Outlined.CurrencyRupee,    "Fee",          consultationFee),
        )

        rows.forEachIndexed { index, (icon, label, value) ->
            TokenDetailRow(icon = icon, label = label, value = value)
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = TokenColors.Outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TokenDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(TokenColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TokenColors.Primary, modifier = Modifier.size(16.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(color = TokenColors.TextSecondary),
            modifier = Modifier.width(96.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = TokenColors.OnSurface
            ),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  NAVIGATION GUIDE CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun NavigationGuideCard(department: String, roomNumber: String, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier, borderColor = TokenColors.Secondary.copy(alpha = 0.3f)) {
        CardSectionHeader(
            icon = Icons.Outlined.Directions,
            title = "How to Reach Your Doctor",
            iconBg = TokenColors.SecondaryContainer,
            iconTint = TokenColors.Secondary
        )

        Spacer(Modifier.height(16.dp))

        val steps = listOf(
            "Enter through the Main Entrance (Gate A)",
            "Take the elevator or stairs to Floor 2",
            "Turn left into the $department Wing",
            "Proceed to $roomNumber waiting area",
        )

        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Numbered circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == steps.lastIndex)
                                Brush.linearGradient(listOf(TokenColors.Success, Color(0xFF4CAF50)))
                            else
                                Brush.linearGradient(listOf(TokenColors.GradientStart, TokenColors.GradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index == steps.lastIndex) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Text(
                    step,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (index == steps.lastIndex) TokenColors.Success else TokenColors.OnSurface,
                        fontWeight = if (index == steps.lastIndex) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (index < steps.lastIndex) {
                Row {
                    Spacer(Modifier.width(13.dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(14.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(TokenColors.Primary.copy(alpha = 0.5f), TokenColors.Outline)
                                )
                            )
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  INSTRUCTIONS CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun InstructionsCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon = Icons.Outlined.Info,
            title = "Important Instructions",
            iconBg = TokenColors.WarningContainer,
            iconTint = TokenColors.Warning
        )

        Spacer(Modifier.height(16.dp))

        TokenMockData.instructions.forEachIndexed { index, (icon, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(TokenColors.WarningContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = TokenColors.Warning, modifier = Modifier.size(16.dp))
                }
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TokenColors.OnSurface,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            if (index < TokenMockData.instructions.lastIndex) {
                HorizontalDivider(
                    color = TokenColors.Outline.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 44.dp, top = 2.dp, bottom = 2.dp)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  ACTION BUTTONS ROW
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun ActionButtonsRow(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Primary: Share
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TokenColors.Primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Share Token",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Secondary: Download
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, TokenColors.Primary)
        ) {
            Icon(
                Icons.Outlined.Download,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = TokenColors.Primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Download Appointment Slip",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TokenColors.Primary
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SHARED UTILITY COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

/** Dashed horizontal divider — gives the token card a boarding-pass feel. */
@Composable
private fun DashedDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left notch circle
        Box(
            modifier = Modifier
                .size(16.dp)
                .offset(x = (-8).dp)
                .clip(CircleShape)
                .background(TokenColors.Background)
        )
        // Dashed line (simulated with repeated small boxes)
        for (i in 0..28) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (i % 2 == 0) TokenColors.Outline else Color.Transparent
                    )
            )
        }
        // Right notch circle
        Box(
            modifier = Modifier
                .size(16.dp)
                .offset(x = 8.dp)
                .clip(CircleShape)
                .background(TokenColors.Background)
        )
    }
}

/** Premium elevated card with optional accent border. */
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
        colors = CardDefaults.cardColors(containerColor = TokenColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

/** Consistent icon + title header used across cards. */
@Composable
private fun CardSectionHeader(
    icon: ImageVector,
    title: String,
    iconBg: Color,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TokenColors.OnSurface
            )
        )
    }
}

/** Staggered content reveal — same pattern as all other screens. */
@Composable
private fun RevealSection(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
    ) {
        content()
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true, name = "Digital Token Screen")
@Composable
private fun DigitalTokenScreenPreview() {
    MaterialTheme {
        DigitalTokenScreen(onBackClick = {})
    }
}