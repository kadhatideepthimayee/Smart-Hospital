package com.example.medplus.ui.admin

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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.AdminDoctorVerificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorVerificationScreen(
    onBackClick: () -> Unit,
    onReviewClick: (String) -> Unit,
    viewModel: AdminDoctorVerificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Doctor Verification",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryText
                        )
                        Text(
                            text = "Review professional registration requests",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
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
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Filter Tabs
            VerificationFilterTabs(
                selectedFilter = uiState.currentFilter,
                onFilterSelected = { viewModel.loadDoctors(it) }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else if (uiState.errorMessage != null) {
                    ErrorState(
                        message = uiState.errorMessage ?: "Unable to load requests",
                        onRetry = { viewModel.refresh() }
                    )
                } else if (uiState.doctors.isEmpty()) {
                    EmptyState(filter = uiState.currentFilter)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.doctors, key = { it.uid }) { doctor ->
                            DoctorRequestCard(
                                doctor = doctor,
                                onReviewClick = { onReviewClick(doctor.uid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationFilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("PENDING", "VERIFIED", "REJECTED")
    
    TabRow(
        selectedTabIndex = filters.indexOf(selectedFilter),
        containerColor = Surface,
        contentColor = Primary,
        divider = { HorizontalDivider(color = Outline.copy(alpha = 0.5f)) }
    ) {
        filters.forEach { filter ->
            Tab(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = if (filter == "VERIFIED") "Verified" else filter.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                unselectedContentColor = SecondaryText
            )
        }
    }
}

@Composable
fun DoctorRequestCard(
    doctor: DoctorProfile,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image Placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dr. ${doctor.fullName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                    Text(
                        text = "${doctor.specialization} • ${doctor.department}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                
                StatusBadge(status = doctor.verificationStatus)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Submitted Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                    Text(
                        text = formatDate(doctor.submittedAt.toDate()),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = PrimaryText
                    )
                }
                
                Button(
                    onClick = onReviewClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Review", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, bgColor) = when (status) {
        "PENDING" -> Warning to Warning.copy(alpha = 0.1f)
        "APPROVED" -> Success to Success.copy(alpha = 0.1f)
        "REJECTED" -> Error to Error.copy(alpha = 0.1f)
        else -> SecondaryText to Outline.copy(alpha = 0.1f)
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
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = Error.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun EmptyState(filter: String) {
    val message = when (filter) {
        "PENDING" -> "No pending verification requests."
        "VERIFIED" -> "No verified doctors found."
        "REJECTED" -> "No rejected requests found."
        else -> "No requests found."
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = SecondaryText.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = SecondaryText)
    }
}

fun formatDate(date: Date): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
}
