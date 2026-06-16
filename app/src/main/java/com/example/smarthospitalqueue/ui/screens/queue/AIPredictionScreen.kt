package com.example.smarthospitalqueue.ui.screens.queue

// ════════════════════════════════════════════════════════════════════════════
//  AIPredictionScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Self-contained mock data · Aligned with QueueTrackingScreen & ConfirmationScreen
// ════════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

// ─── Color Palette (shared across all screens) ────────────────────────────────

private object AiColors {
    val Primary           = Color(0xFF0B3D91)
    val PrimaryLight      = Color(0xFF1565C0)
    val PrimaryContainer  = Color(0xFFDCE8FF)
    val Secondary         = Color(0xFF00897B)
    val SecondaryContainer= Color(0xFFB2DFDB)
    val Background        = Color(0xFFF4F7FF)
    val Surface           = Color(0xFFFFFFFF)
    val SurfaceVariant    = Color(0xFFF0F4FF)
    val OnSurface         = Color(0xFF1A2744)
    val TextSecondary     = Color(0xFF5A6A8A)
    val Outline           = Color(0xFFCED8F5)
    val Success           = Color(0xFF2E7D32)
    val SuccessLight      = Color(0xFF4CAF50)
    val SuccessContainer  = Color(0xFFE8F5E9)
    val Warning           = Color(0xFFF57F17)
    val WarningContainer  = Color(0xFFFFF8E1)
    val Error             = Color(0xFFC62828)
    val ErrorContainer    = Color(0xFFFFEBEE)
    val GradientStart     = Color(0xFF0B3D91)
    val GradientEnd       = Color(0xFF1976D2)
    val AiAccent          = Color(0xFF7C4DFF)
    val AiContainer       = Color(0xFFF3EEFF)
    val AiDeep            = Color(0xFF5E35B1)
    val StarGold          = Color(0xFFFFC107)
    val CriticalRed       = Color(0xFF991B1B)
}

// ─── Mock Data ────────────────────────────────────────────────────────────────

private object AiMockData {
    // Aligned with booking: Dr. Arjun Mehta, T-052
    val doctorName      = "Dr. Arjun Mehta"
    val department      = "Cardiology"
    val patientToken    = "T-052"
    val patientName     = "Arjun Raghavan"

    // Core prediction
    val predictedWait   = "24–30 min"
    val confidence      = 91
    val confidenceLabel = "High"
    val queueTrend      = "Moderate"
    val trendDirection  = "▼ Dropping"
    val bestArrival     = "9:45 AM"
    val datasetDays     = 90
    val accuracyPct     = 91

    // Hourly crowd data (8 AM – 7 PM, 11 entries)
    val hourlyData = listOf(
        "8 AM" to 22,
        "9 AM" to 48,
        "10 AM" to 65,    // current patient's slot
        "11 AM" to 80,
        "12 PM" to 55,
        "1 PM" to 18,
        "2 PM" to 28,
        "3 PM" to 72,
        "4 PM" to 60,
        "5 PM" to 35,
        "6 PM" to 20
    )
    val currentHourIndex = 2   // 10 AM

    // Best visit windows
    val visitWindows = listOf(
        Triple("1:00 PM – 2:00 PM", "Lowest crowd — ~10 min wait", AiColors.Success),
        Triple("10:00 AM – 10:30 AM", "Low congestion — ~12 min wait", AiColors.Success),
        Triple("4:30 PM – 5:30 PM", "Moderate — ~20 min wait", AiColors.Warning),
    )

    // Department congestion
    data class DeptStat(val name: String, val avgWait: Int, val level: CongestionLevel)
    enum class CongestionLevel { LOW, MODERATE, HIGH, CRITICAL }

    val departments = listOf(
        DeptStat("Cardiology",       28, CongestionLevel.MODERATE),
        DeptStat("Neurology",        35, CongestionLevel.HIGH),
        DeptStat("Orthopedics",      15, CongestionLevel.LOW),
        DeptStat("General Medicine", 42, CongestionLevel.HIGH),
        DeptStat("Pediatrics",       10, CongestionLevel.LOW),
        DeptStat("Dermatology",      50, CongestionLevel.CRITICAL),
    )

    // Smart recommendations
    val recommendations = listOf(
        Icons.Outlined.Schedule        to "Visit between 1–2 PM to reduce your wait by up to 65%.",
        Icons.Outlined.EventAvailable  to "Tuesday and Wednesday are the least crowded days.",
        Icons.Outlined.Smartphone      to "Pre-book your slot online to skip the physical queue entirely.",
        Icons.Outlined.Science         to "Lab tests: best submitted before 9:30 AM for same-day results.",
        Icons.Outlined.DirectionsWalk  to "Arrive 10 minutes early — check-in adds ~3 min to your wait.",
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPredictionScreen(
    onBackClick: () -> Unit = {}
) {
    // Staggered reveal — matches other screens in the project
    var phase by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(150); phase = 1
        delay(250); phase = 2
        delay(200); phase = 3
        delay(200); phase = 4
        delay(200); phase = 5
        delay(200); phase = 6
        delay(200); phase = 7
    }

    Scaffold(
        topBar = { AiTopBar(onBackClick = onBackClick) },
        containerColor = AiColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Hero AI banner ──────────────────────────────────────────────
            AiHeroBanner()

            // ── Three prediction chips ──────────────────────────────────────
            RevealSection(visible = phase >= 1) {
                PredictionSummaryRow(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            // ── Confidence meter ────────────────────────────────────────────
            RevealSection(visible = phase >= 2) {
                ConfidenceMeterCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // ── Crowd analysis chart ────────────────────────────────────────
            RevealSection(visible = phase >= 3) {
                PremiumSectionHeader(
                    title = "Crowd Analysis — Today",
                    subtitle = "Hourly patient footfall forecast",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
                CrowdAnalysisChart(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Best visit windows ──────────────────────────────────────────
            RevealSection(visible = phase >= 4) {
                PremiumSectionHeader(
                    title = "Recommended Visit Windows",
                    subtitle = "Optimal times to minimise your wait",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
                BestVisitWindowsCard(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Department congestion ───────────────────────────────────────
            RevealSection(visible = phase >= 5) {
                PremiumSectionHeader(
                    title = "Department Congestion",
                    subtitle = "Real-time load across all departments",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
                DepartmentCongestionCard(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Smart recommendations ───────────────────────────────────────
            RevealSection(visible = phase >= 6) {
                PremiumSectionHeader(
                    title = "Smart Recommendations",
                    subtitle = "Personalised tips for ${AiMockData.patientName}",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
                SmartRecommendationsCard(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Patient prediction summary ──────────────────────────────────
            RevealSection(visible = phase >= 7) {
                PatientPredictionSummary(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  TOP APP BAR
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(AiColors.AiDeep, AiColors.AiAccent)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "AI Queue Intelligence",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        "Apollo Smart Hospital · Predictive Analytics",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  AI HERO BANNER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiHeroBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.88f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(AiColors.AiAccent, AiColors.Background),
                    startY = 0f,
                    endY = 700f
                )
            )
            .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        // Decorative blurred circles
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 160.dp, y = (-40).dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-20).dp, y = 40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated AI orb
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = glowAlpha * 0.15f))
                )
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Predictive Intelligence",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                )
                Text(
                    "Analysing ${AiMockData.datasetDays} days of hospital traffic patterns to predict your experience.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF69F0AE))
                    )
                    Text(
                        "${AiMockData.accuracyPct}% prediction accuracy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF69F0AE),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREDICTION SUMMARY ROW — THREE CHIPS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PredictionSummaryRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PredictionChip(
            icon = Icons.Outlined.Timer,
            value = AiMockData.predictedWait,
            label = "Predicted Wait",
            iconColor = AiColors.Primary,
            iconBg = AiColors.PrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        PredictionChip(
            icon = Icons.Outlined.Verified,
            value = AiMockData.confidenceLabel,
            label = "Confidence",
            iconColor = AiColors.Success,
            iconBg = AiColors.SuccessContainer,
            modifier = Modifier.weight(1f)
        )
        PredictionChip(
            icon = Icons.Outlined.TrendingDown,
            value = AiMockData.trendDirection,
            label = "Queue Trend",
            iconColor = AiColors.Warning,
            iconBg = AiColors.WarningContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PredictionChip(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AiColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AiColors.OnSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AiColors.TextSecondary
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  CONFIDENCE METER CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun ConfidenceMeterCard(modifier: Modifier = Modifier) {
    val animatedConfidence by animateFloatAsState(
        targetValue = AiMockData.confidence / 100f,
        animationSpec = tween(1400, easing = EaseOutCubic),
        label = "confidence_anim"
    )

    PremiumCard(modifier = modifier, borderColor = AiColors.AiAccent.copy(alpha = 0.3f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.linearGradient(listOf(AiColors.AiAccent, AiColors.AiDeep))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        "Model Confidence",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AiColors.OnSurface
                        )
                    )
                    Text(
                        "Based on ${AiMockData.datasetDays}-day training dataset",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AiColors.TextSecondary
                        )
                    )
                }
            }
            Text(
                "${AiMockData.confidence}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AiColors.AiAccent,
                    fontSize = 28.sp
                )
            )
        }

        Spacer(Modifier.height(14.dp))

        // Progress bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LinearProgressIndicator(
                progress = { animatedConfidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = AiColors.AiAccent,
                trackColor = AiColors.AiContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low", style = MaterialTheme.typography.labelSmall.copy(color = AiColors.TextSecondary))
                Text("Medium", style = MaterialTheme.typography.labelSmall.copy(color = AiColors.TextSecondary))
                Text("High", style = MaterialTheme.typography.labelSmall.copy(color = AiColors.AiAccent, fontWeight = FontWeight.Bold))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Mini metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniMetric("Predicted Wait", AiMockData.predictedWait, Modifier.weight(1f))
            MiniMetric("Best Arrival", AiMockData.bestArrival, Modifier.weight(1f))
            MiniMetric("Trend", AiMockData.queueTrend, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AiColors.SurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AiColors.OnSurface
            )
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(color = AiColors.TextSecondary),
            textAlign = TextAlign.Center
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  CROWD ANALYSIS CHART
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun CrowdAnalysisChart(modifier: Modifier = Modifier) {
    val maxVal = AiMockData.hourlyData.maxOf { it.second }.toFloat()
    val infiniteTransition = rememberInfiniteTransition(label = "bar_breathe")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.97f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOut), RepeatMode.Reverse),
        label = "breathe"
    )

    PremiumCard(modifier = modifier) {
        // Legend
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            ChartLegendDot(AiColors.Success, "Low")
            ChartLegendDot(AiColors.Warning, "Moderate")
            ChartLegendDot(AiColors.Error, "High")
            ChartLegendDot(AiColors.Primary, "Your Slot")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            AiMockData.hourlyData.forEachIndexed { index, (hour, value) ->
                val isCurrent = index == AiMockData.currentHourIndex
                val barColor = when {
                    isCurrent             -> AiColors.Primary
                    value >= 70           -> AiColors.Error.copy(alpha = 0.75f)
                    value >= 45           -> AiColors.Warning.copy(alpha = 0.75f)
                    else                  -> AiColors.Success.copy(alpha = 0.75f)
                }
                val barH = ((value / maxVal) * 110f * if (isCurrent) breathe else 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    // Value label on current bar
                    if (isCurrent) {
                        Text(
                            "${value}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AiColors.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(if (isCurrent) 20.dp else 15.dp)
                            .height(barH.dp)
                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                            .background(
                                if (isCurrent)
                                    Brush.verticalGradient(listOf(AiColors.PrimaryLight, AiColors.Primary))
                                else
                                    Brush.verticalGradient(listOf(barColor.copy(alpha = 0.9f), barColor))
                            )
                    )
                    Spacer(Modifier.height(5.dp))
                    // Show label every 2 bars
                    if (index % 2 == 0) {
                        Text(
                            hour.replace(" AM", "a").replace(" PM", "p"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCurrent) AiColors.Primary else AiColors.TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    } else {
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // X-axis info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AiColors.PrimaryContainer)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = AiColors.Primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Your appointment slot: 10:00 AM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AiColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Text(
                "Moderate load",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AiColors.Warning,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(color = AiColors.TextSecondary)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  BEST VISIT WINDOWS CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun BestVisitWindowsCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        AiMockData.visitWindows.forEachIndexed { index, (window, desc, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (index == 0) AiColors.SuccessContainer
                        else AiColors.SurfaceVariant
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        window,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AiColors.OnSurface
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        desc,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AiColors.TextSecondary
                        )
                    )
                }
                if (index == 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AiColors.Success.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AiColors.Success.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "Best",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AiColors.Success,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = AiColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (index < AiMockData.visitWindows.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  DEPARTMENT CONGESTION CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun DepartmentCongestionCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        AiMockData.departments.forEachIndexed { index, dept ->
            DepartmentRow(dept = dept)
            if (index < AiMockData.departments.lastIndex) {
                HorizontalDivider(
                    color = AiColors.Outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun DepartmentRow(dept: AiMockData.DeptStat) {
    val maxWait = 55f
    val progress = (dept.avgWait / maxWait).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "dept_${dept.name}"
    )
    val isCurrentDept = dept.name == AiMockData.department
    val barColor = when (dept.level) {
        AiMockData.CongestionLevel.LOW      -> AiColors.Success
        AiMockData.CongestionLevel.MODERATE -> AiColors.Warning
        AiMockData.CongestionLevel.HIGH     -> AiColors.Error
        AiMockData.CongestionLevel.CRITICAL -> AiColors.CriticalRed
    }
    val levelLabel = when (dept.level) {
        AiMockData.CongestionLevel.LOW      -> "Low"
        AiMockData.CongestionLevel.MODERATE -> "Moderate"
        AiMockData.CongestionLevel.HIGH     -> "High"
        AiMockData.CongestionLevel.CRITICAL -> "Critical"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Department name + "Your Dept" badge
        Column(modifier = Modifier.width(112.dp)) {
            Text(
                dept.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isCurrentDept) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentDept) AiColors.Primary else AiColors.OnSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isCurrentDept) {
                Text(
                    "Your dept",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AiColors.Primary,
                        fontSize = 9.sp
                    )
                )
            }
        }

        // Progress bar
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = AiColors.SurfaceVariant
            )
        }

        // Wait + level badge
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(58.dp)) {
            Text(
                "~${dept.avgWait}m",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AiColors.OnSurface
                )
            )
            Text(
                levelLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = barColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SMART RECOMMENDATIONS CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun SmartRecommendationsCard(modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier) {
        AiMockData.recommendations.forEachIndexed { index, (icon, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AiColors.AiAccent.copy(0.15f), AiColors.PrimaryContainer)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = AiColors.AiAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AiColors.OnSurface,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            if (index < AiMockData.recommendations.lastIndex) {
                HorizontalDivider(
                    color = AiColors.Outline.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 50.dp, top = 2.dp, bottom = 2.dp)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PATIENT PREDICTION SUMMARY (bottom card linking back to the patient)
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PatientPredictionSummary(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AiColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Outlined.PersonPin, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(
                    "Your Personalised Prediction",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryPill("Token", AiMockData.patientToken, Modifier.weight(1f))
                SummaryPill("Wait", AiMockData.predictedWait, Modifier.weight(1f))
                SummaryPill("Arrive by", AiMockData.bestArrival, Modifier.weight(1f))
            }

            // Tip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color(0xFFFFE082), modifier = Modifier.size(18.dp))
                Text(
                    "AI recommends arriving by ${AiMockData.bestArrival} for the smoothest experience with ${AiMockData.doctorName}.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SHARED UTILITY COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(1.5.dp, borderColor, RoundedCornerShape(24.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AiColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun PremiumSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AiColors.OnSurface
            )
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                color = AiColors.TextSecondary
            )
        )
    }
}

@Composable
private fun RevealSection(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
    ) {
        content()
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true, name = "AI Prediction Screen")
@Composable
private fun AIPredictionScreenPreview() {
    MaterialTheme {
        AIPredictionScreen(onBackClick = {})
    }
}