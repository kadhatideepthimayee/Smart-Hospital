package com.example.medplus.ui.doctor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.DoctorProfileViewModel
import com.example.medplus.viewmodel.DocumentUploadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorVerificationDocumentsScreen(
    onContinue: () -> Unit,
    onBackClick: () -> Unit,
    onSubmissionSuccess: () -> Unit,
    viewModel: DoctorProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val isDocumentsUploaded = uiState.registrationCertificateState.isUploaded && 
            uiState.qualificationCertificateState.isUploaded &&
            !uiState.registrationCertificateState.isUploading &&
            !uiState.qualificationCertificateState.isUploading

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onSubmissionSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Verification Documents",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Professional Verification",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Text(
                    "Upload the documents required to verify your medical credentials. Maximum size: 300 KB.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                )
            }

            DocumentUploadCard(
                title = "Medical Registration Certificate",
                state = uiState.registrationCertificateState,
                onUpload = { uri -> viewModel.uploadDocument(uri, "registration") }
            )

            DocumentUploadCard(
                title = "Medical Qualification Certificate",
                state = uiState.qualificationCertificateState,
                onUpload = { uri -> viewModel.uploadDocument(uri, "qualification") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = isDocumentsUploaded
            ) {
                Text(
                    "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun DocumentUploadCard(
    title: String,
    state: DocumentUploadState,
    onUpload: (android.net.Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onUpload(it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryText
                )
                Text(
                    when {
                        state.isUploading -> "Uploading..."
                        state.isUploaded -> "Uploaded"
                        state.error != null -> "Upload failed"
                        else -> "Required"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        state.isUploaded -> Success
                        state.error != null -> Error
                        else -> SecondaryText
                    }
                )
            }

            Box(contentAlignment = Alignment.Center) {
                when {
                    state.isUploading -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Primary)
                    }
                    state.isUploaded -> {
                        IconButton(onClick = { launcher.launch("*/*") }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Replace", tint = Success)
                        }
                    }
                    state.error != null -> {
                        IconButton(onClick = { launcher.launch("*/*") }) {
                            Icon(Icons.Default.Error, contentDescription = "Retry", tint = Error)
                        }
                    }
                    else -> {
                        IconButton(onClick = { launcher.launch("*/*") }) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = Primary)
                        }
                    }
                }
            }
        }
        
        if (state.error != null) {
            Text(
                state.error,
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
            )
        }
    }
}
