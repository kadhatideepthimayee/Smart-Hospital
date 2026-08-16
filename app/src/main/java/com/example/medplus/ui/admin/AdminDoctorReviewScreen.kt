package com.example.medplus.ui.admin

import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.ui.theme.*
import com.example.medplus.viewmodel.AdminDoctorReviewViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorReviewScreen(
    doctorUid: String,
    onBackClick: () -> Unit,
    onActionSuccess: () -> Unit,
    viewModel: AdminDoctorReviewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showRejectDialog by remember { mutableStateOf(false) }
    var previewData by remember { mutableStateOf<Pair<String, String>?>(null) } // mimeType, base64

    LaunchedEffect(doctorUid) {
        viewModel.loadDoctorDetails(doctorUid)
    }

    LaunchedEffect(uiState.isActionSuccess) {
        if (uiState.isActionSuccess) {
            android.widget.Toast.makeText(context, uiState.successMessage ?: "Action successful", android.widget.Toast.LENGTH_SHORT).show()
            onActionSuccess()
        }
    }

    if (showRejectDialog) {
        RejectDoctorDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                viewModel.rejectDoctor(doctorUid, reason)
                showRejectDialog = false
            }
        )
    }

    if (previewData != null) {
        ImagePreviewDialog(
            mimeType = previewData!!.first,
            base64Data = previewData!!.second,
            onDismiss = { previewData = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Doctor Profile", fontWeight = FontWeight.Bold) },
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
        if (uiState.isLoading && uiState.doctor == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.errorMessage != null) {
            AdminReviewErrorState(message = uiState.errorMessage!!, onRetry = { viewModel.loadDoctorDetails(doctorUid) })
        } else if (uiState.doctor != null) {
            val doctor = uiState.doctor!!
            
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. DOCTOR INFORMATION
                    ReviewSection(title = "DOCTOR INFORMATION", icon = Icons.Outlined.Person) {
                        ProfileInfoRow(label = "Full Name", value = doctor.fullName)
                        ProfileInfoRow(label = "Email", value = doctor.email)
                        ProfileInfoRow(label = "Phone Number", value = doctor.phone)
                        ProfileInfoRow(label = "Department", value = doctor.department)
                        ProfileInfoRow(label = "Specialization", value = doctor.specialization)
                        ProfileInfoRow(label = "Medical Registration Number", value = doctor.registrationNumber)
                        ProfileInfoRow(label = "Qualification", value = doctor.qualification)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Verification Status", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                            AdminStatusBadge(doctor.verificationStatus)
                        }

                        ProfileInfoRow(label = "Submitted Date", value = adminFormatDate(doctor.submittedAt.toDate()))
                    }

                    // 2. PROFESSIONAL (Existing was a bit redundant, merged into Doctor Info as per req)

                    // 3. DOCUMENTS SECTION
                    ReviewSection(title = "DOCUMENTS SECTION", icon = Icons.Outlined.Description) {
                        DocumentReviewItem(
                            label = "Medical Registration Certificate",
                            dataUri = doctor.registrationCertificateUrl,
                            onViewImage = { mime, data -> previewData = mime to data }
                        )
                        DocumentReviewItem(
                            label = "Medical Qualification Certificate",
                            dataUri = doctor.verificationDocumentUrl,
                            onViewImage = { mime, data -> previewData = mime to data }
                        )
                    }
                }

                // BOTTOM ACTIONS
                if (doctor.verificationStatus.trim().uppercase() == "PENDING") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = AppShapes.medium,
                                border = BorderStroke(1.dp, Error),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                            ) {
                                Text("Reject", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.approveDoctor(doctorUid) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = AppShapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Approve Doctor", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReviewErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = Error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun ReviewSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 1.sp
                    )
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = PrimaryText)
    }
}

@Composable
fun RejectDoctorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reason for Rejection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Please provide a reason for rejecting this registration request.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = reason,
                    onValueChange = { 
                        reason = it
                        if (it.isNotBlank()) error = null
                    },
                    label = { Text("Rejection Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    minLines = 3,
                    shape = AppShapes.medium
                )
                if (error != null) {
                    Text(error!!, color = Error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        error = "Reason is required"
                    } else {
                        onConfirm(reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text("Reject", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Surface,
        shape = AppShapes.large
    )
}

@Composable
fun DocumentReviewItem(
    label: String,
    dataUri: String,
    onViewImage: (String, String) -> Unit
) {
    val context = LocalContext.current
    val isValid = dataUri.startsWith("data:") && dataUri.contains(";base64,")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Background.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = PrimaryText, fontWeight = FontWeight.Bold)
                Text(
                    if (isValid) "Document Available" else if (dataUri.isBlank()) "No Document Uploaded" else "Invalid Document Format",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isValid) Success else Error
                )
            }
            
            if (isValid) {
                Button(
                    onClick = {
                        val commaIndex = dataUri.indexOf(",")
                        val header = dataUri.substring(0, commaIndex)
                        val base64Data = dataUri.substring(commaIndex + 1)
                        val mimeType = header.substringAfter("data:").substringBefore(";base64")

                        if (mimeType.startsWith("image/")) {
                            onViewImage(mimeType, base64Data)
                        } else {
                            openUrl(context, dataUri)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("View Document", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(
    mimeType: String,
    base64Data: String,
    onDismiss: () -> Unit
) {
    val bitmap = remember(base64Data) {
        try {
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Document Preview", fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Document Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Text("Failed to load image", color = Error)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

fun adminFormatDate(date: Date): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
}

@Composable
fun DocumentLinkItem(label: String, path: String) {
    val context = LocalContext.current
    var isResolving by remember { mutableStateOf(false) }

    Surface(
        onClick = {
            if (path.isNotBlank() && !isResolving) {
                openUrl(context, path)
            }
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = Background.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        enabled = path.isNotBlank()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = PrimaryText)
            }
            if (isResolving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else if (path.isNotBlank()) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
            } else {
                Text("Not Provided", style = MaterialTheme.typography.labelSmall, color = Error)
            }
        }
    }
}

fun openUrl(context: android.content.Context, url: String) {
    if (url.isBlank()) return
    try {
        if (url.startsWith("data:")) {
            // 1. Handle Base64 Data URI
            val commaIndex = url.indexOf(",")
            if (commaIndex == -1) return
            
            val header = url.substring(0, commaIndex)
            val base64Data = url.substring(commaIndex + 1)
            val mimeType = header.substringAfter("data:").substringBefore(";base64")
            
            val extension = when (mimeType) {
                "application/pdf" -> ".pdf"
                "image/jpeg" -> ".jpg"
                "image/png" -> ".png"
                else -> ""
            }
            
            // 2. Decode Base64 to bytes
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            
            // 3. Create a temporary file in the cache directory
            val tempFile = File(context.cacheDir, "doc_${System.currentTimeMillis()}$extension")
            FileOutputStream(tempFile).use { fos ->
                fos.write(bytes)
            }
            
            // 4. Get a content URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            
            // 5. Open the file with an appropriate Intent
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            // Handle regular http/https URLs for backward compatibility
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminDoctorReview", "Error opening document", e)
        android.widget.Toast.makeText(context, "Failed to open document", android.widget.Toast.LENGTH_SHORT).show()
    }
}
