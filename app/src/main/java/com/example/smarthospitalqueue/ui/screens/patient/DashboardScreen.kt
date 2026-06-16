package com.example.smarthospitalqueue.ui.screens.patient

// ════════════════════════════════════════════════════════════════════════════
//  DashboardScreen.kt  –  Smart Hospital Queue Management System
//  Self-contained: all mock data, models, and composables included.
//  Package: com.example.smarthospitalqueue.ui.screens.patient
// ════════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.smarthospitalqueue.data.BookingRepository
import com.google.firebase.auth.FirebaseAuth

// ── Navigation stub ────────────────────────────────────────────────────────
import androidx.navigation.NavController

// ════════════════════════════════════════════════════════════════════════════
//  MOCK DATA MODELS  (inline – no external ViewModel needed)
// ════════════════════════════════════════════════════════════════════════════

data class MockPatient(
    val name: String,
    val patientId: String,
    val hospitalName: String,
    val bloodGroup: String,
    val age: Int,
    val avatarInitials: String
)

data class MockQueueStatus(
    val isActive: Boolean,
    val tokenNumber: Int,
    val currentServing: Int,
    val totalAhead: Int,
    val estimatedWaitMinutes: Int,
    val doctorName: String,
    val department: String,
    val roomNumber: String,
    val status: String // "In Queue", "Next", "Consulting"
)

data class MockAiPrediction(
    val bestVisitTime: String,
    val queueTrend: String,
    val recommendation: String,
    val confidencePercent: Int,
    val peakHoursWarning: String
)

data class MockQuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0,
    val color: Color
)

data class MockAppointment(
    val doctorName: String,
    val avatarInitials: String,
    val avatarColor: Color,
    val specialization: String,
    val date: String,
    val time: String,
    val status: String,
    val tokenNumber: String,
    val hospital: String,
    val fee: String
)

data class MockStat(
    val value: String,
    val label: String,
    val subLabel: String,
    val icon: ImageVector,
    val color: Color,
    val trend: String,
    val trendUp: Boolean
)

data class MockHealthUpdate(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val tag: String
)

// ════════════════════════════════════════════════════════════════════════════
//  INLINE MOCK DATA
// ════════════════════════════════════════════════════════════════════════════

private val mockPatient = MockPatient(
    name = FirebaseAuth.getInstance().currentUser?.displayName ?: "Patient",
    patientId = "SHQ-2024-00847",
    hospitalName = "Apollo Smart Hospital",
    bloodGroup = "O+",
    age = 34,
    avatarInitials = (
            FirebaseAuth.getInstance().currentUser?.displayName
                ?.split(" ")
                ?.map { it.first() }
                ?.joinToString("")
                ?.take(2)
                ?: "P"
            )
)

private val mockQueue = MockQueueStatus(
    isActive = true,
    tokenNumber = 47,
    currentServing = 42,
    totalAhead = 5,
    estimatedWaitMinutes = 18,
    doctorName = "Dr. Priya Sharma",
    department = "Cardiology",
    roomNumber = "OPD-203",
    status = "In Queue"
)

private val mockAiPrediction = MockAiPrediction(
    bestVisitTime = "10:30 AM – 11:00 AM",
    queueTrend = "Decreasing",
    recommendation = "Visit in the next 20 min for shortest wait",
    confidencePercent = 92,
    peakHoursWarning = "Expect heavy footfall 2–4 PM today"
)

private val mockQuickActions = listOf(
    MockQuickAction("Book Appointment", "Find doctors", Icons.Outlined.CalendarMonth,
        "book_appointment", 0, Color(0xFF1565C0)),
    MockQuickAction("Queue Tracking", "Live status", Icons.Outlined.TrackChanges,
        "queue_tracking", 1, Color(0xFF00897B)),
    MockQuickAction("Medical Records", "History & reports", Icons.Outlined.FolderOpen,
        "medical_records", 3, Color(0xFF6A1B9A)),
    MockQuickAction("Payments", "Bills & receipts", Icons.Outlined.Payments,
        "payments", 0, Color(0xFFE65100)),
    MockQuickAction("Emergency", "SOS & helpline", Icons.Outlined.Emergency,
        "emergency", 0, Color(0xFFC62828)),
    MockQuickAction("Lab Reports", "View results", Icons.Outlined.Science,
        "lab_reports", 2, Color(0xFF558B2F))
)

private val mockAppointment = MockAppointment(
    doctorName = "Dr. Arjun Mehta",
    avatarInitials = "AM",
    avatarColor = Color(0xFF1565C0),
    specialization = "Senior Cardiologist",
    date = "Mon, 2 Jun 2025",
    time = "11:00 AM",
    status = "Confirmed",
    tokenNumber = "T-52",
    hospital = "Apollo Smart Hospital",
    fee = "₹800"
)

private val mockStats = listOf(
    MockStat("47", "Doctors on Duty", "3 on break", Icons.Outlined.LocalHospital,
        Color(0xFF1565C0), "+2 vs yesterday", true),
    MockStat("312", "Patients Served", "Today so far", Icons.Outlined.People,
        Color(0xFF00897B), "+18% vs avg", true),
    MockStat("18 min", "Avg Wait Time", "Across all OPDs", Icons.Outlined.Schedule,
        Color(0xFF6A1B9A), "-4 min vs Mon", false),
    MockStat("94%", "Queue Efficiency", "AI-optimised", Icons.Outlined.AutoGraph,
        Color(0xFFE65100), "Best this month", true)
)

private val mockHealthUpdates = listOf(
    MockHealthUpdate(
        "Flu Season Alert",
        "Influenza cases rising in Chennai. Stay hydrated and get vaccinated.",
        Icons.Outlined.HealthAndSafety,
        Color(0xFFE65100),
        "Advisory"
    ),
    MockHealthUpdate(
        "Blood Donation Camp",
        "Join our camp on 5 Jun at Apollo. All blood groups needed urgently.",
        Icons.Outlined.Bloodtype,
        Color(0xFFC62828),
        "Event"
    ),
    MockHealthUpdate(
        "Free Health Checkup",
        "Complete body checkup for ₹299. Valid till 30 Jun 2025.",
        Icons.Outlined.MedicalServices,
        Color(0xFF00897B),
        "Offer"
    )
)

// ════════════════════════════════════════════════════════════════════════════
//  COLOR PALETTE
// ════════════════════════════════════════════════════════════════════════════

private object DC {
    val Primary       = Color(0xFF0B3D91)
    val PrimaryLight  = Color(0xFF1565C0)
    val PrimaryBg     = Color(0xFFDCE8FF)
    val Secondary     = Color(0xFF00897B)
    val SecondaryBg   = Color(0xFFB2DFDB)
    val Background    = Color(0xFFF2F5FF)
    val Surface       = Color(0xFFFFFFFF)
    val SurfaceVar    = Color(0xFFF0F4FF)
    val OnSurface     = Color(0xFF0D1B3E)
    val TextSec       = Color(0xFF5A6A8A)
    val Outline       = Color(0xFFCDD5F0)
    val Success       = Color(0xFF2E7D32)
    val SuccessBg     = Color(0xFFE8F5E9)
    val Warning       = Color(0xFFF57F17)
    val WarningBg     = Color(0xFFFFF8E1)
    val Error         = Color(0xFFC62828)
    val ErrorBg       = Color(0xFFFFEBEE)
    val Gold          = Color(0xFFFFC107)
    val GradStart     = Color(0xFF0B3D91)
    val GradMid       = Color(0xFF1565C0)
    val GradEnd       = Color(0xFF1976D2)
    val CardShadow    = Color(0x1A0B3D91)
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController? = null,
    onNavigate: (String) -> Unit = {}
) {
    var selectedBottomNav by remember { mutableIntStateOf(0) }
    var notificationCount by remember { mutableIntStateOf(3) }

    val currentQueue = if (BookingRepository.hasActiveBooking) {
        MockQueueStatus(
            isActive = true,
            tokenNumber = BookingRepository.bookedToken.removePrefix("T-").toIntOrNull() ?: 47,
            currentServing = (BookingRepository.bookedToken.removePrefix("T-").toIntOrNull() ?: 47) - 5,
            totalAhead = 5,
            estimatedWaitMinutes = 18,
            doctorName = BookingRepository.bookedDoctorName,
            department = BookingRepository.bookedDepartment,
            roomNumber = "OPD-203",
            status = "In Queue"
        )
    } else {
        mockQueue
    }

    val currentAppointment = if (BookingRepository.hasActiveBooking) {
        MockAppointment(
            doctorName = BookingRepository.bookedDoctorName,
            avatarInitials = BookingRepository.bookedDoctorName.split(" ").filter { it.isNotEmpty() }.let {
                if (it.size >= 2) "${it[0].first()}${it[1].first()}" else if (it.isNotEmpty()) "${it[0].first()}" else "DR"
            },
            avatarColor = Color(0xFF1565C0),
            specialization = BookingRepository.bookedSpecialization,
            date = BookingRepository.bookedDate,
            time = BookingRepository.bookedTime,
            status = "Confirmed",
            tokenNumber = BookingRepository.bookedToken,
            hospital = BookingRepository.bookedHospital,
            fee = BookingRepository.bookedFee
        )
    } else {
        mockAppointment
    }

    Scaffold(
        containerColor = DC.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("ai_chat") },
                containerColor = DC.Primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "AI Chat",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        bottomBar = {
            DashboardBottomNav(
                selected = selectedBottomNav,
                onSelect = { idx ->
                    selectedBottomNav = idx
                    when (idx) {
                        1 -> onNavigate("book_appointment")
                        2 -> onNavigate("queue_tracking")
                        3 -> onNavigate("medical_records")
                        4 -> onNavigate("profile")
                    }
                }
            )
        }
    ) { paddings ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── 1. HERO HEADER ────────────────────────────────────────────
            item {
                HeroHeader(
                    patient = mockPatient,
                    notificationCount = notificationCount,
                    onNotificationsClick = {
                        notificationCount = 0
                        onNavigate("notifications")
                    },
                    onProfileClick = { onNavigate("profile") }
                )
            }

            // ── 2. LIVE QUEUE CARD ────────────────────────────────────────
            item {
                SectionLabel(
                    title = "Live Queue Status",
                    actionLabel = "Track Live",
                    onAction = { onNavigate("queue_tracking") },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item {
                LiveQueueCard(
                    queue = currentQueue,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 3. AI PREDICTION CARD ─────────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                AiPredictionCard(
                    prediction = mockAiPrediction,
                    onClick = { onNavigate("ai_prediction") },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 4. QUICK ACTIONS ──────────────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionLabel(
                    title = "Quick Actions",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item {
                QuickActionsGrid(
                    actions = mockQuickActions,
                    onNavigate = onNavigate,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 5. UPCOMING APPOINTMENT ───────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionLabel(
                    title = "Upcoming Appointment",
                    actionLabel = "View All",
                    onAction = { onNavigate("appointments") },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item {
                UpcomingAppointmentCard(
                    appt = currentAppointment,
                    onReschedule = { onNavigate("book_appointment") },
                    onViewToken = { onNavigate("digital_token") },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 6. HOSPITAL STATS ─────────────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionLabel(
                    title = "Hospital Today",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item {
                HospitalStatsGrid(
                    stats = mockStats,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 7. HEALTH UPDATES ─────────────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionLabel(
                    title = "Health Updates",
                    actionLabel = "See All",
                    onAction = { onNavigate("health_updates") },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            items(mockHealthUpdates) { update ->
                HealthUpdateCard(
                    update = update,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 1 – HERO HEADER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeroHeader(
    patient: MockPatient,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        // Gradient background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(DC.GradStart, DC.GradMid, DC.GradEnd),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
            // Decorative circles
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.85f, -size.height * 0.2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.width * 0.4f,
                center = Offset(-size.width * 0.1f, size.height * 1.1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: hospital + icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocalHospital,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = patient.hospitalName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Notification bell
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFFF5252),
                                    contentColor = Color.White
                                ) {
                                    Text(notificationCount.toString(),
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Bottom row: Avatar + Patient info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular avatar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .clickable(onClick = onProfileClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = patient.avatarInitials,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                    }
                    Column {
                        Text(
                            text = "Good Morning 👋",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                        Text(
                            text = patient.name.split(" ").take(2).joinToString(" "),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "ID: ${patient.patientId}  •  ${patient.bloodGroup}  •  ${patient.age}y",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Health pulse chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF69F0AE))
                        )
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 2 – LIVE QUEUE CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun LiveQueueCard(queue: MockQueueStatus, modifier: Modifier = Modifier) {
    val progress = remember(queue.currentServing, queue.tokenNumber) {
        if (queue.tokenNumber > 0)
            (queue.currentServing.toFloat() / queue.tokenNumber.toFloat()).coerceIn(0f, 1f)
        else 0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "queue_progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DC.PrimaryBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ConfirmationNumber, null,
                            tint = DC.Primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Your Token", style = MaterialTheme.typography.labelSmall.copy(
                            color = DC.TextSec))
                        Text(
                            text = "# ${queue.tokenNumber}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DC.Primary,
                                fontSize = 26.sp
                            )
                        )
                    }
                }

                // Status chip
                val (chipColor, chipBg) = when (queue.status) {
                    "Next" -> DC.Success to DC.SuccessBg
                    "Consulting" -> DC.Warning to DC.WarningBg
                    else -> DC.Primary to DC.PrimaryBg
                }
                Surface(shape = RoundedCornerShape(50), color = chipBg) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(chipColor))
                        Text(queue.status, style = MaterialTheme.typography.labelMedium.copy(
                            color = chipColor, fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Now Serving: ${queue.currentServing}",
                        style = MaterialTheme.typography.labelSmall.copy(color = DC.TextSec))
                    Text("Your Token: ${queue.tokenNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DC.Primary, fontWeight = FontWeight.SemiBold))
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DC.PrimaryBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(DC.PrimaryLight, DC.Secondary)
                                )
                            )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QueueStat(label = "People Ahead", value = "${queue.totalAhead}", icon = Icons.Outlined.PeopleAlt)
                QueueStatDivider()
                QueueStat(label = "Est. Wait", value = "${queue.estimatedWaitMinutes} min", icon = Icons.Outlined.Timer)
                QueueStatDivider()
                QueueStat(label = "Room", value = queue.roomNumber, icon = Icons.Outlined.MeetingRoom)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = DC.Outline.copy(alpha = 0.5f)
            )

            // Doctor info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DC.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PS", style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White, fontWeight = FontWeight.Bold))
                    }
                    Column {
                        Text(queue.doctorName, style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold, color = DC.OnSurface))
                        Text(queue.department, style = MaterialTheme.typography.labelSmall.copy(
                            color = DC.TextSec))
                    }
                }

                // Cancel / Track button
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DC.Outline),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.MyLocation, null,
                        modifier = Modifier.size(14.dp), tint = DC.Primary)
                    Spacer(Modifier.width(4.dp))
                    Text("Track", style = MaterialTheme.typography.labelSmall.copy(
                        color = DC.Primary, fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
private fun QueueStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = DC.Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold, color = DC.OnSurface))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(
            color = DC.TextSec, fontSize = 10.sp))
    }
}

@Composable
private fun QueueStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(DC.Outline)
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 3 – AI PREDICTION CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiPredictionCard(
    prediction: MockAiPrediction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box {
            // Gradient accent strip on left
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF00897B), Color(0xFF26C6DA))
                        )
                    )
            )
            Column(modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF00897B), Color(0xFF26C6DA))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, null,
                                tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("AI Smart Prediction",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold, color = DC.OnSurface))
                            Text("${prediction.confidencePercent}% confidence",
                                style = MaterialTheme.typography.labelSmall.copy(color = DC.TextSec))
                        }
                    }
                    Icon(Icons.Outlined.ChevronRight, null,
                        tint = DC.TextSec, modifier = Modifier.size(18.dp))
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DC.SurfaceVar)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AiStat(label = "Best Visit", value = "10:30 AM", icon = Icons.Outlined.WbSunny,
                        color = DC.Warning)
                    AiStat(label = "Trend", value = prediction.queueTrend, icon = Icons.Outlined.TrendingDown,
                        color = DC.Success)
                    AiStat(label = "Avg Wait", value = "18 min", icon = Icons.Outlined.Timer,
                        color = DC.Primary)
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE0F7FA))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Lightbulb, null,
                        tint = Color(0xFF00838F), modifier = Modifier.size(16.dp))
                    Text(prediction.recommendation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF00838F), fontWeight = FontWeight.Medium))
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DC.WarningBg)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Warning, null,
                        tint = DC.Warning, modifier = Modifier.size(14.dp))
                    Text(prediction.peakHoursWarning,
                        style = MaterialTheme.typography.labelSmall.copy(color = DC.Warning))
                }
            }
        }
    }
}

@Composable
private fun RowScope.AiStat(label: String, value: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold, color = DC.OnSurface))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(
            color = DC.TextSec, fontSize = 10.sp))
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 4 – QUICK ACTIONS GRID
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuickActionsGrid(
    actions: List<MockQuickAction>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = actions.chunked(3)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { action ->
                    QuickActionTile(
                        action = action,
                        onClick = { onNavigate(action.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining slots
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    action: MockQuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmergency = action.route == "emergency"
    BadgedBox(
        badge = {
            if (action.badgeCount > 0) {
                Badge(containerColor = Color(0xFFFF5252), contentColor = Color.White) {
                    Text(action.badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        modifier = modifier
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = if (isEmergency) DC.ErrorBg else DC.Surface,
            border = if (isEmergency) BorderStroke(1.5.dp, DC.Error.copy(alpha = 0.4f))
            else BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(action.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(action.icon, null,
                        tint = action.color,
                        modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = action.title.split(" ").first(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DC.OnSurface
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DC.TextSec,
                        fontSize = 9.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 5 – UPCOMING APPOINTMENT CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun UpcomingAppointmentCard(
    appt: MockAppointment,
    onReschedule: () -> Unit,
    onViewToken: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Doctor avatar
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(appt.avatarColor, appt.avatarColor.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(appt.avatarInitials, style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp))
                    }
                    Column {
                        Text(appt.doctorName, style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold, color = DC.OnSurface))
                        Text(appt.specialization, style = MaterialTheme.typography.bodySmall.copy(
                            color = DC.Primary, fontWeight = FontWeight.Medium))
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(50), color = DC.SuccessBg) {
                            Text(appt.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DC.Success, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }

                // Token badge
                Column(horizontalAlignment = Alignment.End) {
                    Surface(shape = RoundedCornerShape(10.dp), color = DC.PrimaryBg) {
                        Text(appt.tokenNumber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DC.Primary, fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(appt.fee, style = MaterialTheme.typography.labelSmall.copy(
                        color = DC.TextSec))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Date/Time/Hospital chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DC.SurfaceVar)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AppointmentDetail(Icons.Outlined.CalendarMonth, "Date", appt.date)
                Box(Modifier.width(1.dp).height(32.dp).background(DC.Outline))
                AppointmentDetail(Icons.Outlined.Schedule, "Time", appt.time)
                Box(Modifier.width(1.dp).height(32.dp).background(DC.Outline))
                AppointmentDetail(Icons.Outlined.LocationOn, "Hospital", "Apollo")
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReschedule,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DC.Outline)
                ) {
                    Icon(Icons.Outlined.EditCalendar, null,
                        modifier = Modifier.size(16.dp), tint = DC.Primary)
                    Spacer(Modifier.width(6.dp))
                    Text("Reschedule", style = MaterialTheme.typography.labelMedium.copy(
                        color = DC.Primary, fontWeight = FontWeight.SemiBold))
                }
                Button(
                    onClick = onViewToken,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)
                ) {
                    Icon(Icons.Outlined.QrCode2, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Token", style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
private fun AppointmentDetail(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = DC.Primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(3.dp))
        Text(value, style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold, color = DC.OnSurface), textAlign = TextAlign.Center)
        Text(label, style = MaterialTheme.typography.labelSmall.copy(
            color = DC.TextSec, fontSize = 9.sp))
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 6 – HOSPITAL STATS GRID
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun HospitalStatsGrid(stats: List<MockStat>, modifier: Modifier = Modifier) {
    val rows = stats.chunked(2)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { stat ->
                    StatTile(stat = stat, modifier = Modifier.weight(1f))
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(stat: MockStat, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(stat.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(stat.icon, null, tint = stat.color, modifier = Modifier.size(18.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        if (stat.trendUp) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        null,
                        tint = if (stat.trendUp) DC.Success else DC.Error,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(stat.value, style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold, color = DC.OnSurface, fontSize = 22.sp))
            Text(stat.label, style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold, color = DC.OnSurface))
            Text(stat.subLabel, style = MaterialTheme.typography.labelSmall.copy(
                color = DC.TextSec, fontSize = 10.sp))
            Spacer(Modifier.height(6.dp))
            Text(stat.trend, style = MaterialTheme.typography.labelSmall.copy(
                color = if (stat.trendUp) DC.Success else DC.Error, fontWeight = FontWeight.Medium,
                fontSize = 10.sp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 7 – HEALTH UPDATE CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun HealthUpdateCard(update: MockHealthUpdate, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(update.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(update.icon, null, tint = update.color, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(update.title, style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold, color = DC.OnSurface))
                    Surface(shape = RoundedCornerShape(50), color = update.color.copy(alpha = 0.1f)) {
                        Text(update.tag, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = update.color, fontWeight = FontWeight.SemiBold, fontSize = 9.sp))
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(update.description, style = MaterialTheme.typography.bodySmall.copy(
                    color = DC.TextSec), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Icon(Icons.Outlined.ChevronRight, null,
                tint = DC.TextSec, modifier = Modifier.size(18.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION
// ════════════════════════════════════════════════════════════════════════════

private data class NavItem(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val navItems = listOf(
    NavItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
    NavItem("Book", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    NavItem("Queue", Icons.Outlined.TrackChanges, Icons.Filled.TrackChanges),
    NavItem("Records", Icons.Outlined.FolderOpen, Icons.Filled.Folder),
    NavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person)
)

@Composable
private fun DashboardBottomNav(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(
        containerColor = DC.Surface,
        contentColor = DC.Primary,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = DC.CardShadow,
                spotColor = DC.CardShadow
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        navItems.forEachIndexed { index, item ->
            val isSelected = index == selected

            // Emergency tab special styling
            val isQueue = index == 2

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(index) },
                icon = {
                    if (isQueue) {
                        // Floating centre button effect
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(DC.GradStart, DC.GradEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isSelected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DC.Primary,
                    selectedTextColor = DC.Primary,
                    unselectedIconColor = DC.TextSec,
                    unselectedTextColor = DC.TextSec,
                    indicatorColor = DC.PrimaryBg
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SHARED UTILITY COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DC.OnSurface,
                fontSize = 15.sp
            )
        )
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DC.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Icon(Icons.Default.ChevronRight, null,
                    tint = DC.Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}