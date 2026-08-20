package com.example.medplus.ui.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medplus.model.Appointment
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.MyAppointmentsViewModel

// ════════════════════════════════════════════════════════════════════════════
//  SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppointmentsScreen(
    viewModel: MyAppointmentsViewModel,
    onBackClick: () -> Unit,
    onBookAppointmentClick: () -> Unit,
    onViewDetailsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Completed", "Cancelled")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Appointments",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── TABS ──────────────────────────────────────────────────
            Surface(
                color = Surface,
                modifier = Modifier.fillMaxWidth().shadow(2.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Surface,
                    contentColor = Primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            unselectedContentColor = SecondaryText
                        )
                    }
                }
            }

            // ── CONTENT ───────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.appointments.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (uiState.errorMessage != null && uiState.appointments.isEmpty()) {
                    ErrorState(
                        message = uiState.errorMessage ?: "Unable to load appointments",
                        onRetry = { viewModel.loadAppointments() }
                    )
                } else {
                    val filteredAppointments = when (selectedTabIndex) {
                        0 -> uiState.appointments.filter { it.status == "UPCOMING" || it.status == "CONFIRMED" || it.status == "IN_PROGRESS" || it.status == "PENDING" || it.status == "ACTIVE" }
                        1 -> uiState.appointments.filter { it.status == "COMPLETED" }
                        2 -> uiState.appointments.filter { it.status == "CANCELLED" }
                        else -> emptyList()
                    }

                    if (filteredAppointments.isEmpty() && !uiState.isLoading) {
                        EmptyState(
                            tabIndex = selectedTabIndex,
                            onBookClick = onBookAppointmentClick
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredAppointments, key = { it.appointmentId }) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onViewDetails = { onViewDetailsClick(appointment.appointmentId) },
                                    onCancel = { viewModel.cancelAppointment(appointment.appointmentId) }
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }

                // Small loading overlay if refreshing
                if (uiState.isLoading && uiState.appointments.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                        color = Primary,
                        trackColor = Primary.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SUB-COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onViewDetails: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Doctor Info & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Person, null, tint = Primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = appointment.doctorName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryText
                        )
                        Text(
                            text = appointment.department,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Primary
                        )
                    }
                }
                StatusBadge(status = appointment.status)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Outline.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            // Details: Date & Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Background.copy(alpha = 0.5f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailItem(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Date",
                    value = appointment.date
                )
                Box(Modifier.width(1.dp).height(32.dp).background(Outline))
                DetailItem(
                    icon = Icons.Outlined.Schedule,
                    label = "Time",
                    value = appointment.time
                )
                if (!appointment.tokenNumber.isNullOrEmpty()) {
                    Box(Modifier.width(1.dp).height(32.dp).background(Outline))
                    DetailItem(
                        icon = Icons.Outlined.ConfirmationNumber,
                        label = "Token",
                        value = "#${appointment.tokenNumber}"
                    )
                }
            }

            // Actions (only for Upcoming)
            if (appointment.status == "UPCOMING" || appointment.status == "PENDING" || appointment.status == "CONFIRMED") {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("View Details", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "UPCOMING", "PENDING", "CONFIRMED", "ACTIVE" -> Primary
        "COMPLETED" -> Success
        "CANCELLED" -> Error
        else -> SecondaryText
    }
    val bgColor = when (status) {
        "UPCOMING", "PENDING", "CONFIRMED", "ACTIVE" -> Color(0xFFE3F2FD)
        "COMPLETED" -> Color(0xFFE8F5E9)
        "CANCELLED" -> Color(0xFFFFEBEE)
        else -> Outline.copy(alpha = 0.3f)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryText
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = SecondaryText
        )
    }
}

@Composable
private fun EmptyState(tabIndex: Int, onBookClick: () -> Unit) {
    val title = when (tabIndex) {
        0 -> "No Upcoming Appointments"
        1 -> "No Completed Appointments"
        2 -> "No Cancelled Appointments"
        else -> ""
    }
    val description = when (tabIndex) {
        0 -> "Book an appointment with a doctor to get started."
        else -> "Your appointment history will appear here."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = Primary.copy(alpha = 0.3f)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = PrimaryText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )
        if (tabIndex == 0) {
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBookClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Book Appointment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.ErrorOutline, null, modifier = Modifier.size(60.dp), tint = Error.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text("Unable to load appointments", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryText)
        Text("Please try again.", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry, 
            shape = RoundedCornerShape(14.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Retry")
        }
    }
}
