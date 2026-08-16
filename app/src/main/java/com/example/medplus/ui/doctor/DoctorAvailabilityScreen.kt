package com.example.medplus.ui.doctor

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.DoctorAvailabilityViewModel
import java.util.*

private object DAC {
    val Primary = Color(0xFF0B3D91)
    val Background = Color(0xFFF8FAFF)
    val Surface = Color(0xFFFFFFFF)
    val PrimaryText = Color(0xFF0D1B3E)
    val SecondaryText = Color(0xFF5A6A8A)
    val Outline = Color(0xFFE1E5F2)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoctorAvailabilityScreen(
    viewModel: DoctorAvailabilityViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var consultationFee by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("05:00 PM") }
    var lunchStartTime by remember { mutableStateOf("") }
    var lunchEndTime by remember { mutableStateOf("") }
    var breakStartTime by remember { mutableStateOf("") }
    var breakEndTime by remember { mutableStateOf("") }
    var slotDuration by remember { mutableIntStateOf(15) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            consultationFee = if (uiState.consultationFee > 0) uiState.consultationFee.toString() else ""
            selectedDays = uiState.workingDays.toSet()
            startTime = uiState.startTime
            endTime = uiState.endTime
            lunchStartTime = uiState.lunchStartTime
            lunchEndTime = uiState.lunchEndTime
            breakStartTime = uiState.breakStartTime
            breakEndTime = uiState.breakEndTime
            slotDuration = uiState.slotDuration
        }
    }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            android.widget.Toast.makeText(context, "Availability updated successfully", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    fun showTimePicker(initialTime: String, onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        // Simple parsing for initial time (HH:mm AM/PM)
        try {
            val parts = initialTime.split(" ", ":")
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val amPm = parts[2]
            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        } catch (e: Exception) {}

        TimePickerDialog(
            context,
            { _, hour, minute ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                val format = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
                onTimeSelected(format.format(cal.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Availability & Schedule", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Set your consultation hours", style = MaterialTheme.typography.labelSmall, color = DAC.SecondaryText)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DAC.Surface)
            )
        },
        containerColor = DAC.Background
    ) { padding ->
        if (uiState.isLoading && uiState.workingDays.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DAC.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section 1: Working Days
                PracticeSection(title = "Working Days", icon = Icons.Outlined.CalendarToday) {
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        days.forEach { day ->
                            val isSelected = selectedDays.contains(day) || selectedDays.contains(day.take(3))
                            // Handle both full names and abbreviations
                            val normalizedDay = day.take(3) 
                            
                            Surface(
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays.filter { it != day && it != normalizedDay }.toSet()
                                    } else {
                                        selectedDays + normalizedDay
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) DAC.Primary.copy(alpha = 0.05f) else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, DAC.Primary.copy(alpha = 0.5f)) else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedDays = if (checked) selectedDays + normalizedDay else selectedDays.filter { it != normalizedDay && it != day }.toSet()
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = DAC.Primary)
                                    )
                                    Text(day, style = MaterialTheme.typography.bodyLarge, color = if (isSelected) DAC.PrimaryText else DAC.SecondaryText)
                                    if (isSelected) {
                                        Spacer(Modifier.weight(1f))
                                        Icon(Icons.Default.Check, null, tint = DAC.Primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Consultation Hours
                PracticeSection(title = "Consultation Time", icon = Icons.Outlined.Schedule) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimeDisplayField(
                            label = "Start Time",
                            value = startTime,
                            onClick = { showTimePicker(startTime) { startTime = it } },
                            modifier = Modifier.weight(1f)
                        )
                        TimeDisplayField(
                            label = "End Time",
                            value = endTime,
                            onClick = { showTimePicker(endTime) { endTime = it } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 3: Lunch Break
                PracticeSection(title = "Lunch Break", icon = Icons.Outlined.Restaurant) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimeDisplayField(
                            label = "Start Time",
                            value = lunchStartTime.ifBlank { "None" },
                            onClick = { showTimePicker(if (lunchStartTime.isNotBlank()) lunchStartTime else "01:00 PM") { lunchStartTime = it } },
                            modifier = Modifier.weight(1f)
                        )
                        TimeDisplayField(
                            label = "End Time",
                            value = lunchEndTime.ifBlank { "None" },
                            onClick = { showTimePicker(if (lunchEndTime.isNotBlank()) lunchEndTime else "02:00 PM") { lunchEndTime = it } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (lunchStartTime.isNotBlank()) {
                        TextButton(onClick = { lunchStartTime = ""; lunchEndTime = "" }) {
                            Text("Clear Lunch Break", color = Color.Red)
                        }
                    }
                }

                // Section 4: Coffee/Other Break (Optional)
                PracticeSection(title = "Other Break (Optional)", icon = Icons.Outlined.Coffee) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimeDisplayField(
                            label = "Start Time",
                            value = breakStartTime.ifBlank { "None" },
                            onClick = { showTimePicker(if (breakStartTime.isNotBlank()) breakStartTime else "04:00 PM") { breakStartTime = it } },
                            modifier = Modifier.weight(1f)
                        )
                        TimeDisplayField(
                            label = "End Time",
                            value = breakEndTime.ifBlank { "None" },
                            onClick = { showTimePicker(if (breakEndTime.isNotBlank()) breakEndTime else "04:15 PM") { breakEndTime = it } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (breakStartTime.isNotBlank()) {
                        TextButton(onClick = { breakStartTime = ""; breakEndTime = "" }) {
                            Text("Clear Other Break", color = Color.Red)
                        }
                    }
                }

                // Section 5: Appointment Duration
                PracticeSection(title = "Slot Duration", icon = Icons.Outlined.Timer) {
                    val durations = listOf(15, 30, 45, 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        durations.forEach { duration ->
                            val isSelected = slotDuration == duration
                            Surface(
                                onClick = { slotDuration = duration },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) DAC.Primary else DAC.Surface,
                                border = BorderStroke(1.dp, if (isSelected) DAC.Primary else DAC.Outline),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$duration\nmin",
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else DAC.PrimaryText
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Fees
                PracticeSection(title = "Consultation Fee", icon = Icons.Outlined.Payments) {
                    OutlinedTextField(
                        value = consultationFee,
                        onValueChange = { consultationFee = it },
                        label = { Text("Fee (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = { Icon(Icons.Outlined.Payments, null, tint = DAC.Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DAC.Primary,
                            unfocusedBorderColor = DAC.Outline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.updatePracticeDetails(
                            consultationFee = consultationFee.toDoubleOrNull() ?: 0.0,
                            workingDays = selectedDays.toList(),
                            startTime = startTime,
                            endTime = endTime,
                            lunchStart = lunchStartTime,
                            lunchEnd = lunchEndTime,
                            breakStart = breakStartTime,
                            breakEnd = breakEndTime,
                            slotDuration = slotDuration
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DAC.Primary),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ErrorOutline, null, tint = Color(0xFFDC2626))
                            Spacer(Modifier.width(12.dp))
                            Text(text = uiState.errorMessage!!, color = Color(0xFFDC2626), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun PracticeSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(icon, null, tint = DAC.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DAC.PrimaryText
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = DAC.Surface),
            border = BorderStroke(1.dp, DAC.Outline.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun TimeDisplayField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = DAC.SecondaryText, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = DAC.Surface,
            border = BorderStroke(1.dp, DAC.Outline)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                Icon(Icons.Outlined.AccessTime, null, tint = DAC.Primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
