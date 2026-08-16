package com.example.medplus.ui.patient

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medplus.dashboard.model.PatientDashboardUiState

// ════════════════════════════════════════════════════════════════════════════
//  COLORS (MedPlus Theme)
// ════════════════════════════════════════════════════════════════════════════

private object PC {
    val Primary = Color(0xFF0B3D91)
    val PrimaryLight = Color(0xFF1565C0)
    val Background = Color(0xFFF8FAFF)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1C1E)
    val TextSec = Color(0xFF5A6A8A)
    val Outline = Color(0xFFE1E5F2)
    val Error = Color(0xFFC62828)
    val GradStart = Color(0xFF0B3D91)
    val GradEnd = Color(0xFF1976D2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: PatientDashboardUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSaveChanges: (fullName: String, phone: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var fullName by remember(uiState.patientName) { mutableStateOf(uiState.patientName) }
    var phone by remember(uiState.phone) { mutableStateOf(uiState.phone) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        TextButton(onClick = { isEditing = true }) {
                            Text("Edit", color = PC.Primary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Cancel", color = PC.TextSec)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PC.Surface)
            )
        },
        containerColor = PC.Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PC.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // PROFILE PHOTO SECTION
                ProfilePhotoSection(uiState.patientName, uiState.profileImageUrl)

            Spacer(Modifier.height(32.dp))

            // INFO CARDS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PC.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EditableInfoField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        isEditing = isEditing,
                        icon = Icons.Outlined.Person
                    )

                    InfoField(
                        label = "Email Address",
                        value = uiState.email,
                        icon = Icons.Outlined.Email
                    )

                    EditableInfoField(
                        label = "Phone Number",
                        value = phone,
                        onValueChange = { phone = it },
                        isEditing = isEditing,
                        icon = Icons.Outlined.Phone
                    )

                    InfoField(
                        label = "Account Role",
                        value = uiState.role,
                        icon = Icons.Outlined.Badge
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ACTIONS
            if (isEditing) {
                Button(
                    onClick = {
                        onSaveChanges(fullName, phone)
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PC.Primary)
                ) {
                    Text("Save Changes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, PC.Error.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PC.Error)
                ) {
                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
    }
}

@Composable
private fun ProfilePhotoSection(name: String, imageUrl: String?) {
    val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PC.GradStart, PC.GradEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            // Initials Fallback
            Text(
                text = initials,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )
            
            // Edit Overlay Icon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, PC.Outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = PC.Primary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = PC.OnSurface)
        )
        Text(
            text = "MedPlus Patient",
            style = MaterialTheme.typography.bodyMedium.copy(color = PC.TextSec)
        )
    }
}

@Composable
private fun InfoField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PC.Background),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = PC.Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = PC.TextSec)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = PC.OnSurface)
        }
    }
}

@Composable
private fun EditableInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PC.Background),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = PC.Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = PC.TextSec)
            if (isEditing) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = PC.Primary,
                        unfocusedIndicatorColor = PC.Outline
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            } else {
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = PC.OnSurface)
            }
        }
    }
}
