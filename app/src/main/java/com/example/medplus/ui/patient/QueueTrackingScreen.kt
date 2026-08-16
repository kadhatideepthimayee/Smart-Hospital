package com.example.medplus.ui.patient

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medplus.dashboard.model.CrowdLevel
import com.example.medplus.dashboard.model.LiveQueueInfo
import com.example.medplus.dashboard.model.PatientDashboardUiState

// ════════════════════════════════════════════════════════════════════════════
//  COLORS (MedPlus Professional Theme)
// ════════════════════════════════════════════════════════════════════════════

private object QC {
    val Primary = Color(0xFF0B3D91)
    val PrimaryLight = Color(0xFF1565C0)
    val Secondary = Color(0xFF00897B)
    val Background = Color(0xFFF8FAFF)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1C1E)
    val TextSec = Color(0xFF5A6A8A)
    val Outline = Color(0xFFE1E5F2)
    val Success = Color(0xFF2E7D32)
    val Warning = Color(0xFFF57F17)
    val Error = Color(0xFFC62828)
    val GradStart = Color(0xFF0B3D91)
    val GradEnd = Color(0xFF1976D2)
}

// ════════════════════════════════════════════════════════════════════════════
//  SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueTrackingScreen(
    uiState: PatientDashboardUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val queue = uiState.liveQueue

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Live Queue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = QC.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = QC.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = QC.Surface
                )
            )
        },
        containerColor = QC.Background
    ) { paddingValues ->
        if (queue == null || !queue.isActive) {
            NoActiveQueue(onBackClick, Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // LIVE STATUS INDICATOR
                LivePulseIndicator()

                // MAIN TICKET CARD
                YourTokenCard(queue)

                // PROGRESS INDICATOR
                QueueProgressSection(queue)

                // INFORMATION GRID
                InfoGridSection(queue)

                Spacer(Modifier.height(8.dp))

                // ACTION BUTTON
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QC.Primary)
                ) {
                    Text(
                        "Done",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun LivePulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FiberManualRecord,
            contentDescription = null,
            tint = QC.Success,
            modifier = Modifier
                .size(12.dp)
                .alpha(alpha)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "LIVE UPDATES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = QC.Success,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
private fun YourTokenCard(queue: LiveQueueInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = QC.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Token",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = QC.TextSec,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "#${queue.queueNumber}",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = QC.Primary
                )
            )
            
            if (queue.department != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = queue.department,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = QC.PrimaryLight,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun QueueProgressSection(queue: LiveQueueInfo) {
    val current = queue.currentServingToken.toIntOrNull() ?: 0
    val target = queue.queueNumber.toIntOrNull() ?: 0
    
    val progress = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = QC.Surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Queue Progress",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = QC.OnSurface
                )
            )
            Spacer(Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(QC.Outline.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(QC.GradStart, QC.GradEnd)
                            )
                        )
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Now Serving", style = MaterialTheme.typography.labelSmall, color = QC.TextSec)
                    Text("#${queue.currentServingToken}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("People Ahead", style = MaterialTheme.typography.labelSmall, color = QC.TextSec)
                    Text("${queue.patientsAhead}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = QC.Primary)
                }
            }
        }
    }
}

@Composable
private fun InfoGridSection(queue: LiveQueueInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItemCard(
                label = "Estimated Wait",
                value = "${queue.estimatedWaitMinutes} min",
                icon = Icons.Default.Timer,
                iconColor = QC.Secondary,
                modifier = Modifier.weight(1f)
            )
            InfoItemCard(
                label = "Crowd Level",
                value = queue.crowdLevel.name,
                icon = Icons.Default.Groups,
                iconColor = when(queue.crowdLevel) {
                    CrowdLevel.LOW -> QC.Success
                    CrowdLevel.MEDIUM -> QC.Warning
                    CrowdLevel.HIGH -> QC.Error
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItemCard(
                label = "Status",
                value = queue.status.replace("_", " ").uppercase(),
                icon = Icons.Default.Info,
                iconColor = QC.Primary,
                modifier = Modifier.weight(1f)
            )
            // Empty space or another card if needed. Using 1f weight on one makes it full width but we want consistent grid.
            // Let's make Status full width or add another stat.
            // Actually, let's keep it 2x2 and maybe add "Now Serving" here too for consistency?
            // The prompt says: Now Serving, People Ahead, Estimated Wait, Crowd Level, Status.
            // We already have People Ahead and Now Serving in progress section.
            // Let's put Now Serving and People Ahead here as well for completeness as per requested list.
            InfoItemCard(
                label = "Now Serving",
                value = "#${queue.currentServingToken}",
                icon = Icons.Default.PeopleAlt,
                iconColor = QC.PrimaryLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoItemCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = QC.Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = QC.TextSec
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = QC.OnSurface
            )
        }
    }
}

@Composable
private fun NoActiveQueue(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = QC.Outline.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(40.dp), tint = QC.TextSec)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "No Active Queue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = QC.OnSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your appointment does not currently have an active queue.",
                textAlign = TextAlign.Center,
                color = QC.TextSec,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QC.Primary)
            ) {
                Text("Back to Dashboard")
            }
        }
    }
}
