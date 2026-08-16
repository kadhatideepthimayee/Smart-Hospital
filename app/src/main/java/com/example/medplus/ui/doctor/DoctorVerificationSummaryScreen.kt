package com.example.medplus.ui.doctor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import com.example.medplus.model.DoctorProfile
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.DoctorProfileViewModel
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorVerificationSummaryScreen(
    onBackClick: () -> Unit,
    onSubmissionSuccess: () -> Unit,
    viewModel: DoctorProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile ?: DoctorProfile()

    val isProfileComplete = profile.fullName.isNotBlank() && profile.phone.isNotBlank()
    val isProfessionalComplete = profile.qualification.isNotBlank() && 
            profile.specialization.isNotBlank() && 
            profile.department.isNotBlank() && 
            profile.registrationNumber.isNotBlank()
    val isDocumentsComplete = profile.registrationCertificateUrl.isNotBlank() && 
            profile.verificationDocumentUrl.isNotBlank()

    val isAllComplete = isProfileComplete && isProfessionalComplete && isDocumentsComplete

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onSubmissionSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            android.widget.Toast.makeText(
                com.google.firebase.FirebaseApp.getInstance().applicationContext,
                error,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Review your information before submitting for verification.",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )

            SummarySection(
                title = "Profile Information",
                isComplete = isProfileComplete
            )

            SummarySection(
                title = "Professional Information",
                isComplete = isProfessionalComplete
            )

            SummarySection(
                title = "Verification Documents",
                isComplete = isDocumentsComplete
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!isAllComplete) {
                Surface(
                    color = Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Error.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Error)
                        Text(
                            "Please complete all sections before submitting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error
                        )
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.submitForVerification()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = isAllComplete && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        "Submit for Verification",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SummarySection(
    title: String,
    isComplete: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryText
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isComplete) Success else Warning,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    if (isComplete) "Complete" else "Incomplete",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isComplete) Success else Warning
                )
            }
        }
    }
}
