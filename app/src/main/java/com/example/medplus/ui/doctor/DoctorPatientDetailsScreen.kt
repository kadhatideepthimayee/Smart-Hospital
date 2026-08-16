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
import androidx.compose.material.icons.outlined.*
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
import com.example.medplus.viewmodel.DoctorPatientDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientDetailsScreen(
    patientId: String,
    onBackClick: () -> Unit,
    viewModel: DoctorPatientDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(patientId) {
        viewModel.loadPatientDetails(patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Details", fontWeight = FontWeight.Bold) },
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
            PatientDetailsErrorState(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.loadPatientDetails(patientId) },
                modifier = Modifier.padding(padding)
            )
        } else if (uiState.patient != null) {
            val patient = uiState.patient!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                item {
                    PatientHeaderCard(patient.fullName, patient.email, patient.phone)
                }

                // Appointment History Title
                item {
                    Text(
                        "Appointment History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                }

                if (uiState.appointments.isEmpty()) {
                    item {
                        Text("No appointment history found.", color = SecondaryText)
                    }
                } else {
                    items(uiState.appointments, key = { it.appointmentId }) { appointment ->
                        AppointmentHistoryItem(appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun PatientHeaderCard(name: String, email: String, phone: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, null, tint = Primary, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryText
            )
            Spacer(Modifier.height(12.dp))
            InfoRow(Icons.Outlined.Email, email)
            InfoRow(Icons.Outlined.Phone, phone)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = SecondaryText, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(value.ifBlank { "Not provided" }, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
    }
}

@Composable
fun AppointmentHistoryItem(appointment: Appointment) {
    val status = appointment.status.trim().uppercase()
    val statusColor = when (status) {
        "COMPLETED" -> Success
        "CANCELLED" -> Error
        "PENDING" -> Warning
        else -> Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(appointment.date, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(appointment.time, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = appointment.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun PatientDetailsErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
