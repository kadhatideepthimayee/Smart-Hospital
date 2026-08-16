package com.example.medplus.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.PatientMedicalRecordsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMedicalRecordDetailsScreen(
    recordId: String,
    onBackClick: () -> Unit,
    viewModel: PatientMedicalRecordsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recordId) {
        viewModel.loadRecordDetails(recordId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Record Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
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
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            uiState.errorMessage != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage ?: "Failed to load record details", color = Error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadRecordDetails(recordId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.selectedRecord != null -> {
                val record = uiState.selectedRecord!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Person, null, tint = Primary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "Dr. ${record.doctorName}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryText
                                    )
                                    Text(
                                        "Consultation Date: ${record.createdAt.split("T").firstOrNull() ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryText
                                    )
                                }
                            }
                        }
                    }

                    // Record details card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            RecordDetailField(
                                icon = Icons.Outlined.Description,
                                label = "DIAGNOSIS",
                                value = record.diagnosis
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                            RecordDetailField(
                                icon = Icons.Outlined.Assignment,
                                label = "PRESCRIPTION",
                                value = record.prescription
                            )
                            if (record.notes.isNotEmpty()) {
                                HorizontalDivider(thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                                RecordDetailField(
                                    icon = Icons.Outlined.StickyNote2,
                                    label = "DOCTOR NOTES",
                                    value = record.notes
                                )
                            }
                            if (record.followUpDate.isNotEmpty()) {
                                HorizontalDivider(thickness = 0.5.dp, color = Outline.copy(alpha = 0.5f))
                                RecordDetailField(
                                    icon = Icons.Outlined.AccessTime,
                                    label = "FOLLOW-UP DATE",
                                    value = record.followUpDate,
                                    valueColor = Primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordDetailField(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = PrimaryText
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SecondaryText)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
        }
    }
}
