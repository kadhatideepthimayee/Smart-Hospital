package com.example.medplus.ui.doctor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
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
import com.example.medplus.model.QueueItem
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.DoctorQueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorQueueScreen(
    onBackClick: () -> Unit,
    viewModel: DoctorQueueViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showCompletionDialog by remember { mutableStateOf(false) }
    var diagnosis by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var followUpDate by remember { mutableStateOf("") }
    
    val currentPatient = uiState.queueItems.find { it.status.trim().uppercase() == "IN_PROGRESS" }
    val waitingPatients = uiState.queueItems.filter { it.status.trim().uppercase() == "WAITING" }
        .sortedBy { it.tokenNumber.toIntOrNull() ?: 0 }
    val totalWaitingTime = waitingPatients.size * 10 // Assuming 10 mins per patient

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            if (uiState.queueItems.isNotEmpty()) {
                android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Queue", fontWeight = FontWeight.Bold) },
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
        if (uiState.isLoading && uiState.queueItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.errorMessage != null && uiState.queueItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unable to load patient queue.", color = Error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadQueue() }) { Text("Retry") }
                }
            }
        } else if (uiState.queueItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(80.dp), tint = Outline)
                    Spacer(Modifier.height(16.dp))
                    Text("No patients in the queue", color = SecondaryText)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Summary Header
                item {
                    QueueSummaryHeader(
                        waitingCount = waitingPatients.size,
                        estimatedWait = totalWaitingTime
                    )
                }

                // Current Patient Section
                if (currentPatient != null) {
                    item { QueueSectionHeader("Current Patient") }
                    item {
                        CurrentPatientCard(
                            patient = currentPatient,
                            onComplete = { showCompletionDialog = true }
                        )
                    }
                } else if (waitingPatients.isNotEmpty()) {
                    item { QueueSectionHeader("Current Patient") }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface, AppShapes.large)
                                .border(1.dp, Outline.copy(alpha = 0.5f), AppShapes.large)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No consultation in progress", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.startConsultation(waitingPatients.first().queueId, waitingPatients.first().appointmentId) },
                                    shape = AppShapes.medium
                                ) {
                                    Icon(Icons.Outlined.PlayArrow, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Start Next Consultation")
                                }
                            }
                        }
                    }
                }

                // Waiting Queue Section
                if (waitingPatients.isNotEmpty()) {
                    item { QueueSectionHeader("Waiting Queue") }
                    items(waitingPatients, key = { it.queueId }) { item ->
                        WaitingPatientItem(item)
                    }
                }
            }
        }
        
        if (showCompletionDialog && currentPatient != null) {
            AlertDialog(
                onDismissRequest = { showCompletionDialog = false },
                title = { Text("Complete Consultation", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Patient: ${currentPatient.patientName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = diagnosis,
                            onValueChange = { diagnosis = it },
                            label = { Text("Diagnosis") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = prescription,
                            onValueChange = { prescription = it },
                            label = { Text("Prescription") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Doctor Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = followUpDate,
                            onValueChange = { followUpDate = it },
                            label = { Text("Follow-up Date (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (diagnosis.isBlank()) {
                                android.widget.Toast.makeText(context, "Diagnosis is required", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (prescription.isBlank()) {
                                android.widget.Toast.makeText(context, "Prescription is required", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.completeConsultation(
                                queueId = currentPatient.queueId,
                                appointmentId = currentPatient.appointmentId,
                                patientId = currentPatient.patientId,
                                diagnosis = diagnosis,
                                prescription = prescription,
                                notes = notes,
                                followUpDate = followUpDate
                            )
                            showCompletionDialog = false
                            diagnosis = ""
                            prescription = ""
                            notes = ""
                            followUpDate = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("Submit & Complete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCompletionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun QueueSummaryHeader(waitingCount: Int, estimatedWait: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Waiting Patients", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Text("$waitingCount", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            }
            VerticalDivider(modifier = Modifier.height(40.dp), color = Color.White.copy(alpha = 0.3f))
            Column(horizontalAlignment = Alignment.End) {
                Text("Est. Waiting Time", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Text("$estimatedWait mins", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun QueueSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = SecondaryText,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun CurrentPatientCard(patient: QueueItem, onComplete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(2.dp, Primary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, null, tint = Primary, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(patient.patientName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Token: #${patient.tokenNumber}", style = MaterialTheme.typography.bodyMedium, color = Primary, fontWeight = FontWeight.SemiBold)
                }
                Surface(color = Success.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
                    Text(
                        "IN PROGRESS",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Success
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Success)
            ) {
                Icon(Icons.Outlined.CheckCircle, null)
                Spacer(Modifier.width(8.dp))
                Text("Complete Consultation", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WaitingPatientItem(item: QueueItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SecondaryText.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.tokenNumber, fontWeight = FontWeight.Bold, color = Primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.patientName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Waiting", style = MaterialTheme.typography.bodySmall, color = Warning)
            }
            Icon(Icons.Outlined.AccessTime, null, tint = SecondaryText, modifier = Modifier.size(16.dp))
        }
    }
}
