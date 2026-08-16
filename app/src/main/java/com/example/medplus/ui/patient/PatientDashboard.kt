package com.example.medplus.ui.patient

// ════════════════════════════════════════════════════════════════════════════
//  PatientDashboardScreen.kt  –  MedPlus Smart Hospital Queue Management
//  Stateless screen: takes PatientDashboardUiState + callbacks.
//  Visual design (colors, gradient, cards, spacing, rounded corners,
//  typography, bottom nav) preserved from the original reference UI.
//  Removed: blood group, age, AI prediction, hospital-wide stats,
//  health-updates feed, and all other data not backed by the real
//  PatientDashboardUiState.
// ════════════════════════════════════════════════════════════════════════════

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
import com.example.medplus.dashboard.model.ActivityItem
import com.example.medplus.dashboard.model.ActivityType
import com.example.medplus.dashboard.model.CrowdLevel
import com.example.medplus.dashboard.model.HealthSummary
import com.example.medplus.dashboard.model.LiveQueueInfo
import com.example.medplus.dashboard.model.PatientDashboardUiState
import com.example.medplus.dashboard.model.UpcomingAppointment

// ════════════════════════════════════════════════════════════════════════════
//  COLOR PALETTE  (unchanged from original design)
// ════════════════════════════════════════════════════════════════════════════

private object DC {
    val Primary       = Color(0xFF0B3D91)
    val PrimaryLight  = Color(0xFF1565C0)
    val PrimaryBg     = Color(0xFFDCE8FF)
    val Secondary     = Color(0xFF00897B)
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
fun PatientDashboardScreen(
    uiState: PatientDashboardUiState,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBookAppointmentClick: () -> Unit,
    onMyAppointmentsClick: () -> Unit,
    onQueueStatusClick: () -> Unit,
    onMedicalRecordsClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onMedicineReminderClick: () -> Unit,
    onViewAppointmentDetailsClick: (appointmentId: String) -> Unit,
    onRescheduleAppointmentClick: (appointmentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBottomNav by remember { mutableIntStateOf(0) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(DC.Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DC.Primary)
        }
        return
    }

    if (uiState.errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(DC.Background),
            contentAlignment = Alignment.Center
        ) {
            Text(text = uiState.errorMessage, color = DC.Error)
        }
        return
    }

    Scaffold(
        modifier = modifier,
        containerColor = DC.Background,
        bottomBar = {
            DashboardBottomNav(
                selected = selectedBottomNav,
                onSelect = { idx ->
                    selectedBottomNav = idx
                    when (idx) {
                        0 -> {} // Home – already here
                        1 -> onBookAppointmentClick()
                        2 -> onQueueStatusClick()
                        3 -> onMedicalRecordsClick()
                        4 -> onProfileClick()
                    }
                }
            )
        }
    ) { paddings ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddings),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── 1. HERO HEADER ────────────────────────────────────────────
            item {
                HeroHeader(
                    patientName = uiState.patientName,
                    profileImageUrl = uiState.profileImageUrl,
                    notificationCount = uiState.unreadNotificationCount,
                    onNotificationsClick = onNotificationClick,
                    onProfileClick = onProfileClick
                )
            }

            // ── 2. LIVE QUEUE ──────────────────────────────────────────────
            item {
                SectionLabel(
                    title = "Live Queue Status",
                    actionLabel = "Track Live",
                    onAction = onQueueStatusClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item {
                LiveQueueCard(
                    queue = uiState.liveQueue,
                    onTrackClick = onQueueStatusClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 3. QUICK ACTIONS ──────────────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionLabel(
                    title = "Quick Actions",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item {
                QuickActionsGrid(
                    onBookAppointmentClick = onBookAppointmentClick,
                    onMyAppointmentsClick = onMyAppointmentsClick,
                    onQueueStatusClick = onQueueStatusClick,
                    onMedicalRecordsClick = onMedicalRecordsClick,
                    onPrescriptionsClick = onPrescriptionsClick,
                    onMedicineReminderClick = onMedicineReminderClick,
                    onProfileClick = onProfileClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 4. UPCOMING APPOINTMENT ────────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionLabel(
                    title = "Upcoming Appointment",
                    actionLabel = "My Appointments",
                    onAction = onMyAppointmentsClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            item {
                UpcomingAppointmentSection(
                    appointment = uiState.upcomingAppointment,
                    onViewDetails = onViewAppointmentDetailsClick,
                    onReschedule = onRescheduleAppointmentClick,
                    onBookAppointmentClick = onBookAppointmentClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── 5. HEALTH SUMMARY (only if there's real data) ─────────────
            if (uiState.healthSummary?.hasData == true) {
                item { Spacer(Modifier.height(20.dp)) }
                item {
                    SectionLabel(
                        title = "Health Summary",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                item {
                    HealthSummaryCard(
                        summary = uiState.healthSummary,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // ── 6. RECENT ACTIVITY ─────────────────────────────────────────
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionLabel(
                    title = "Recent Activity",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            if (uiState.recentActivity.isNotEmpty()) {
                items(uiState.recentActivity) { activity ->
                    ActivityRow(
                        activity = activity,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                    )
                }
            } else {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.History,
                        message = "No recent activity yet.",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 1 – HERO HEADER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeroHeader(
    patientName: String,
    profileImageUrl: String?,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val initials = remember(patientName) {
        patientName.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "P" }
    }

    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(DC.GradStart, DC.GradMid, DC.GradEnd),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: branding + notification bell
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
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocalHospital, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "MedPlus",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = Color(0xFFFF5252), contentColor = Color.White) {
                                Text(
                                    if (notificationCount > 9) "9+" else notificationCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.Outlined.Notifications, contentDescription = "Notifications",
                            tint = Color.White, modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom row: avatar + greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    // NOTE: swap for AsyncImage(profileImageUrl) once an image
                    // loading library (e.g. Coil) is wired in. Falls back to
                    // initials when there is no profileImageUrl.
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp
                        )
                    )
                }
                Column {
                    Text(
                        text = "Good day 👋",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    )
                    Text(
                        text = patientName.ifBlank { "Patient" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp
                        )
                    )
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
private fun LiveQueueCard(
    queue: LiveQueueInfo?,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (queue == null || !queue.isActive) {
        EmptyStateCard(
            icon = Icons.Outlined.ConfirmationNumber,
            title = "No Active Queue",
            message = "Book an appointment to track your queue position in real-time.",
            modifier = modifier
        )
        return
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                            .background(DC.PrimaryBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ConfirmationNumber, null,
                            tint = DC.Primary, modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "Your Token",
                            style = MaterialTheme.typography.labelSmall.copy(color = DC.TextSec)
                        )
                        Text(
                            text = "# ${queue.queueNumber}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold, color = DC.Primary, fontSize = 26.sp
                            )
                        )
                    }
                }

                val (chipColor, chipBg) = when (queue.status) {
                    "Next" -> DC.Success to DC.SuccessBg
                    "In Progress" -> DC.Warning to DC.WarningBg
                    else -> DC.Primary to DC.PrimaryBg
                }
                Surface(shape = RoundedCornerShape(50), color = chipBg) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(chipColor))
                        Text(
                            queue.status,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = chipColor, fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QueueStat(
                    label = "People Ahead", value = "${queue.patientsAhead}",
                    icon = Icons.Outlined.PeopleAlt
                )
                QueueStatDivider()
                QueueStat(
                    label = "Est. Wait", value = "${queue.estimatedWaitMinutes} min",
                    icon = Icons.Outlined.Timer
                )
                if (queue.department != null) {
                    QueueStatDivider()
                    QueueStat(
                        label = "Department", value = queue.department,
                        icon = Icons.Outlined.LocalHospital
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onTrackClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DC.Outline)
            ) {
                Icon(
                    Icons.Outlined.MyLocation, null,
                    modifier = Modifier.size(16.dp), tint = DC.Primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Track Live",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DC.Primary, fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun QueueStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = DC.Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold, color = DC.OnSurface
            )
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(color = DC.TextSec, fontSize = 10.sp)
        )
    }
}

@Composable
private fun QueueStatDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(DC.Outline))
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 3 – QUICK ACTIONS
// ════════════════════════════════════════════════════════════════════════════

private data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionsGrid(
    onBookAppointmentClick: () -> Unit,
    onMyAppointmentsClick: () -> Unit,
    onQueueStatusClick: () -> Unit,
    onMedicalRecordsClick: () -> Unit,
    onPrescriptionsClick: () -> Unit,
    onMedicineReminderClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickAction("Book", "Appointment", Icons.Outlined.CalendarMonth, Color(0xFF1565C0), onBookAppointmentClick),
        QuickAction("Queue", "Live status", Icons.Outlined.TrackChanges, Color(0xFF00897B), onQueueStatusClick),
        QuickAction("Records", "Medical history", Icons.Outlined.FolderOpen, Color(0xFF6A1B9A), onMedicalRecordsClick),
        QuickAction("Prescriptions", "View & refill", Icons.Outlined.Medication, Color(0xFF558B2F), onPrescriptionsClick),
        QuickAction("Reminders", "Medicine", Icons.Outlined.Alarm, Color(0xFFE65100), onMedicineReminderClick),
        QuickAction("Profile", "My details", Icons.Outlined.Person, Color(0xFF37474F), onProfileClick)
    )

    val rows = actions.chunked(3)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { action ->
                    QuickActionTile(action = action, modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun QuickActionTile(action: QuickAction, modifier: Modifier = Modifier) {
    val isEmergency = action.title == "Emergency"
    Surface(
        onClick = action.onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isEmergency) DC.ErrorBg else DC.Surface,
        border = if (isEmergency) BorderStroke(1.5.dp, DC.Error.copy(alpha = 0.4f))
        else BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                    .background(action.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(action.icon, null, tint = action.color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold, color = DC.OnSurface
                ),
                textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = DC.TextSec, fontSize = 9.sp),
                textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 4 – UPCOMING APPOINTMENT
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun UpcomingAppointmentSection(
    appointment: UpcomingAppointment?,
    onViewDetails: (String) -> Unit,
    onReschedule: (String) -> Unit,
    onBookAppointmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (appointment == null) {
        EmptyStateCard(
            icon = Icons.Outlined.EventBusy,
            title = "No Upcoming Appointments",
            message = "You don't have any appointments booked yet.",
            actionLabel = "Book Now",
            onAction = onBookAppointmentClick,
            modifier = modifier
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(DC.PrimaryLight, DC.PrimaryLight.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.MedicalServices, null,
                        tint = Color.White, modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        appointment.doctorName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold, color = DC.OnSurface
                        )
                    )
                    Text(
                        appointment.department,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DC.Primary, fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(50), color = DC.SuccessBg) {
                        Text(
                            appointment.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DC.Success, fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(DC.SurfaceVar).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AppointmentDetail(Icons.Outlined.CalendarMonth, "Date", appointment.date)
                Box(Modifier.width(1.dp).height(32.dp).background(DC.Outline))
                AppointmentDetail(Icons.Outlined.Schedule, "Time", appointment.time)
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onReschedule(appointment.appointmentId) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DC.Outline)
                ) {
                    Icon(
                        Icons.Outlined.EditCalendar, null,
                        modifier = Modifier.size(16.dp), tint = DC.Primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Reschedule",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = DC.Primary, fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Button(
                    onClick = { onViewDetails(appointment.appointmentId) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)
                ) {
                    Icon(Icons.Outlined.Info, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "View Details",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
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
        Text(
            value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold, color = DC.OnSurface
            ),
            textAlign = TextAlign.Center
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(color = DC.TextSec, fontSize = 9.sp)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 5 – HEALTH SUMMARY (minimal, only shown when it has real data)
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun HealthSummaryCard(summary: HealthSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (summary.allergies.isNotEmpty()) {
                Text(
                    "Allergies",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold, color = DC.OnSurface
                    )
                )
                Spacer(Modifier.height(6.dp))
                FlowChips(items = summary.allergies, color = DC.Error, bg = DC.ErrorBg)
                if (summary.chronicConditions.isNotEmpty()) Spacer(Modifier.height(12.dp))
            }
            if (summary.chronicConditions.isNotEmpty()) {
                Text(
                    "Chronic Conditions",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold, color = DC.OnSurface
                    )
                )
                Spacer(Modifier.height(6.dp))
                FlowChips(items = summary.chronicConditions, color = DC.Warning, bg = DC.WarningBg)
            }
        }
    }
}

@Composable
private fun FlowChips(items: List<String>, color: Color, bg: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        items.forEach { label ->
            Surface(shape = RoundedCornerShape(50), color = bg) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color, fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION 6 – RECENT ACTIVITY
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun ActivityRow(activity: ActivityItem, modifier: Modifier = Modifier) {
    val (icon, color) = when (activity.type) {
        ActivityType.QUEUE -> Icons.Outlined.ConfirmationNumber to DC.Primary
        ActivityType.APPOINTMENT -> Icons.Outlined.CalendarMonth to DC.Secondary
        ActivityType.RECORD -> Icons.Outlined.FolderOpen to Color(0xFF6A1B9A)
        ActivityType.GENERAL -> Icons.Outlined.Notifications to DC.TextSec
    }

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
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold, color = DC.OnSurface
                    )
                )
                Text(
                    activity.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = DC.TextSec),
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                activity.timestamp,
                style = MaterialTheme.typography.labelSmall.copy(color = DC.TextSec, fontSize = 10.sp)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION  (visual style unchanged)
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
            val isQueue = index == 2

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(index) },
                icon = {
                    if (isQueue) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(brush = Brush.linearGradient(listOf(DC.GradStart, DC.GradEnd))),
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
                fontWeight = FontWeight.Bold, color = DC.OnSurface, fontSize = 15.sp
            )
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DC.Primary, fontWeight = FontWeight.SemiBold
                    )
                )
                Icon(Icons.Default.ChevronRight, null, tint = DC.Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon, contentDescription = null,
                tint = DC.TextSec.copy(alpha = 0.5f), modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(10.dp))
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, color = DC.OnSurface
                    )
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = DC.TextSec, textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)
                ) {
                    Text(actionLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}