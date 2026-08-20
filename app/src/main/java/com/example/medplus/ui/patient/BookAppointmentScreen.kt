package com.example.medplus.ui.patient

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medplus.model.DoctorProfile
import com.example.medplus.viewmodel.BookAppointmentViewModel
import com.example.medplus.viewmodel.TimeSlot
import com.example.medplus.viewmodel.DateItem
import com.example.medplus.viewmodel.BookingStep
import com.example.medplus.ui.theme.*
import com.example.medplus.utils.HospitalDepartments
import android.util.Log

// ════════════════════════════════════════════════════════════════════════════
//  SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    viewModel: BookAppointmentViewModel,
    onBackClick: () -> Unit,
    onViewAppointmentClick: (String) -> Unit,
    onMyAppointmentsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = uiState.currentStep,
        transitionSpec = {
            if (targetState.ordinal > initialState.ordinal) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "BookingStepTransition"
    ) { step ->
        when (step) {
            BookingStep.SELECT_SLOT -> {
                BookingFormContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    onBackClick = onBackClick
                )
            }
            BookingStep.CONFIRM_APPOINTMENT -> {
                ConfirmationContent(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
            BookingStep.BOOKING_SUCCESS -> {
                SuccessContent(
                    uiState = uiState,
                    onViewAppointment = { 
                        uiState.confirmedAppointment?.appointmentId?.let { id ->
                            onViewAppointmentClick(id)
                        }
                    },
                    onMyAppointmentsClick = onMyAppointmentsClick,
                    onBackToDashboard = onBackClick // This usually pops back to dashboard in AppNavigation
                )
            }
        }
    }
}

// ── 1. BOOKING FORM CONTENT ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingFormContent(
    viewModel: BookAppointmentViewModel,
    uiState: com.example.medplus.viewmodel.BookAppointmentUiState,
    onBackClick: () -> Unit
) {
    // ---- Local Selection State (kept for smooth UI interaction) ----------------
    var selectedDepartment by remember { mutableStateOf(uiState.selectedDepartment) }
    var selectedDoctor by remember { mutableStateOf<DoctorProfile?>(uiState.selectedDoctor) }
    var selectedDate by remember { mutableStateOf<DateItem?>(uiState.selectedDate) }
    var selectedTime by remember { mutableStateOf(uiState.selectedTime) }

    // Sync local state variables when the ViewModel's state is updated (for Rescheduling pre-fills)
    LaunchedEffect(uiState.selectedDoctor, uiState.selectedDepartment) {
        if (uiState.selectedDoctor != null) {
            selectedDoctor = uiState.selectedDoctor
            selectedDepartment = uiState.selectedDepartment
        }
    }

    val departments = HospitalDepartments.departments

    // Initial load of all verified doctors
    LaunchedEffect(Unit) {
        if (uiState.doctors.isEmpty()) {
            viewModel.fetchDoctors("")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (uiState.rescheduleId != null) "Reschedule Appointment" else "Book Appointment", style = MaterialTheme.typography.titleLarge, color = PrimaryText)
                        Text(if (uiState.rescheduleId != null) "Select a new date and time for your visit" else "Find a doctor and schedule your visit", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                    }
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SectionContainer(title = "Select Department") {
                    DepartmentSelector(
                        selected = selectedDepartment,
                        options = departments,
                        onSelect = {
                            selectedDepartment = it
                            selectedDoctor = null
                            selectedDate = null
                            selectedTime = ""
                            viewModel.fetchDoctors(it)
                        }
                    )
                }
            }

            item {
                SectionContainer(title = "Select Doctor") {
                    if (uiState.isLoading && uiState.doctors.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else if (uiState.errorMessage != null && uiState.currentStep == BookingStep.SELECT_SLOT) {
                        ErrorState(
                            message = uiState.errorMessage ?: "Unable to load doctors",
                            onRetry = { viewModel.fetchDoctors(selectedDepartment) }
                        )
                    } else if (uiState.doctors.isEmpty() && !uiState.isLoading) {
                        EmptyState(
                            department = selectedDepartment,
                            onRetry = { viewModel.fetchDoctors(selectedDepartment) }
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.doctors.forEach { doctor ->
                                DoctorCard(
                                    doctor = doctor,
                                    isSelected = selectedDoctor?.uid == doctor.uid,
                                    onSelect = {
                                        selectedDoctor = doctor
                                        selectedDate = null
                                        selectedTime = ""
                                        viewModel.generateAvailableDates(doctor)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = selectedDoctor != null) {
                    SectionContainer(title = "Select Date") {
                        if (uiState.availableDates.isEmpty() && !uiState.isLoading) {
                            Text("No available days for this doctor.", style = MaterialTheme.typography.bodyMedium, color = Error, modifier = Modifier.padding(8.dp))
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(uiState.availableDates) { dateItem ->
                                    DateCard(
                                        item = dateItem,
                                        isSelected = selectedDate?.fullDate == dateItem.fullDate,
                                        onSelect = {
                                            selectedDate = dateItem
                                            selectedTime = ""
                                            selectedDoctor?.let { viewModel.generateTimeSlots(it, dateItem.fullDate) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = selectedDate != null) {
                    SectionContainer(title = "Available Time Slots") {
                        if (uiState.availableTimeSlots.isEmpty() && !uiState.isLoading) {
                            Text("Doctor schedule is not available.", style = MaterialTheme.typography.bodyMedium, color = Error, modifier = Modifier.padding(8.dp))
                        } else {
                            TimeSlotGrid(
                                slots = uiState.availableTimeSlots,
                                selectedTime = selectedTime,
                                onTimeSelect = { selectedTime = it }
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = selectedTime.isNotEmpty()) {
                    SectionContainer(title = "Appointment Summary") {
                        SummaryCard(
                            doctor = selectedDoctor?.fullName ?: "",
                            department = selectedDepartment,
                            date = selectedDate?.fullDate ?: "",
                            time = selectedTime
                        )
                    }
                }
            }

            item {
                val isEnabled = selectedDoctor != null && selectedDate != null && selectedTime.isNotEmpty() && !uiState.isLoading
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Button(
                        onClick = { 
                            selectedDoctor?.let { doctor ->
                                selectedDate?.let { date ->
                                    // Use doctor.department instead of selectedDepartment to ensure it's not empty
                                    viewModel.moveToConfirmation(doctor, doctor.department, date, selectedTime)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = isEnabled,
                        shape = AppShapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Confirm Appointment", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
        }
    }
}

// ── 2. CONFIRMATION CONTENT ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationContent(
    viewModel: BookAppointmentViewModel,
    uiState: com.example.medplus.viewmodel.BookAppointmentUiState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Appointment", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Please review your appointment details before confirming.",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ConfirmationDetailItem("Doctor", "Dr. ${uiState.selectedDoctor?.fullName}", Icons.Default.Person)
                    ConfirmationDetailItem("Department", uiState.selectedDepartment, Icons.Default.LocalHospital)
                    ConfirmationDetailItem("Date", uiState.selectedDate?.fullDate ?: "", Icons.Default.CalendarToday)
                    ConfirmationDetailItem("Time", uiState.selectedTime, Icons.Default.AccessTime)
                    
                    Divider(color = Outline.copy(alpha = 0.5f))
                    
                    ConfirmationDetailItem(
                        "Consultation Fee", 
                        "₹${uiState.selectedDoctor?.consultationFee?.toInt() ?: 0}", 
                        Icons.Default.Payments,
                        valueColor = Primary
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.bookAppointment() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = AppShapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Confirm Appointment", style = MaterialTheme.typography.titleMedium)
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = AppShapes.medium,
                    border = BorderStroke(1.dp, Primary)
                ) {
                    Text("Go Back", color = Primary, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// ── 3. SUCCESS CONTENT ───────────────────────────────────────────────────

@Composable
private fun SuccessContent(
    uiState: com.example.medplus.viewmodel.BookAppointmentUiState,
    onViewAppointment: () -> Unit,
    onMyAppointmentsClick: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val doctor = uiState.selectedDoctor
    val appt = uiState.confirmedAppointment
    
    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(Success.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(48.dp))
        }

        Spacer(Modifier.height(16.dp))

        val isRescheduling = uiState.rescheduleId != null
        Text(if (isRescheduling) "Appointment Rescheduled" else "Appointment Confirmed", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PrimaryText, textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            if (isRescheduling) "Your appointment with Dr. ${doctor?.fullName} has been successfully rescheduled." else "Your appointment with Dr. ${doctor?.fullName} has been successfully scheduled.",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Doctor", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    Text("Dr. ${doctor?.fullName ?: ""}", color = PrimaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Department", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    Text(doctor?.department ?: uiState.selectedDepartment, color = PrimaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    Text(uiState.selectedDate?.fullDate ?: "", color = PrimaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Time", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    Text(uiState.selectedTime, color = PrimaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                doctor?.consultationFee?.let { fee ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Consultation Fee", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                        Text("₹${fee.toInt()}", color = Primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
                
                appt?.tokenNumber?.let { token ->
                    Divider(color = Outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Token Number", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                        Text("#$token", color = Primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                appt?.appointmentId?.let { id ->
                    Divider(color = Outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Appointment ID", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                        Text(id, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onViewAppointment,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("View Appointment", style = MaterialTheme.typography.titleSmall)
            }

            Button(
                onClick = onMyAppointmentsClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
            ) {
                Text("Go to My Appointments", style = MaterialTheme.typography.titleSmall)
            }

            OutlinedButton(
                onClick = onBackToDashboard,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = AppShapes.medium,
                border = BorderStroke(1.dp, Primary)
            ) {
                Text("Go to Patient Dashboard", color = Primary, style = MaterialTheme.typography.titleSmall)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SUB-COMPOSABLES & UTILS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun ConfirmationDetailItem(label: String, value: String, icon: ImageVector, valueColor: Color = PrimaryText) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = valueColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SectionContainer(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun DepartmentSelector(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = Surface,
        border = BorderStroke(1.dp, if (selected.isNotEmpty()) Primary else Outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalHospital, null, tint = Primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = selected.ifBlank { "Select Department" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected.isEmpty()) SecondaryText else PrimaryText
                )
            }
            Icon(Icons.Default.KeyboardArrowDown, null, tint = SecondaryText)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { dept ->
                DropdownMenuItem(
                    text = { Text(dept) },
                    onClick = { onSelect(dept); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun DoctorCard(doctor: DoctorProfile, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        color = if (isSelected) SurfaceVariant else Surface,
        border = BorderStroke(1.dp, if (isSelected) Primary else Outline)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(doctor.fullName, style = MaterialTheme.typography.titleMedium, color = PrimaryText)
                Text(doctor.department, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                Text(doctor.qualification, style = MaterialTheme.typography.bodySmall, color = Primary, fontWeight = FontWeight.Medium)
                Text("${doctor.experienceYears} years experience • ${doctor.specialization}", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Consultation: ₹${doctor.consultationFee.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Primary)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = Primary)
            }
        }
    }
}

@Composable
private fun DateCard(item: DateItem, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.width(72.dp),
        shape = AppShapes.medium,
        color = if (isSelected) Primary else Surface,
        border = BorderStroke(1.dp, if (isSelected) Primary else Outline)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(item.dayName, style = MaterialTheme.typography.labelSmall, color = if (isSelected) Color.White.copy(alpha = 0.8f) else SecondaryText)
            Text(item.dayNumber, style = MaterialTheme.typography.titleLarge, color = if (isSelected) Color.White else PrimaryText)
        }
    }
}

@Composable
private fun TimeSlotGrid(slots: List<TimeSlot>, selectedTime: String, onTimeSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slots.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { slot ->
                    val isSelected = selectedTime == slot.time
                    val bgColor = if (isSelected) Primary else if (!slot.isAvailable) Background else Surface
                    val contentColor = if (isSelected) Color.White else if (!slot.isAvailable) SecondaryText.copy(alpha = 0.5f) else PrimaryText
                    
                    Surface(
                        onClick = { if (slot.isAvailable) onTimeSelect(slot.time) },
                        enabled = slot.isAvailable,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = AppShapes.small,
                        color = bgColor,
                        border = if (slot.isAvailable) BorderStroke(1.dp, if (isSelected) Primary else Outline) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(slot.time, style = MaterialTheme.typography.bodyMedium, color = contentColor)
                        }
                    }
                }
                // Add an empty box if the row has only one slot to keep alignment
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(doctor: String, department: String, date: String, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        border = BorderStroke(1.dp, Outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, null, tint = Success, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Appointment Summary", style = MaterialTheme.typography.titleSmall, color = Success)
            }
            Spacer(Modifier.height(12.dp))
            SummaryItem(Icons.Default.Person, doctor)
            SummaryItem(Icons.Default.LocalHospital, department)
            SummaryItem(Icons.Default.CalendarToday, "$date • $time")
        }
    }
}

@Composable
private fun SummaryItem(icon: ImageVector, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = PrimaryText)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = PrimaryText, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, shape = AppShapes.medium, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(department: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.SearchOff, null, tint = SecondaryText, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        val message = if (department.isBlank()) {
            "No verified doctors available."
        } else {
            "No verified doctors available for this department."
        }
        Text(message, style = MaterialTheme.typography.bodyLarge, color = PrimaryText, textAlign = TextAlign.Center)
        Text("Please try another department.", style = MaterialTheme.typography.bodySmall, color = SecondaryText, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry, shape = AppShapes.medium) {
            Text("Retry")
        }
    }
}
