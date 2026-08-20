package com.example.medplus.ui.doctor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medplus.dashboard.viewmodel.DoctorDashboardViewModel
import com.example.medplus.model.Appointment
import com.example.medplus.model.DoctorProfile
import com.example.medplus.dashboard.model.LiveQueueInfo
import com.example.medplus.ui.theme.*

/**
 * Professional colors for Doctor Dashboard
 */
private object DC {
    val Primary = Color(0xFF0B3D91)
    val PrimaryLight = Color(0xFFE3F2FD)
    val Secondary = Color(0xFF00897B)
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val Outline = Color(0xFFE2E8F0)
    
    val Success = Color(0xFF10B981)
    val SuccessBg = Color(0xFFECFDF5)
    val Warning = Color(0xFFF59E0B)
    val WarningBg = Color(0xFFFFFBEB)
    val Error = Color(0xFFEF4444)
    
    val GradStart = Color(0xFF0B3D91)
    val GradEnd = Color(0xFF1E40AF)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    viewModel: DoctorDashboardViewModel,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onQueueClick: () -> Unit,
    onAvailabilityClick: () -> Unit,
    onViewPatientClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var showCompleteDialog by remember { mutableStateOf<com.example.medplus.model.QueueItem?>(null) }
    var diagnosis by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var followUpDate by remember { mutableStateOf("") }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboardData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    if (showCompleteDialog != null) {
        val patientName = showCompleteDialog?.patientName ?: "Patient"
        AlertDialog(
            onDismissRequest = {
                showCompleteDialog = null
                diagnosis = ""
                prescription = ""
                notes = ""
                followUpDate = ""
            },
            title = { Text("Complete Consultation", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Patient: $patientName")
                    OutlinedTextField(
                        value = diagnosis,
                        onValueChange = { diagnosis = it },
                        label = { Text("Diagnosis (Required)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = prescription,
                        onValueChange = { prescription = it },
                        label = { Text("Prescription/Medicines (Required)") },
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
                            queueItem = showCompleteDialog!!,
                            diagnosis = diagnosis,
                            prescription = prescription,
                            notes = notes,
                            followUpDate = followUpDate
                        )
                        showCompleteDialog = null
                        diagnosis = ""
                        prescription = ""
                        notes = ""
                        followUpDate = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DC.Success)
                ) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteDialog = null
                    diagnosis = ""
                    prescription = ""
                    notes = ""
                    followUpDate = ""
                }) {
                    Text("Cancel")
                }
            },
            containerColor = DC.Surface,
            shape = AppShapes.large
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = DC.Background
    ) { paddings ->
        if (uiState.isLoading && uiState.doctorProfile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DC.Primary)
            }
        } else if (uiState.errorMessage != null && uiState.doctorProfile == null) {
            ErrorState(
                message = uiState.errorMessage ?: "Something went wrong",
                onRetry = { viewModel.loadDashboardData() }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. HEADER SECTION
                item {
                    DoctorDashboardHeader(
                        profile = uiState.doctorProfile,
                        unreadCount = uiState.unreadNotificationCount,
                        onNotificationClick = onNotificationClick,
                        onProfileClick = onProfileClick
                    )
                }

                // 2. OVERVIEW STATS
                item {
                    DashboardOverviewSection(
                        today = uiState.todayCount,
                        completed = uiState.completedCount,
                        patients = uiState.uniquePatientCount,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 2.2 NEXT APPOINTMENT CARD
                item {
                    NextAppointmentCard(
                        appointment = uiState.nextAppointment,
                        onAppointmentsClick = onAppointmentsClick,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 2.5 CURRENT CONSULTATION SECTION
                item {
                    CurrentConsultationSection(
                        current = uiState.currentConsultation,
                        currentTime = uiState.currentPatientTime,
                        next = uiState.nextWaitingPatient,
                        actionLoading = uiState.actionLoading,
                        onStart = { viewModel.startConsultation(it) },
                        onComplete = { showCompleteDialog = it },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 3. LIVE QUEUE CARD
                item {
                    LiveQueueCard(
                        waitingCount = uiState.waitingQueueCount,
                        nextPatientName = uiState.nextPatientName,
                        estimatedWait = uiState.estimatedWaitMinutes,
                        onQueueClick = onQueueClick,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 3.5 AVAILABILITY SUMMARY CARD
                item {
                    AvailabilitySummaryCard(
                        profile = uiState.doctorProfile,
                        onClick = onAvailabilityClick,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 4. QUICK ACTIONS
                item {
                    DashboardQuickActions(
                        onAppointmentsClick = onAppointmentsClick,
                        onPatientsClick = onPatientsClick,
                        onQueueClick = onQueueClick,
                        onAvailabilityClick = onAvailabilityClick,
                        onProfileClick = onProfileClick,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 5. TODAY'S APPOINTMENTS
                item {
                    DashboardSectionHeader(
                        title = "Today's Appointments",
                        actionLabel = uiState.todayAppointments.size.let { if (it > 0) "($it) View All" else "View All" },
                        onAction = onAppointmentsClick,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                if (uiState.isAppointmentError) {
                    item {
                        AppointmentErrorCard(
                            onRetry = { viewModel.loadDashboardData() },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else if (uiState.todayAppointments.isEmpty()) {
                    item {
                        EmptyAppointmentsCard(modifier = Modifier.padding(horizontal = 20.dp))
                    }
                } else {
                    items(uiState.todayAppointments, key = { it.appointmentId }) { appointment ->
                        DoctorAppointmentCard(
                            appointment = appointment,
                            onViewPatient = { onViewPatientClick(appointment.patientId) },
                            onStart = { 
                                // Find corresponding queue item to use startConsultation logic
                                val qItem = uiState.nextWaitingPatient?.takeIf { it.appointmentId == appointment.appointmentId }
                                    ?: com.example.medplus.model.QueueItem(
                                        appointmentId = appointment.appointmentId,
                                        patientName = appointment.patientName,
                                        tokenNumber = appointment.tokenNumber ?: "",
                                        doctorId = appointment.doctorId
                                    ) // Fallback if not in current waiting state but from list
                                viewModel.startConsultation(qItem)
                            },
                            onComplete = {
                                val qItem = uiState.currentConsultation?.takeIf { it.appointmentId == appointment.appointmentId }
                                    ?: com.example.medplus.model.QueueItem(
                                        appointmentId = appointment.appointmentId,
                                        patientName = appointment.patientName,
                                        tokenNumber = appointment.tokenNumber ?: "",
                                        doctorId = appointment.doctorId
                                    )
                                showCompleteDialog = qItem
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 6. PROFILE SUMMARY
                item {
                    DashboardSectionHeader(
                        title = "Profile Summary",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                
                item {
                    DoctorProfileSummaryCard(
                        profile = uiState.doctorProfile,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DoctorDashboardHeader(
    profile: DoctorProfile?,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(listOf(DC.GradStart, DC.GradEnd)),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable(onClick = onProfileClick),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile?.profileImage.isNullOrEmpty()) {
                            // Image loading would happen here
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        val calendar = java.util.Calendar.getInstance()
                        val greeting = when (calendar[java.util.Calendar.HOUR_OF_DAY]) {
                            in 0..11 -> "Good Morning"
                            in 12..16 -> "Good Afternoon"
                            else -> "Good Evening"
                        }
                        Text(
                            greeting,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "Dr. ${profile?.fullName ?: "Doctor"}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            profile?.specialization?.ifBlank { profile.department } ?: "Healthcare Professional",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge {
                                    Text(unreadCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Notifications, "Notifications", tint = Color.White)
                    }
                }
            }

            // Verification Badge
            VerificationStatusBadge(status = profile?.verificationStatus ?: "PENDING")
        }
    }
}

@Composable
private fun VerificationStatusBadge(status: String) {
    val normalizedStatus = status.trim().uppercase()
    
    val color = when (normalizedStatus) {
        "VERIFIED", "APPROVED" -> DC.Success
        "REJECTED" -> DC.Error
        else -> DC.Warning
    }
    
    val text = when (normalizedStatus) {
        "VERIFIED", "APPROVED" -> "Verified Doctor"
        "REJECTED" -> "Verification Rejected"
        else -> "Verification Pending"
    }
    
    val icon = when (normalizedStatus) {
        "VERIFIED", "APPROVED" -> Icons.Default.Verified
        "REJECTED" -> Icons.Default.Cancel
        else -> Icons.Default.HourglassEmpty
    }

    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = if (normalizedStatus in listOf("APPROVED", "VERIFIED")) Color(0xFF4CAF50) else color, modifier = Modifier.size(16.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun DashboardOverviewSection(
    today: Int,
    completed: Int,
    patients: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverviewStatCard(
            label = "Today's Appts",
            value = today.toString(),
            icon = Icons.Outlined.CalendarToday,
            color = DC.Primary,
            modifier = Modifier.weight(1f)
        )
        OverviewStatCard(
            label = "My Patients",
            value = patients.toString(),
            icon = Icons.Outlined.Group,
            color = Color(0xFF6366F1),
            modifier = Modifier.weight(1f)
        )
        OverviewStatCard(
            label = "Completed",
            value = completed.toString(),
            icon = Icons.Outlined.CheckCircle,
            color = DC.Secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OverviewStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = DC.OnSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = DC.TextSecondary
            )
        }
    }
}

@Composable
private fun LiveQueueCard(
    waitingCount: Int,
    nextPatientName: String?,
    estimatedWait: Int,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onQueueClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DC.Error)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Patient Queue",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = DC.OnSurface
                    )
                }
                Text(
                    "Waiting: $waitingCount",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DC.Primary
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = if (nextPatientName != null) "Next: $nextPatientName" else "No patients waiting",
                style = MaterialTheme.typography.bodyMedium,
                color = DC.OnSurface
            )

            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Estimated Wait:",
                    style = MaterialTheme.typography.labelSmall,
                    color = DC.TextSecondary
                )
                Text(
                    if (waitingCount > 0) "$estimatedWait mins" else "-",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DC.Warning
                )
            }
        }
    }
}

@Composable
private fun CurrentConsultationSection(
    current: com.example.medplus.model.QueueItem?,
    currentTime: String?,
    next: com.example.medplus.model.QueueItem?,
    actionLoading: Boolean,
    onStart: (com.example.medplus.model.QueueItem) -> Unit,
    onComplete: (com.example.medplus.model.QueueItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (current != null || next != null) {
            Text(
                text = if (current != null) "Current Patient" else "Next Patient",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DC.OnSurface
            )
        }

        if (current != null) {
            ConsultationActionCard(
                patientName = current.patientName,
                token = current.tokenNumber,
                time = currentTime,
                status = "IN PROGRESS",
                buttonLabel = "Complete Consultation",
                buttonColor = DC.Success,
                isLoading = actionLoading,
                onClick = { onComplete(current) }
            )
        } else if (next != null) {
            ConsultationActionCard(
                patientName = next.patientName,
                token = next.tokenNumber,
                time = currentTime,
                status = "WAITING",
                buttonLabel = "Start Consultation",
                buttonColor = DC.Primary,
                isLoading = actionLoading,
                onClick = { onStart(next) }
            )
        } else {
            Text(
                text = "Queue Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DC.OnSurface
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(DC.Surface, AppShapes.large)
                    .border(BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f)), AppShapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text("No more patients waiting.", color = DC.TextSecondary)
            }
        }
    }
}

@Composable
private fun ConsultationActionCard(
    patientName: String,
    token: String,
    time: String?,
    status: String,
    buttonLabel: String,
    buttonColor: Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DC.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, null, tint = DC.Primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(patientName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Token: #$token", style = MaterialTheme.typography.bodyMedium, color = DC.Primary, fontWeight = FontWeight.SemiBold)
                }
                StatusChip(status = status)
            }
            
            if (time != null) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = DC.TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Appointment Time: $time", style = MaterialTheme.typography.bodySmall, color = DC.TextSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(buttonLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AvailabilitySummaryCard(
    profile: DoctorProfile?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFECB3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Schedule, null, tint = Color(0xFFFFA000))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Your Availability", style = MaterialTheme.typography.labelMedium, color = DC.TextSecondary)
                if (profile != null && profile.workingDays.isNotEmpty()) {
                    val daysRange = if (profile.workingDays.size >= 2) {
                        "${profile.workingDays.first()} - ${profile.workingDays.last()}"
                    } else {
                        profile.workingDays.first()
                    }
                    Text(
                        "$daysRange • ${profile.consultationStartTime} - ${profile.consultationEndTime}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DC.OnSurface
                    )
                    Text(
                        "Slot: ${profile.slotDuration} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = DC.TextSecondary
                    )
                } else {
                    Text("Schedule not configured", style = MaterialTheme.typography.bodyMedium, color = DC.Error)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = DC.Outline)
        }
    }
}

@Composable
private fun QueueInfoItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = DC.TextSecondary
        )
    }
}

@Composable
private fun DashboardQuickActions(
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onQueueClick: () -> Unit,
    onAvailabilityClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Quick Management",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DC.OnSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                label = "Appointments",
                icon = Icons.Outlined.Event,
                color = DC.Primary,
                onClick = onAppointmentsClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Patients",
                icon = Icons.Outlined.Group,
                color = Color(0xFF6366F1),
                onClick = onPatientsClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                label = "Queue",
                icon = Icons.Outlined.FormatListNumbered,
                color = DC.Secondary,
                onClick = onQueueClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Schedule",
                icon = Icons.Outlined.WatchLater,
                color = Color(0xFFEC4899),
                onClick = onAvailabilityClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionButton(
                label = "My Profile",
                icon = Icons.Outlined.Person,
                color = Color(0xFF6366F1),
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth(0.485f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.medium,
        color = DC.Surface,
        border = BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = DC.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DoctorAppointmentCard(
    appointment: Appointment,
    onViewPatient: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DC.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        appointment.patientName.take(1).uppercase(),
                        color = DC.Primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        appointment.patientName.ifEmpty { "Patient Name" },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = DC.OnSurface
                    )
                    Text(
                        "${appointment.time} • Token #${appointment.tokenNumber ?: "-"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DC.TextSecondary
                    )
                }
                
                StatusChip(status = appointment.status)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewPatient,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.medium,
                    border = BorderStroke(1.dp, DC.Outline)
                ) {
                    Text("Patient Details", style = MaterialTheme.typography.labelMedium)
                }
                
                if (appointment.status == "UPCOMING" || appointment.status == "PENDING") {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)
                    ) {
                        Text("Start", style = MaterialTheme.typography.labelMedium)
                    }
                } else if (appointment.status == "IN_PROGRESS") {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = DC.Secondary)
                    ) {
                        Text("Complete", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (color, bgColor) = when (status.uppercase()) {
        "UPCOMING" -> DC.Warning to DC.WarningBg
        "COMPLETED" -> DC.Success to DC.SuccessBg
        "IN_PROGRESS" -> DC.Primary to DC.PrimaryLight
        else -> DC.TextSecondary to DC.Outline.copy(alpha = 0.1f)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun DoctorProfileSummaryCard(
    profile: DoctorProfile?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileSummaryRow(Icons.Outlined.Badge, "Reg. Number", profile?.registrationNumber ?: "Not set")
            ProfileSummaryRow(Icons.Outlined.School, "Qualification", profile?.qualification ?: "Not set")
            ProfileSummaryRow(Icons.Outlined.History, "Experience", "${profile?.experienceYears ?: 0} Years")
            ProfileSummaryRow(Icons.Outlined.Payments, "Consultation Fee", "₹${profile?.consultationFee ?: 0.0}")
            ProfileSummaryRow(Icons.Outlined.Today, "Working Days", profile?.workingDays?.joinToString(", ") ?: "Not set")
            ProfileSummaryRow(Icons.Outlined.Schedule, "Time", "${profile?.consultationStartTime ?: "-"} to ${profile?.consultationEndTime ?: "-"}")
        }
    }
}

@Composable
private fun ProfileSummaryRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = DC.Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = DC.TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = DC.OnSurface)
        }
    }
}

@Composable
private fun DashboardSectionHeader(
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
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = DC.OnSurface
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = DC.Primary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Icon(Icons.Default.ChevronRight, null, tint = DC.Primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun EmptyAppointmentsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        border = BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.EventBusy, null, tint = DC.Outline, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                "No appointments scheduled",
                color = DC.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NextAppointmentCard(
    appointment: Appointment?,
    onAppointmentsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = DC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, DC.Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Next Appointment",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DC.OnSurface
                )
                TextButton(onClick = onAppointmentsClick) {
                    Text("Schedule", color = DC.Primary, style = MaterialTheme.typography.labelMedium)
                }
            }
            
            if (appointment != null) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DC.Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = DC.Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            appointment.patientName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = DC.OnSurface
                        )
                        Text(
                            "${appointment.date} • ${appointment.time}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DC.TextSecondary
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    "No upcoming appointments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DC.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AppointmentErrorCard(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Unable to load appointments",
                color = DC.Error,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = DC.Error),
                shape = AppShapes.medium,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Retry", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = DC.Error, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                color = DC.OnSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = DC.Primary),
                shape = AppShapes.medium
            ) {
                Text("Retry")
            }
        }
    }
}
