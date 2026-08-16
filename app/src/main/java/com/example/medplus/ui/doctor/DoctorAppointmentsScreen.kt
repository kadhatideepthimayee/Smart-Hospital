package com.example.medplus.ui.doctor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Token
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.model.Appointment
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.DoctorAppointmentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentsScreen(
    onBackClick: () -> Unit,
    onViewDetails: (String) -> Unit,
    viewModel: DoctorAppointmentsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val todayAppointments = viewModel.getTodayAppointments()
    val upcomingAppointments = viewModel.getUpcomingAppointments()
    val completedAppointments = viewModel.getCompletedAppointments()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.errorMessage != null) {
            AppointmentErrorState(
                message = "Unable to load appointments.",
                onRetry = { viewModel.loadAppointments() },
                modifier = Modifier.padding(padding)
            )
        } else if (uiState.appointments.isEmpty()) {
            AppointmentEmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (todayAppointments.isNotEmpty()) {
                    item { AppointmentSectionHeader("Today") }
                    items(todayAppointments, key = { it.appointmentId }) { appointment ->
                        DoctorAppointmentCard(appointment) { onViewDetails(appointment.appointmentId) }
                    }
                }

                if (upcomingAppointments.isNotEmpty()) {
                    item { AppointmentSectionHeader("Upcoming") }
                    items(upcomingAppointments, key = { it.appointmentId }) { appointment ->
                        DoctorAppointmentCard(appointment) { onViewDetails(appointment.appointmentId) }
                    }
                }

                if (completedAppointments.isNotEmpty()) {
                    item { AppointmentSectionHeader("Completed") }
                    items(completedAppointments, key = { it.appointmentId }) { appointment ->
                        DoctorAppointmentCard(appointment) { onViewDetails(appointment.appointmentId) }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = SecondaryText,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DoctorAppointmentCard(
    appointment: Appointment,
    onViewDetails: () -> Unit
) {
    val status = appointment.status.trim().uppercase()
    val statusColor = when (status) {
        "CONFIRMED", "APPROVED" -> Success
        "PENDING" -> Warning
        "COMPLETED" -> Primary
        "CANCELLED" -> Error
        else -> SecondaryText
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, null, tint = Primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        appointment.patientName.ifEmpty { "Patient" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccessTime, null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(appointment.time, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        
                        if (!appointment.tokenNumber.isNullOrEmpty()) {
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.Outlined.Token, null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Token: ${appointment.tokenNumber}", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        }
                    }
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = appointment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(appointment.date, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                
                Spacer(Modifier.weight(1f))
                
                TextButton(onClick = onViewDetails) {
                    Text("View Details", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun AppointmentEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = Outline,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("No appointments scheduled.", color = SecondaryText)
        }
    }
}

@Composable
fun AppointmentErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
