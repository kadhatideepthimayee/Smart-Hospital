package com.example.smarthospitalqueue.ui.screens.queue

// ════════════════════════════════════════════════════════════════════════════
//  QueueTrackingScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Self-contained mock data · Aligned with AppointmentConfirmationScreen.kt
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

// ─── Color Palette (shared with AppointmentConfirmationScreen) ───────────────

private object QColors {
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
    val Error             = Color(0xFFC62828)
    val ErrorContainer    = Color(0xFFFFEBEE)
    val GradientStart     = Color(0xFF0B3D91)
    val GradientEnd       = Color(0xFF1976D2)
    val AiAccent          = Color(0xFF7C4DFF)
    val AiContainer       = Color(0xFFF3EEFF)
    val TokenBlue         = Color(0xFF1E3A8A)
}

// ─── Mock Data (aligned with BookAppointmentScreen + ConfirmationScreen) ─────

private object QueueMockData {
    // Appointment / patient
    val currentUser = FirebaseAuth.getInstance().currentUser
    val patientName = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")
        ?: "Patient"
    val patientId       = "APOL-PAT-20241182"
    val tokenNumber     = "T-052"

    // Doctor from BookAppointmentScreen mockDoctors[0]
    val doctorName      = "Dr. Arjun Mehta"
    val specialization  = "Senior Cardiologist"
    val department      = "Cardiology"
    val roomNumber      = "Room 204, Floor 2"
    val avatarInitials  = "AM"
    val avatarColor     = Color(0xFF1565C0)

    // Queue state
    val currentServing  = 46          // token number currently with doctor
    val patientTokenNum = 52          // T-052
    val peopleAhead     = 6
    val totalInQueue    = 18
    val estimatedWait   = 28          // minutes
    val avgPerPatient   = 5           // minutes per patient (for timeline)

    // AI prediction
    val aiWaitMin       = 24
    val aiWaitMax       = 30
    val bestArrival     = "9:45 AM"
    val queueTrend      = "Moderate"
    val confidence      = 91

    // Appointment
    val appointmentTime = "10:00 AM"
    val appointmentDate = "Tue, 3 Jun 2025"

    // Emergency notice
    val emergencyActive = true
    val emergencyNote   = "Dr. Arjun Mehta is briefly attending an emergency. +5–10 min possible."
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueTrackingScreen(
    onBackClick: () -> Unit = {},
    onNavigateToAIPrediction: () -> Unit = {}
) {
    // Staggered reveal phases
    var phase by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(150); phase = 1
        delay(250); phase = 2
        delay(200); phase = 3
        delay(200); phase = 4
        delay(200); phase = 5
        delay(200); phase = 6
    }

    // Live dot blink
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOut), RepeatMode.Reverse),
        label = "live_dot"
    )

    Scaffold(
        topBar = {
            QueueTopBar(
                onBackClick = onBackClick,
                onRefresh = {},
                onAiClick = onNavigateToAIPrediction
            )
        },
        containerColor = QColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Live indicator strip ────────────────────────────────────────
            LiveIndicatorStrip(liveDotAlpha = liveDotAlpha)

            // ── Main queue status hero card ─────────────────────────────────
            RevealSection(visible = phase >= 1) {
                QueueStatusHeroCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Emergency banner ────────────────────────────────────────────
            if (QueueMockData.emergencyActive) {
                RevealSection(visible = phase >= 2) {
                    EmergencyDelayBanner(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Three stat boxes ────────────────────────────────────────────
            RevealSection(visible = phase >= 2) {
                QueueStatRow(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Doctor info card ────────────────────────────────────────────
            RevealSection(visible = phase >= 3) {
                DoctorInfoCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Position timeline ───────────────────────────────────────────
            RevealSection(visible = phase >= 4) {
                PositionTimelineCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── AI prediction strip ─────────────────────────────────────────
            RevealSection(visible = phase >= 5) {
                AiPredictionStrip(
                    onTap = onNavigateToAIPrediction,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Appointment reminder ────────────────────────────────────────
            RevealSection(visible = phase >= 6) {
                AppointmentReminderCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
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
private fun QueueTopBar(
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onAiClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(QColors.GradientStart, QColors.GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Live Queue Tracking",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        "Apollo Smart Hospital · Cardiology",
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
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
                IconButton(onClick = onAiClick) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Prediction",
                        tint = Color(0xFFCE93D8)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  LIVE INDICATOR STRIP
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun LiveIndicatorStrip(liveDotAlpha: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(QColors.SuccessContainer)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(QColors.Success.copy(alpha = liveDotAlpha))
            )
            Text(
                "LIVE QUEUE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = QColors.Success,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
        }
        Text(
            "Last updated: just now",
            style = MaterialTheme.typography.labelSmall.copy(
                color = QColors.Success.copy(alpha = 0.75f)
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  QUEUE STATUS HERO CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun QueueStatusHeroCard(modifier: Modifier = Modifier) {
    val progress = remember {
        1f - (QueueMockData.peopleAhead.toFloat() / QueueMockData.totalInQueue)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1400, easing = EaseOutCubic),
        label = "queue_progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = QColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(x = 150.dp, y = (-70).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(x = (-30).dp, y = 80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Token comparison row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TokenDisplay(
                        label = "NOW SERVING",
                        token = "T-0${QueueMockData.currentServing}"
                    )

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(80.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    TokenDisplay(
                        label = "YOUR TOKEN",
                        token = QueueMockData.tokenNumber,
                        isHighlighted = true
                    )
                }

                // Progress bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${(animatedProgress * 100).toInt()}% queue complete",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                        Text(
                            "${QueueMockData.peopleAhead} ahead of you",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }

                // Wait time row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Estimated Wait Time",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                    Text(
                        "~${QueueMockData.estimatedWait} min",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenDisplay(label: String, token: String, isHighlighted: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp
            )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            token,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (isHighlighted) Color(0xFFFFE082) else Color.White,
                fontSize = 42.sp
            )
        )
        if (isHighlighted) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFFE082).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFFFFE082).copy(alpha = 0.5f))
            ) {
                Text(
                    "YOU",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  EMERGENCY DELAY BANNER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmergencyDelayBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(QColors.ErrorContainer)
            .border(1.dp, QColors.Error.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(QColors.Error.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = QColors.Error,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Emergency Delay Notice",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = QColors.Error
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                QueueMockData.emergencyNote,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF7B1F1F)
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  THREE STAT BOXES
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun QueueStatRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QueueStatBox(
            icon = Icons.Outlined.People,
            value = "${QueueMockData.totalInQueue}",
            label = "In Queue",
            iconColor = QColors.Primary,
            iconBg = QColors.PrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        QueueStatBox(
            icon = Icons.Outlined.SkipNext,
            value = "${QueueMockData.peopleAhead}",
            label = "Ahead of You",
            iconColor = QColors.Secondary,
            iconBg = QColors.SecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        QueueStatBox(
            icon = Icons.Outlined.Timer,
            value = "~${QueueMockData.estimatedWait}m",
            label = "Est. Wait",
            iconColor = QColors.Warning,
            iconBg = QColors.WarningContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QueueStatBox(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = QColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = QColors.OnSurface
                )
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = QColors.TextSecondary
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  DOCTOR INFO CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun DoctorInfoCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon = Icons.Outlined.LocalHospital,
            title = "Your Doctor",
            iconBg = QColors.PrimaryContainer,
            iconTint = QColors.Primary
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                QueueMockData.avatarColor,
                                QueueMockData.avatarColor.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    QueueMockData.avatarInitials,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    QueueMockData.doctorName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = QColors.OnSurface
                    )
                )
                Text(
                    QueueMockData.specialization,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = QColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.MeetingRoom,
                        contentDescription = null,
                        tint = QColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        QueueMockData.roomNumber,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = QColors.TextSecondary
                        )
                    )
                }
            }

            // Status pill
            Surface(
                shape = RoundedCornerShape(50),
                color = QColors.SuccessContainer,
                border = BorderStroke(1.dp, QColors.Success.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(QColors.Success)
                    )
                    Text(
                        "On Duty",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = QColors.Success,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Navigation hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(QColors.SurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Directions,
                contentDescription = null,
                tint = QColors.Primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Main Entrance → Elevator → Floor 2 → Turn Left → ${QueueMockData.roomNumber}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = QColors.TextSecondary
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  POSITION TIMELINE CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PositionTimelineCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon = Icons.Outlined.FormatListNumbered,
            title = "Queue Position",
            iconBg = QColors.PrimaryContainer,
            iconTint = QColors.Primary
        )

        Spacer(Modifier.height(16.dp))

        // Build visible tokens: currentServing to patient's token (show up to 6)
        val base = QueueMockData.currentServing
        val patientNum = QueueMockData.patientTokenNum
        val visibleCount = minOf(QueueMockData.peopleAhead + 2, 7)
        val tokens = (0 until visibleCount).map { i -> base + i }

        tokens.forEachIndexed { index, tokenNum ->
            val isCurrent = index == 0
            val isPatient = tokenNum == patientNum
            val isPast = tokenNum < base
            val token = "T-0$tokenNum"

            TimelineRow(
                token = token,
                isCurrent = isCurrent,
                isPatient = isPatient,
                position = index
            )

            if (index < tokens.lastIndex) {
                // Connecting line
                Row {
                    Spacer(Modifier.width(17.dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(18.dp)
                            .background(
                                if (isCurrent)
                                    Brush.verticalGradient(listOf(QColors.Success, QColors.Outline))
                                else
                                    Brush.verticalGradient(listOf(QColors.Outline, QColors.Outline))
                            )
                    )
                }
            }
        }

        if (QueueMockData.peopleAhead > 5) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.padding(start = 50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = null,
                    tint = QColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "+ ${QueueMockData.peopleAhead - 5} more patients ahead",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = QColors.TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(
    token: String,
    isCurrent: Boolean,
    isPatient: Boolean,
    position: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> Brush.linearGradient(listOf(QColors.Success, QColors.SuccessLight))
                        isPatient -> Brush.linearGradient(listOf(QColors.Primary, QColors.PrimaryLight))
                        else -> Brush.linearGradient(listOf(QColors.SurfaceVariant, QColors.SurfaceVariant))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    token.takeLast(3),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isPatient) Color.White else QColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                )
            }
        }

        // Label
        Column(modifier = Modifier.weight(1f)) {
            Text(
                when {
                    isCurrent -> "$token — With Doctor Now"
                    isPatient -> "$token — You"
                    else -> "$token — Waiting"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isCurrent || isPatient) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrent -> QColors.Success
                        isPatient -> QColors.Primary
                        else -> QColors.OnSurface
                    }
                )
            )
            if (!isCurrent && !isPatient) {
                Text(
                    "~${position * QueueMockData.avgPerPatient} min wait",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = QColors.TextSecondary
                    )
                )
            }
        }

        // Badge
        when {
            isCurrent -> StatusBadge("In Room", QColors.SuccessContainer, QColors.Success)
            isPatient -> StatusBadge("You", QColors.PrimaryContainer, QColors.Primary)
            else -> {}
        }
    }
}

@Composable
private fun StatusBadge(text: String, bg: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  AI PREDICTION STRIP
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiPredictionStrip(onTap: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onTap,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = QColors.AiContainer,
        border = BorderStroke(1.dp, QColors.AiAccent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(QColors.AiAccent, Color(0xFF5E35B1))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "AI Queue Prediction",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = QColors.AiAccent
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Based on current flow, your actual wait is ${QueueMockData.aiWaitMin}–${QueueMockData.aiWaitMax} min · ${QueueMockData.confidence}% confidence",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = QColors.OnSurface.copy(alpha = 0.75f)
                    )
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = QColors.AiAccent
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  APPOINTMENT REMINDER CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AppointmentReminderCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier, borderColor = QColors.Primary.copy(alpha = 0.2f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(QColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.EventNote,
                    contentDescription = null,
                    tint = QColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Your Appointment",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = QColors.TextSecondary
                    )
                )
                Text(
                    "${QueueMockData.appointmentDate} · ${QueueMockData.appointmentTime}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = QColors.OnSurface
                    )
                )
                Text(
                    "Arrive by ${QueueMockData.bestArrival} — recommended by AI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = QColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Patient info row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(QColors.SurfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = QColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        QueueMockData.patientName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = QColors.OnSurface
                        )
                    )
                    Text(
                        QueueMockData.patientId,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = QColors.TextSecondary
                        )
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = QColors.PrimaryContainer
            ) {
                Text(
                    QueueMockData.tokenNumber,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = QColors.Primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
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
        colors = CardDefaults.cardColors(containerColor = QColors.Surface),
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
                color = QColors.OnSurface
            )
        )
    }
}

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

@Preview(showBackground = true, showSystemUi = true, name = "Queue Tracking Screen")
@Composable
private fun QueueTrackingScreenPreview() {
    MaterialTheme {
        QueueTrackingScreen(
            onBackClick = {},
            onNavigateToAIPrediction = {}
        )
    }
}
