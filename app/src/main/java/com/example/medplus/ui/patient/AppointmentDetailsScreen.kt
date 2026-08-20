package com.example.medplus.ui.patient

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medplus.model.Appointment
import com.example.medplus.repository.AppointmentRepository
import com.example.medplus.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(
    appointmentId: String,
    onBackClick: () -> Unit,
    onViewLiveQueueClick: () -> Unit
) {
    val context = LocalContext.current
    val firestore = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    val dashboardRepo = remember { com.example.medplus.repository.DashboardRepository() }
    val scope = rememberCoroutineScope()
    val appointmentRepo = remember { AppointmentRepository() }

    var appointment by remember { mutableStateOf<Appointment?>(null) }
    var consultationFee by remember { mutableStateOf<Double?>(null) }
    var queueStatus by remember { mutableStateOf<String?>(null) }
    var patientsAhead by remember { mutableStateOf(0) }
    var estimatedWaitMinutes by remember { mutableStateOf(0) }
    var isQueueActive by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isCancelling by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var hasSubmittedFeedback by remember { mutableStateOf(false) }
    var isSubmittingFeedback by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(0) }
    var userFeedbackText by remember { mutableStateOf("") }
    var existingRating by remember { mutableStateOf(0) }
    var existingFeedbackText by remember { mutableStateOf("") }

    var queuePollingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun loadData() {
        isLoading = true
        error = null
        scope.launch {
            try {
                firestore.collection("appointments").document(appointmentId).get()
                    .addOnSuccessListener { apptDoc ->
                        val appt = apptDoc.toObject(Appointment::class.java)?.copy(appointmentId = apptDoc.id)
                        if (appt != null) {
                            appointment = appt

                            // Fetch Doctor Consultation Fee
                            firestore.collection("doctor_profiles").document(appt.doctorId).get()
                                .addOnSuccessListener { docProfileDoc ->
                                    if (docProfileDoc.exists()) {
                                        consultationFee = docProfileDoc.getDouble("consultationFee")
                                    }
                                }

                            // Start live queue polling via flow
                            queuePollingJob?.cancel()
                            queuePollingJob = scope.launch {
                                dashboardRepo.getLiveQueueUpdates(appointmentId).collect { liveQueue ->
                                    if (liveQueue != null) {
                                        isQueueActive = liveQueue.isActive
                                        queueStatus = liveQueue.status
                                        patientsAhead = liveQueue.patientsAhead
                                        estimatedWaitMinutes = liveQueue.estimatedWaitMinutes
                                    } else {
                                        isQueueActive = false
                                    }
                                }
                            }

                            // Fetch if feedback already submitted
                            firestore.collection("feedback")
                                .whereEqualTo("appointmentId", appointmentId)
                                .get()
                                .addOnSuccessListener { fbSnap ->
                                    val fbDoc = fbSnap.documents.firstOrNull()
                                    if (fbDoc != null) {
                                        hasSubmittedFeedback = true
                                        existingRating = fbDoc.getLong("rating")?.toInt() ?: 0
                                        existingFeedbackText = fbDoc.getString("feedback") ?: ""
                                    } else {
                                        hasSubmittedFeedback = false
                                    }
                                    isLoading = false
                                }
                                .addOnFailureListener {
                                    hasSubmittedFeedback = false
                                    isLoading = false
                                }
                        } else {
                            error = "Failed to load appointment details"
                            isLoading = false
                        }
                    }
                    .addOnFailureListener { e ->
                        error = e.message ?: "Failed to load appointment details"
                        isLoading = false
                    }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load details"
                isLoading = false
            }
        }
    }

    LaunchedEffect(appointmentId) {
        loadData()
    }

    DisposableEffect(Unit) {
        onDispose {
            queuePollingJob?.cancel()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Primary)
            } else if (error != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = error!!, color = Error)
                    Button(onClick = { loadData() }) {
                        Text("Retry")
                    }
                }
            } else if (appointment != null) {
                val appt = appointment!!
                val currentUid = com.example.medplus.data.network.SessionManager.getInstance(context).getUserId()
                val isPatient = currentUid == appt.patientId
                val status = appt.status.trim().uppercase()

                val statusColor = when (status) {
                    "CONFIRMED", "UPCOMING" -> Success
                    "IN_PROGRESS" -> Warning
                    "COMPLETED" -> Primary
                    "CANCELLED" -> Error
                    else -> SecondaryText
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Status Alert Banner if In Progress
                    if (status == "IN_PROGRESS") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Warning)
                                Text(
                                    "Your consultation is currently in progress. Please enter the doctor's room.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = PrimaryText
                                )
                            }
                        }
                    }

                    // Doctor Card Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DetailItem(
                                icon = Icons.Outlined.Person,
                                label = "DOCTOR",
                                value = "Dr. ${appt.doctorName}",
                                valueColor = Primary
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                            DetailItem(
                                icon = Icons.Outlined.LocalHospital,
                                label = "DEPARTMENT",
                                value = appt.department
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                            DetailItem(
                                icon = Icons.Outlined.AssignmentInd,
                                label = "PATIENT NAME",
                                value = appt.patientName.ifBlank { "Patient" }
                            )
                            consultationFee?.let { fee ->
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                                DetailItem(
                                    icon = Icons.Outlined.AttachMoney,
                                    label = "CONSULTATION FEE",
                                    value = "$${String.format(Locale.US, "%.2f", fee)}"
                                )
                            }
                        }
                    }

                    // Appointment Card Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DetailItem(
                                icon = Icons.Outlined.CalendarToday,
                                label = "APPOINTMENT DATE",
                                value = appt.date
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                            DetailItem(
                                icon = Icons.Outlined.AccessTime,
                                label = "APPOINTMENT TIME",
                                value = appt.time
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                            DetailItem(
                                icon = Icons.Outlined.Info,
                                label = "STATUS",
                                value = appt.status,
                                valueColor = statusColor
                            )
                            if (!appt.tokenNumber.isNullOrEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                                DetailItem(
                                    icon = Icons.Outlined.Token,
                                    label = "TOKEN NUMBER",
                                    value = "#${appt.tokenNumber}",
                                    valueColor = Primary
                                )
                            }
                        }
                    }

                    // Queue status Card (if active and waiting/delayed)
                    if (isQueueActive && (queueStatus == "WAITING" || queueStatus == "DOCTOR_RUNNING_LATE")) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "LIVE QUEUE POSITION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Patients Ahead",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SecondaryText
                                        )
                                        Text(
                                            text = patientsAhead.toString(),
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryText
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Est. Wait Time",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SecondaryText
                                        )
                                        Text(
                                            text = "$estimatedWaitMinutes mins",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryText
                                        )
                                    }
                                }

                                if (queueStatus == "DOCTOR_RUNNING_LATE") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Warning.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Doctor is currently running approximately $estimatedWaitMinutes minutes late.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = PrimaryText
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onViewLiveQueueClick,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Icon(Icons.Default.Queue, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("View Live Queue", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                            }
                        }
                    }

                    // Booking ID Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Appointment ID",
                                style = MaterialTheme.typography.labelMedium,
                                color = SecondaryText
                            )
                            Text(
                                appt.appointmentId,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = PrimaryText
                            )
                        }
                    }

                    // Rating / Feedback Card (Only if Completed and patient)
                    if (status == "COMPLETED" && isPatient) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = if (hasSubmittedFeedback) "YOUR FEEDBACK" else "RATE YOUR EXPERIENCE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (hasSubmittedFeedback) {
                                    // Display stars
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = if (i <= existingRating) Icons.Filled.Star else Icons.Filled.StarOutline,
                                                contentDescription = null,
                                                tint = if (i <= existingRating) Warning else SecondaryText.copy(alpha = 0.3f),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    if (existingFeedbackText.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = existingFeedbackText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PrimaryText
                                        )
                                    }
                                } else {
                                    // Input rating stars
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 1..5) {
                                            IconButton(
                                                onClick = { userRating = i },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (i <= userRating) Icons.Filled.Star else Icons.Filled.StarOutline,
                                                    contentDescription = "Rate $i Star",
                                                    tint = if (i <= userRating) Warning else SecondaryText.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = userFeedbackText,
                                        onValueChange = { userFeedbackText = it },
                                        placeholder = { Text("Write your review (optional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Primary,
                                            unfocusedBorderColor = Outline
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            if (userRating == 0) {
                                                Toast.makeText(context, "Please select a star rating", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            isSubmittingFeedback = true
                                            val doctorRepo = com.example.medplus.repository.DoctorRepository()
                                            doctorRepo.addDoctorFeedback(
                                                doctorId = appt.doctorId,
                                                rating = userRating,
                                                feedback = userFeedbackText,
                                                appointmentId = appt.appointmentId,
                                                onSuccess = {
                                                    isSubmittingFeedback = false
                                                    Toast.makeText(context, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                                                    loadData()
                                                },
                                                onFailure = { errorMsg ->
                                                    isSubmittingFeedback = false
                                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        enabled = !isSubmittingFeedback
                                    ) {
                                        if (isSubmittingFeedback) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                        } else {
                                            Text("Submit Review", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cancel Appointment Button for patients (Only if Upcoming/Confirmed)
                    if (isPatient && (status == "CONFIRMED" || status == "UPCOMING")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isCancelling = true
                                appointmentRepo.cancelAppointment(
                                    appointmentId = appt.appointmentId,
                                    onSuccess = {
                                        isCancelling = false
                                        Toast.makeText(context, "Appointment cancelled successfully.", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    },
                                    onFailure = { errorMsg ->
                                        isCancelling = false
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Error),
                            enabled = !isCancelling
                        ) {
                            if (isCancelling) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cancel Appointment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0D1B3E)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF0F4FF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    fontSize = 16.sp
                )
            )
        }
    }
}
