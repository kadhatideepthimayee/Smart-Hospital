package com.example.smarthospitalqueue.ui.screens.patient

// ════════════════════════════════════════════════════════════════════════════
//  MedicalRecordsScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Self-contained mock data · Aligned with all preceding screens
//  Patient: Arjun Raghavan · Doctor: Dr. Arjun Mehta · Cardiology
// ════════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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

private object MRColors {
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
    val SuccessContainer  = Color(0xFFE8F5E9)
    val Warning           = Color(0xFFF57F17)
    val WarningContainer  = Color(0xFFFFF8E1)
    val Error             = Color(0xFFC62828)
    val GradientStart     = Color(0xFF0B3D91)
    val GradientEnd       = Color(0xFF1976D2)

    // Record type colours
    val Prescription      = Color(0xFF1565C0)
    val PrescriptionBg    = Color(0xFFDCE8FF)
    val LabReport         = Color(0xFF6A1B9A)
    val LabReportBg       = Color(0xFFF3E5F5)
    val Consultation      = Color(0xFF2E7D32)
    val ConsultationBg    = Color(0xFFE8F5E9)
    val Scan              = Color(0xFFF57F17)
    val ScanBg            = Color(0xFFFFF8E1)
}

// ─── Domain model ─────────────────────────────────────────────────────────────

private enum class RecordType { PRESCRIPTION, LAB_REPORT, CONSULTATION, SCAN }

private data class MedicalRecord(
    val id: String,
    val type: RecordType,
    val doctorName: String,
    val specialization: String,
    val department: String,
    val date: String,
    val description: String,
    val fileSize: String? = null,
    val isNew: Boolean = false
)

// ─── Mock Data — all linked to Arjun Raghavan / Dr. Arjun Mehta narrative ────

private object MRMockData {
    val patientName = "Arjun Raghavan"
    val patientId   = "APOL-PAT-20241182"

    val records = listOf(
        MedicalRecord(
            id            = "RX-2024-0612",
            type          = RecordType.PRESCRIPTION,
            doctorName    = "Dr. Arjun Mehta",
            specialization= "Senior Cardiologist",
            department    = "Cardiology",
            date          = "3 Jun 2025",
            description   = "Post-consultation prescription: Atorvastatin 20 mg (once daily), Aspirin 75 mg (once daily), Metoprolol 25 mg (twice daily). Follow-up in 4 weeks.",
            fileSize      = "128 KB",
            isNew         = true
        ),
        MedicalRecord(
            id            = "LR-2024-0598",
            type          = RecordType.LAB_REPORT,
            doctorName    = "Dr. Arjun Mehta",
            specialization= "Senior Cardiologist",
            department    = "Cardiology",
            date          = "28 May 2025",
            description   = "Lipid profile: Total Cholesterol 218 mg/dL (borderline), LDL 142 mg/dL (high), HDL 38 mg/dL (low), Triglycerides 192 mg/dL. HbA1c 5.8%.",
            fileSize      = "340 KB",
            isNew         = true
        ),
        MedicalRecord(
            id            = "CN-2024-0551",
            type          = RecordType.CONSULTATION,
            doctorName    = "Dr. Arjun Mehta",
            specialization= "Senior Cardiologist",
            department    = "Cardiology",
            date          = "14 Apr 2025",
            description   = "Initial cardiology review. Patient reported intermittent chest tightness on exertion. Recommended lifestyle changes, started on statins, and ordered lipid panel + ECG.",
            fileSize      = null
        ),
        MedicalRecord(
            id            = "SC-2024-0488",
            type          = RecordType.SCAN,
            doctorName    = "Dr. Arjun Mehta",
            specialization= "Senior Cardiologist",
            department    = "Cardiology",
            date          = "14 Apr 2025",
            description   = "12-lead ECG: Normal sinus rhythm at 78 bpm. No ST-segment changes. Mild left axis deviation. Echocardiogram scheduled for follow-up.",
            fileSize      = "1.2 MB"
        ),
        MedicalRecord(
            id            = "CN-2024-0320",
            type          = RecordType.CONSULTATION,
            doctorName    = "Dr. Sunita Patel",
            specialization= "General Physician",
            department    = "General Medicine",
            date          = "02 Jan 2025",
            description   = "Annual health check-up. BP: 134/86 mmHg (elevated). BMI: 27.4 (overweight). Referred to Cardiology for further evaluation.",
            fileSize      = null
        ),
        MedicalRecord(
            id            = "LR-2024-0301",
            type          = RecordType.LAB_REPORT,
            doctorName    = "Dr. Sunita Patel",
            specialization= "General Physician",
            department    = "General Medicine",
            date          = "02 Jan 2025",
            description   = "Complete Blood Count: Within normal limits. Fasting Blood Sugar: 98 mg/dL. Creatinine: 0.9 mg/dL (normal). Vitamin D: 18 ng/mL (deficient).",
            fileSize      = "210 KB"
        ),
        MedicalRecord(
            id            = "RX-2023-1142",
            type          = RecordType.PRESCRIPTION,
            doctorName    = "Dr. Sunita Patel",
            specialization= "General Physician",
            department    = "General Medicine",
            date          = "02 Jan 2025",
            description   = "Vitamin D3 60,000 IU (weekly × 8 weeks), Calcium 500 mg (daily). Lifestyle counselling: reduced sodium diet, 30-min daily walk.",
            fileSize      = "95 KB"
        ),
    )

    val filterTypes = listOf("All", "Prescriptions", "Lab Reports", "Consultations", "Scans")

    val recordStats = listOf(
        Triple(Icons.Outlined.Medication,       "${records.count { it.type == RecordType.PRESCRIPTION }}",  "Prescriptions"),
        Triple(Icons.Outlined.Science,          "${records.count { it.type == RecordType.LAB_REPORT }}",    "Lab Reports"),
        Triple(Icons.Outlined.MedicalServices,  "${records.count { it.type == RecordType.CONSULTATION }}",  "Consultations"),
        Triple(Icons.Outlined.DocumentScanner,  "${records.count { it.type == RecordType.SCAN }}",          "Scans"),
    )
}

// ─── Record type style helper ─────────────────────────────────────────────────

private data class RecordStyle(
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val label: String
)

private fun recordStyle(type: RecordType): RecordStyle = when (type) {
    RecordType.PRESCRIPTION -> RecordStyle(Icons.Outlined.Medication,      MRColors.Prescription, MRColors.PrescriptionBg, "Prescription")
    RecordType.LAB_REPORT   -> RecordStyle(Icons.Outlined.Science,          MRColors.LabReport,    MRColors.LabReportBg,    "Lab Report")
    RecordType.CONSULTATION -> RecordStyle(Icons.Outlined.MedicalServices,  MRColors.Consultation, MRColors.ConsultationBg, "Consultation")
    RecordType.SCAN         -> RecordStyle(Icons.Outlined.DocumentScanner,  MRColors.Scan,         MRColors.ScanBg,         "Scan / ECG")
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(
    onBackClick: () -> Unit = {}
) {
    var searchQuery    by remember { mutableStateOf("") }
    var selectedType   by remember { mutableStateOf("All") }
    var headerVisible  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); headerVisible = true }

    val filtered = remember(searchQuery, selectedType) {
        MRMockData.records.filter { r ->
            val typeMatch = when (selectedType) {
                "Prescriptions" -> r.type == RecordType.PRESCRIPTION
                "Lab Reports"   -> r.type == RecordType.LAB_REPORT
                "Consultations" -> r.type == RecordType.CONSULTATION
                "Scans"         -> r.type == RecordType.SCAN
                else            -> true
            }
            val q = searchQuery.trim()
            val searchMatch = q.isEmpty() ||
                    r.description.contains(q, ignoreCase = true) ||
                    r.doctorName.contains(q, ignoreCase = true) ||
                    r.department.contains(q, ignoreCase = true)
            typeMatch && searchMatch
        }
    }

    Scaffold(
        topBar = { MedicalRecordsTopBar(onBackClick = onBackClick) },
        containerColor = MRColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Patient summary strip ───────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 3 }
            ) {
                PatientSummaryStrip()
            }

            // ── Stats row ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(500, 100))
            ) {
                RecordStatsRow(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            // ── Search bar ──────────────────────────────────────────────────
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(10.dp))

            // ── Filter chips ────────────────────────────────────────────────
            FilterChipsRow(
                types = MRMockData.filterTypes,
                selected = selectedType,
                onSelect = { selectedType = it }
            )

            Spacer(Modifier.height(10.dp))

            // ── Results header ──────────────────────────────────────────────
            AnimatedContent(
                targetState = filtered.size,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "count_anim"
            ) { count ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$count record${if (count != 1) "s" else ""} found",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MRColors.TextSecondary
                        )
                    )
                    if (searchQuery.isNotEmpty() || selectedType != "All") {
                        TextButton(
                            onClick = { searchQuery = ""; selectedType = "All" },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Clear filters",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MRColors.Primary
                                )
                            )
                        }
                    }
                }
            }

            // ── Records list / empty state ──────────────────────────────────
            if (filtered.isEmpty()) {
                EmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { record ->
                        MedicalRecordCard(record = record)
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  TOP APP BAR
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicalRecordsTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(MRColors.GradientStart, MRColors.GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Medical Records",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        "Apollo Smart Hospital",
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
                    Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = Color.White)
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
//  PATIENT SUMMARY STRIP
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PatientSummaryStrip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(MRColors.GradientEnd, MRColors.Background),
                    startY = 0f,
                    endY = 400f
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "AR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    MRMockData.patientName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    MRMockData.patientId,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }

            // Total records badge
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Text(
                    "${MRMockData.records.size} Records",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  STATS ROW
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun RecordStatsRow(modifier: Modifier = Modifier) {
    val statColors = listOf(
        MRColors.Prescription,
        MRColors.LabReport,
        MRColors.Consultation,
        MRColors.Scan
    )
    val statBgs = listOf(
        MRColors.PrescriptionBg,
        MRColors.LabReportBg,
        MRColors.ConsultationBg,
        MRColors.ScanBg
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MRMockData.recordStats.forEachIndexed { i, (icon, count, label) ->
            StatBox(
                icon = icon,
                count = count,
                label = label,
                iconColor = statColors[i],
                iconBg = statBgs[i],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBox(
    icon: ImageVector,
    count: String,
    label: String,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MRColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Text(
                count,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MRColors.OnSurface
                )
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MRColors.TextSecondary,
                    fontSize = 9.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SEARCH BAR
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "Search by doctor, department or condition…",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MRColors.TextSecondary
                )
            )
        },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MRColors.Primary)
        },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MRColors.TextSecondary)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MRColors.Primary,
            unfocusedBorderColor = MRColors.Outline,
            focusedContainerColor = MRColors.Surface,
            unfocusedContainerColor = MRColors.Surface
        ),
        singleLine = true
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  FILTER CHIPS ROW
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun FilterChipsRow(
    types: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val chipIcons = mapOf(
        "All"           to Icons.Default.GridView,
        "Prescriptions" to Icons.Outlined.Medication,
        "Lab Reports"   to Icons.Outlined.Science,
        "Consultations" to Icons.Outlined.MedicalServices,
        "Scans"         to Icons.Outlined.DocumentScanner,
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types) { type ->
            val isSelected = type == selected
            val chipColor by animateColorAsState(
                targetValue = if (isSelected) MRColors.Primary else MRColors.Surface,
                animationSpec = tween(200),
                label = "chip_$type"
            )

            Surface(
                onClick = { onSelect(type) },
                shape = RoundedCornerShape(50),
                color = chipColor,
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) MRColors.Primary else MRColors.Outline
                ),
                shadowElevation = if (isSelected) 4.dp else 0.dp,
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = chipIcons[type] ?: Icons.Outlined.MedicalServices,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else MRColors.TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        type,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) Color.White else MRColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  MEDICAL RECORD CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun MedicalRecordCard(record: MedicalRecord) {
    val style = recordStyle(record.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (record.isNew)
                    Modifier.border(1.5.dp, style.color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MRColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        onClick = {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Top row: icon + meta + NEW badge ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Type icon box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(style.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        style.icon,
                        contentDescription = null,
                        tint = style.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Type label + date row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = style.bgColor
                            ) {
                                Text(
                                    style.label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = style.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            if (record.isNew) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MRColors.SuccessContainer
                                ) {
                                    Text(
                                        "NEW",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MRColors.Success,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MRColors.TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                record.date,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MRColors.TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Doctor name
                    Text(
                        record.doctorName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MRColors.OnSurface
                        )
                    )

                    // Department
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Business,
                            contentDescription = null,
                            tint = MRColors.Primary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            "${record.specialization} · ${record.department}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MRColors.Primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // ── Divider ─────────────────────────────────────────────────────
            HorizontalDivider(
                color = MRColors.Outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // ── Description ─────────────────────────────────────────────────
            Text(
                record.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MRColors.TextSecondary,
                    lineHeight = 19.sp
                )
            )

            // ── Record ID ───────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Text(
                "Record ID: ${record.id}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MRColors.Outline,
                    letterSpacing = 0.5.sp
                )
            )

            // ── File row (if attachment present) ────────────────────────────
            if (record.fileSize != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MRColors.SurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AttachFile,
                            contentDescription = null,
                            tint = MRColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Attached document",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MRColors.TextSecondary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MRColors.Outline.copy(alpha = 0.4f)
                        ) {
                            Text(
                                record.fileSize,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MRColors.TextSecondary,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // View
                        Surface(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            color = MRColors.PrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = MRColors.Primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    "View",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MRColors.Primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        // Download
                        Surface(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            color = style.bgColor
                        ) {
                            Icon(
                                Icons.Outlined.Download,
                                contentDescription = "Download",
                                tint = style.color,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  EMPTY STATE
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MRColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = MRColors.Primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No records found",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MRColors.OnSurface
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Try adjusting your search\nor select a different filter",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MRColors.TextSecondary
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true, name = "Medical Records Screen")
@Composable
private fun MedicalRecordsScreenPreview() {
    MaterialTheme {
        MedicalRecordsScreen(onBackClick = {})
    }
}