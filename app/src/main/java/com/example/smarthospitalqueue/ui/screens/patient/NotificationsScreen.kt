package com.example.smarthospitalqueue.ui.screens.patient

// ════════════════════════════════════════════════════════════════════════════
//  NotificationsScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Self-contained mock data · Aligned with all preceding screens
//  Patient: Arjun Raghavan · Doctor: Dr. Arjun Mehta · Token: T-052
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

// ─── Color Palette (shared across all screens) ────────────────────────────────

private object NColors {
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

    // Notification type accents
    val Queue             = Color(0xFF1565C0)
    val QueueBg           = Color(0xFFDCE8FF)
    val Appointment       = Color(0xFF2E7D32)
    val AppointmentBg     = Color(0xFFE8F5E9)
    val Emergency         = Color(0xFFC62828)
    val EmergencyBg       = Color(0xFFFFEBEE)
    val Payment           = Color(0xFF6A1B9A)
    val PaymentBg         = Color(0xFFF3E5F5)
    val Announcement      = Color(0xFFF57F17)
    val AnnouncementBg    = Color(0xFFFFF8E1)
}

// ─── Domain model ─────────────────────────────────────────────────────────────

private enum class NotificationType {
    QUEUE, APPOINTMENT, EMERGENCY, PAYMENT, ANNOUNCEMENT
}

private data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val time: String,
    val isRead: Boolean,
    val isPinned: Boolean = false
)

// ─── Mock Data — all events linked to Arjun Raghavan / T-052 narrative ────────

private object NotifMockData {
    val patientName = "Arjun Raghavan"
    val patientId   = "APOL-PAT-20241182"

    val notifications = listOf(
        // Pinned emergency — most prominent
        Notification(
            id        = "N-001",
            type      = NotificationType.EMERGENCY,
            title     = "Emergency Delay — Dr. Arjun Mehta",
            body      = "Dr. Arjun Mehta is attending an emergency case. Your estimated wait has increased by 5–10 minutes. We apologise for the inconvenience.",
            time      = "2 min ago",
            isRead    = false,
            isPinned  = true
        ),
        // Queue alert — unread
        Notification(
            id        = "N-002",
            type      = NotificationType.QUEUE,
            title     = "You're Next! Token T-052",
            body      = "Only 2 patients remain before you. Please proceed to Room 204, Floor 2, Cardiology Wing and be ready.",
            time      = "8 min ago",
            isRead    = false
        ),
        // Queue update — unread
        Notification(
            id        = "N-003",
            type      = NotificationType.QUEUE,
            title     = "Queue Update — T-052",
            body      = "6 patients are ahead of you. Estimated wait: ~28 minutes. AI predicts your slot at approximately 10:26 AM.",
            time      = "35 min ago",
            isRead    = false
        ),
        // Appointment confirmed — unread
        Notification(
            id        = "N-004",
            type      = NotificationType.APPOINTMENT,
            title     = "Appointment Confirmed",
            body      = "Your appointment with Dr. Arjun Mehta (Senior Cardiologist) is confirmed for Tuesday, 3 June 2025 at 10:00 AM. Token T-052 has been generated.",
            time      = "1 hr ago",
            isRead    = false
        ),
        // Payment — read
        Notification(
            id        = "N-005",
            type      = NotificationType.PAYMENT,
            title     = "Payment Successful — ₹800",
            body      = "Consultation fee of ₹800 for Dr. Arjun Mehta has been received. Reference: APT-2024-1182. A receipt has been sent to your registered email.",
            time      = "1 hr ago",
            isRead    = true
        ),
        // Appointment reminder — read
        Notification(
            id        = "N-006",
            type      = NotificationType.APPOINTMENT,
            title     = "Appointment Reminder",
            body      = "Reminder: Your cardiology appointment is tomorrow at 10:00 AM. AI recommends arriving by 9:45 AM to avoid peak queue hours.",
            time      = "Yesterday, 8:00 PM",
            isRead    = true
        ),
        // Announcement — read
        Notification(
            id        = "N-007",
            type      = NotificationType.ANNOUNCEMENT,
            title     = "OPD Hours Extended — Cardiology",
            body      = "Apollo Smart Hospital has extended Cardiology OPD hours to 8:00 PM on weekdays starting June 2025. Book your slot now.",
            time      = "Yesterday, 2:00 PM",
            isRead    = true
        ),
        // Queue token generated — read
        Notification(
            id        = "N-008",
            type      = NotificationType.QUEUE,
            title     = "Digital Token Generated",
            body      = "Token T-052 has been assigned to you for Cardiology, Room 204. Show this token at the reception or scan the QR code at the kiosk.",
            time      = "1 hr ago",
            isRead    = true
        ),
        // Announcement — read
        Notification(
            id        = "N-009",
            type      = NotificationType.ANNOUNCEMENT,
            title     = "Health Camp — Free BP Screening",
            body      = "Apollo Smart Hospital is conducting a free blood pressure and diabetes screening camp on 7 June 2025. Register at the front desk.",
            time      = "2 days ago",
            isRead    = true
        ),
        // Payment reminder — read
        Notification(
            id        = "N-010",
            type      = NotificationType.PAYMENT,
            title     = "Lab Report Ready for Download",
            body      = "Your lipid profile report (LR-2024-0598) ordered by Dr. Arjun Mehta is now available. Visit Medical Records to download.",
            time      = "3 days ago",
            isRead    = true
        ),
    )

    val filterTypes = listOf("All", "Queue", "Appointment", "Payment", "Emergency", "Announcement")

    // Stats for the header
    val totalCount   = notifications.size
    val unreadCount  = notifications.count { !it.isRead }
    val pinnedCount  = notifications.count { it.isPinned }
}

// ─── Type-style helper ────────────────────────────────────────────────────────

private data class NotifStyle(
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val label: String
)

private fun notifStyle(type: NotificationType): NotifStyle = when (type) {
    NotificationType.QUEUE        -> NotifStyle(Icons.Outlined.QueueMusic,       NColors.Queue,        NColors.QueueBg,        "Queue")
    NotificationType.APPOINTMENT  -> NotifStyle(Icons.Outlined.CalendarMonth,    NColors.Appointment,  NColors.AppointmentBg,  "Appointment")
    NotificationType.EMERGENCY    -> NotifStyle(Icons.Outlined.Warning,          NColors.Emergency,    NColors.EmergencyBg,    "Emergency")
    NotificationType.PAYMENT      -> NotifStyle(Icons.Outlined.Receipt,          NColors.Payment,      NColors.PaymentBg,      "Payment")
    NotificationType.ANNOUNCEMENT -> NotifStyle(Icons.Outlined.Campaign,         NColors.Announcement, NColors.AnnouncementBg, "Announcement")
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var headerVisible  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); headerVisible = true }

    val filtered = remember(selectedFilter) {
        when (selectedFilter) {
            "Queue"        -> NotifMockData.notifications.filter { it.type == NotificationType.QUEUE }
            "Appointment"  -> NotifMockData.notifications.filter { it.type == NotificationType.APPOINTMENT }
            "Payment"      -> NotifMockData.notifications.filter { it.type == NotificationType.PAYMENT }
            "Emergency"    -> NotifMockData.notifications.filter { it.type == NotificationType.EMERGENCY }
            "Announcement" -> NotifMockData.notifications.filter { it.type == NotificationType.ANNOUNCEMENT }
            else           -> NotifMockData.notifications
        }
    }

    val unreadInFilter = filtered.count { !it.isRead }

    Scaffold(
        topBar = { NotificationsTopBar(onBackClick = onBackClick) },
        containerColor = NColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Patient + unread summary strip ──────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 3 }
            ) {
                NotificationSummaryHeader()
            }

            // ── Unread banner ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible && unreadInFilter > 0,
                enter = fadeIn(tween(400, 150))
            ) {
                UnreadBanner(count = unreadInFilter)
            }

            // ── Filter chips ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(400, 200))
            ) {
                FilterChipsRow(
                    types = NotifMockData.filterTypes,
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it },
                    notifications = NotifMockData.notifications
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Results header ──────────────────────────────────────────────
            AnimatedContent(
                targetState = filtered.size,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "count"
            ) { count ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$count notification${if (count != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NColors.TextSecondary
                        )
                    )
                    TextButton(
                        onClick = {},
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Outlined.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = NColors.Primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Mark all read",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NColors.Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // ── List / empty state ──────────────────────────────────────────
            if (filtered.isEmpty()) {
                EmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 2.dp, bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pinned first
                    val pinned   = filtered.filter { it.isPinned }
                    val unpinned = filtered.filter { !it.isPinned }

                    if (pinned.isNotEmpty()) {
                        item {
                            SectionLabel(
                                label = "📌  Pinned",
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        items(pinned, key = { it.id }) { notif ->
                            NotificationCard(notification = notif)
                        }
                        item { Spacer(Modifier.height(4.dp)) }
                    }

                    if (unpinned.isNotEmpty()) {
                        item {
                            SectionLabel(
                                label = "Recent",
                                modifier = Modifier.padding(bottom = 6.dp, top = if (pinned.isNotEmpty()) 8.dp else 0.dp)
                            )
                        }
                        items(unpinned, key = { it.id }) { notif ->
                            NotificationCard(notification = notif)
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
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
private fun NotificationsTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(NColors.GradientStart, NColors.GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Notifications",
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
                // Unread badge icon
                Box {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notifications", tint = Color.White)
                    }
                    if (NotifMockData.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-6).dp, y = 6.dp)
                                .clip(CircleShape)
                                .background(NColors.Emergency),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${NotifMockData.unreadCount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Options", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  NOTIFICATION SUMMARY HEADER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun NotificationSummaryHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(NColors.GradientEnd, NColors.Background),
                    startY = 0f,
                    endY = 400f
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Patient avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "AR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    NotifMockData.patientName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    NotifMockData.patientId,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }

            // Stat pills
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotifStatPill(
                    label = "${NotifMockData.unreadCount} Unread",
                    color = NColors.Emergency
                )
                NotifStatPill(
                    label = "${NotifMockData.totalCount} Total",
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun NotifStatPill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  UNREAD BANNER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun UnreadBanner(count: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label = "dot_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NColors.PrimaryContainer)
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
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NColors.Primary.copy(alpha = dotAlpha))
            )
            Text(
                "$count unread notification${if (count != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NColors.Primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Text(
            "Tap to dismiss",
            style = MaterialTheme.typography.labelSmall.copy(
                color = NColors.Primary.copy(alpha = 0.7f)
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  FILTER CHIPS ROW
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun FilterChipsRow(
    types: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    notifications: List<Notification>
) {
    val chipIcons = mapOf(
        "All"          to Icons.Default.GridView,
        "Queue"        to Icons.Outlined.QueueMusic,
        "Appointment"  to Icons.Outlined.CalendarMonth,
        "Payment"      to Icons.Outlined.Receipt,
        "Emergency"    to Icons.Outlined.Warning,
        "Announcement" to Icons.Outlined.Campaign,
    )
    val chipColors = mapOf(
        "All"          to NColors.Primary,
        "Queue"        to NColors.Queue,
        "Appointment"  to NColors.Appointment,
        "Payment"      to NColors.Payment,
        "Emergency"    to NColors.Emergency,
        "Announcement" to NColors.Announcement,
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types) { type ->
            val isSelected = type == selected
            val accentColor = chipColors[type] ?: NColors.Primary
            val unreadForType = when (type) {
                "All"          -> notifications.count { !it.isRead }
                "Queue"        -> notifications.count { it.type == NotificationType.QUEUE && !it.isRead }
                "Appointment"  -> notifications.count { it.type == NotificationType.APPOINTMENT && !it.isRead }
                "Payment"      -> notifications.count { it.type == NotificationType.PAYMENT && !it.isRead }
                "Emergency"    -> notifications.count { it.type == NotificationType.EMERGENCY && !it.isRead }
                "Announcement" -> notifications.count { it.type == NotificationType.ANNOUNCEMENT && !it.isRead }
                else           -> 0
            }

            Surface(
                onClick = { onSelect(type) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) accentColor else NColors.Surface,
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) accentColor else NColors.Outline
                ),
                shadowElevation = if (isSelected) 4.dp else 0.dp,
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = chipIcons[type] ?: Icons.Default.GridView,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else NColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        type,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) Color.White else NColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    // Unread badge on chip
                    if (unreadForType > 0) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.3f)
                                    else accentColor.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$unreadForType",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else accentColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  NOTIFICATION CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun NotificationCard(notification: Notification) {
    val style = notifStyle(notification.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!notification.isRead)
                    Modifier.border(1.5.dp, style.color.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                else if (notification.isPinned)
                    Modifier.border(1.5.dp, NColors.Emergency.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                notification.isPinned  -> NColors.ErrorContainer
                !notification.isRead   -> NColors.PrimaryContainer.copy(alpha = 0.45f)
                else                   -> NColors.Surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notification.isRead || notification.isPinned) 4.dp else 2.dp
        ),
        onClick = {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Type icon box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(style.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        style.icon,
                        contentDescription = null,
                        tint = style.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            notification.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (!notification.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = NColors.OnSurface
                            ),
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Unread dot
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(NColors.Primary)
                            )
                        }
                    }

                    Spacer(Modifier.height(5.dp))

                    // Body text
                    Text(
                        notification.body,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NColors.TextSecondary,
                            lineHeight = 19.sp
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    // Bottom meta row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type badge + time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = style.bgColor
                            ) {
                                Text(
                                    style.label,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = style.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            if (notification.isPinned) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NColors.ErrorContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = NColors.Emergency,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            "Pinned",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NColors.Emergency,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = NColors.Outline,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                notification.time,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NColors.Outline
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SECTION LABEL
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = NColors.TextSecondary,
            letterSpacing = 0.5.sp
        )
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  EMPTY STATE
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = NColors.Primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No notifications",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = NColors.OnSurface
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "You're all caught up!\nCheck back later for updates.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = NColors.TextSecondary
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true, name = "Notifications Screen")
@Composable
private fun NotificationsScreenPreview() {
    MaterialTheme {
        NotificationsScreen(onBackClick = {})
    }
}