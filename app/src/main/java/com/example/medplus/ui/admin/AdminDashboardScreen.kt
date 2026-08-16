package com.example.medplus.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.AdminDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onDoctorClick: (String) -> Unit,
    onPendingClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onPatientsClick: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var doctorToReject by remember { mutableStateOf<DoctorProfile?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("ADMIN_DEBUG", "AdminDashboardScreen ON_RESUME - Refreshing data")
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

    if (doctorToReject != null) {
        AlertDialog(
            onDismissRequest = { doctorToReject = null },
            title = { Text("Reject Doctor?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reject Dr. ${doctorToReject?.fullName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectDoctor(doctorToReject!!.uid, doctorToReject!!.fullName)
                        doctorToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { doctorToReject = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface,
            shape = AppShapes.large
        )
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                onProfileClick = onProfileClick,
                onNotificationClick = onNotificationClick,
                unreadCount = uiState.unreadNotificationsCount
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading && uiState.doctors.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.errorMessage != null) {
            AdminDashboardErrorState(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.loadDashboardData() },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Welcome Section
                item {
                    DashboardWelcomeSection(uiState.adminName)
                }

                // 2. Quick Actions (The Four Main Sections)
                item {
                    DashboardQuickActionsSection(
                        onPendingClick = onPendingClick,
                        onPatientsClick = onPatientsClick,
                        onNotificationClick = onNotificationClick,
                        onProfileClick = onProfileClick
                    )
                }

                // 3. Pending Doctor Requests Header
                item {
                    DashboardSectionHeader(
                        title = "Pending Doctor Verifications",
                        subtitle = "Review new doctor registrations",
                        onViewAllClick = onPendingClick
                    )
                }

                if (uiState.pendingDoctors.isEmpty()) {
                    item {
                        DashboardEmptyState("No pending verification requests.")
                    }
                } else {
                    items(uiState.pendingDoctors) { doctor ->
                        DashboardDoctorRequestCard(
                            doctor = doctor,
                            onClick = { onDoctorClick(doctor.uid) }
                        )
                    }
                }

                // 4. Approved Doctors Section
                item {
                    DashboardSectionHeader(
                        title = "Approved Doctors",
                        subtitle = "Currently active medical professionals",
                        onViewAllClick = null
                    )
                }

                if (uiState.approvedDoctors.isEmpty()) {
                    item {
                        DashboardEmptyState("No approved doctors.")
                    }
                } else {
                    items(uiState.approvedDoctors) { doctor ->
                        ApprovedDoctorCard(
                            doctor = doctor,
                            onViewClick = { onDoctorClick(doctor.uid) },
                            onRejectClick = { doctorToReject = doctor }
                        )
                    }
                }

                // 5. Rejected Doctors Section
                item {
                    DashboardSectionHeader(
                        title = "Rejected Doctors",
                        subtitle = "Applications that were not verified",
                        onViewAllClick = null
                    )
                }

                if (uiState.rejectedDoctors.isEmpty()) {
                    item {
                        DashboardEmptyState("No rejected doctors.")
                    }
                } else {
                    items(uiState.rejectedDoctors) { doctor ->
                        RejectedDoctorCard(
                            doctor = doctor,
                            onViewClick = { onDoctorClick(doctor.uid) }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun DashboardSectionHeader(
    title: String,
    subtitle: String,
    onViewAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PrimaryText
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
        }
        if (onViewAllClick != null) {
            TextButton(onClick = onViewAllClick) {
                Text("View All")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    unreadCount: Int = 0
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "MedPlus",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                    Text(
                        "Admin Portal",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge {
                                Text(unreadCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = SecondaryText)
                }
            }
            IconButton(onClick = onProfileClick) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Primary, modifier = Modifier.size(20.dp))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
        modifier = Modifier.shadow(4.dp)
    )
}

@Composable
private fun DashboardWelcomeSection(adminName: String) {
    Column {
        Text(
            text = "Welcome, $adminName",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryText
        )
        Text(
            text = "Manage your hospital platform efficiently.",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )
    }
}

@Composable
private fun DashboardQuickActionsSection(
    onPendingClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Quick Management",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryText
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardQuickActionItem(
                title = "Doctor Verif.",
                icon = Icons.Default.FactCheck,
                color = Color(0xFF673AB7),
                onClick = onPendingClick,
                modifier = Modifier.weight(1f)
            )
            DashboardQuickActionItem(
                title = "Patients",
                icon = Icons.Default.People,
                color = Color(0xFF009688),
                onClick = onPatientsClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardQuickActionItem(
                title = "Notifications",
                icon = Icons.Default.Notifications,
                color = Color(0xFFF57F17),
                onClick = onNotificationClick,
                modifier = Modifier.weight(1f)
            )
            DashboardQuickActionItem(
                title = "My Profile",
                icon = Icons.Default.AccountCircle,
                color = Color(0xFF1976D2),
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardQuickActionItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.medium,
        color = Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Outline.copy(alpha = 0.5f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = PrimaryText,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DashboardDoctorRequestCard(
    doctor: DoctorProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        doctor.fullName.take(1).uppercase(),
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        doctor.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                    Text(
                        doctor.specialization.ifBlank { doctor.department },
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }

                AdminStatusBadge(doctor.verificationStatus)
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
                        "Registration No.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                    Text(
                        doctor.registrationNumber,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = PrimaryText
                    )
                }
                
                Button(
                    onClick = onClick,
                    shape = AppShapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Review", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ApprovedDoctorCard(
    doctor: DoctorProfile,
    onViewClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Success.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(doctor.fullName.take(1).uppercase(), color = Success, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(doctor.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(doctor.specialization.ifBlank { doctor.department }, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                }
                AdminStatusBadge(doctor.verificationStatus)
            }
            
            Spacer(Modifier.height(12.dp))
            Text("Registration: ${doctor.registrationNumber}", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
            
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.small
                ) {
                    Text("View")
                }
                Button(
                    onClick = onRejectClick,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
fun RejectedDoctorCard(
    doctor: DoctorProfile,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Error.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(doctor.fullName.take(1).uppercase(), color = Error, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(doctor.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(doctor.specialization.ifBlank { doctor.department }, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                }
                AdminStatusBadge(doctor.verificationStatus)
            }
            
            if (doctor.rejectionReason != null) {
                Spacer(Modifier.height(8.dp))
                Text("Reason: ${doctor.rejectionReason}", style = MaterialTheme.typography.labelSmall, color = Error)
            }
            
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onViewClick,
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.small
            ) {
                Text("View")
            }
        }
    }
}

@Composable
fun AdminStatusBadge(status: String) {
    val normalizedStatus = status.trim().uppercase()
    
    val (color, bgColor, displayText) = when (normalizedStatus) {
        "PENDING" -> Triple(Warning, Warning.copy(alpha = 0.1f), "Pending")
        "APPROVED", "VERIFIED" -> Triple(Success, Success.copy(alpha = 0.1f), "✓ Verified")
        "REJECTED" -> Triple(Error, Error.copy(alpha = 0.1f), "Rejected")
        "DRAFT" -> Triple(SecondaryText, Outline.copy(alpha = 0.1f), "Draft")
        else -> Triple(SecondaryText, Outline.copy(alpha = 0.1f), status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun AdminDashboardErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Error, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Admin Error Occurred",
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun DashboardEmptyState(message: String = "No verification requests found.") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = SecondaryText.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = SecondaryText, textAlign = TextAlign.Center)
    }
}
