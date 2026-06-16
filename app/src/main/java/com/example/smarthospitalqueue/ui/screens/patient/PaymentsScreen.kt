package com.example.smarthospitalqueue.ui.screens.patient

// ════════════════════════════════════════════════════════════════════════════
//  PaymentsScreen.kt
//  Smart Hospital Queue Management System — Predictive Waiting & Dynamic Scheduling
//
//  Design language: Material 3 · Premium Healthcare Blue-White Palette
//  Self-contained mock data · Aligned with all preceding screens
//  Patient: Arjun Raghavan · Doctor: Dr. Arjun Mehta · APT-2024-1182
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

private object PayColors {
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
    val Purple            = Color(0xFF6A1B9A)
    val PurpleContainer   = Color(0xFFF3E5F5)
}

// ─── Domain model ─────────────────────────────────────────────────────────────

private enum class PaymentStatus { PAID, PENDING, FAILED }

private enum class PaymentMethod { UPI, CARD, CASH, NETBANKING, INSURANCE }

private data class Payment(
    val id: String,
    val invoiceId: String,
    val description: String,
    val category: String,
    val doctorName: String,
    val department: String,
    val date: String,
    val amount: Int,
    val status: PaymentStatus,
    val method: PaymentMethod? = null,
    val referenceNo: String? = null
)

// ─── Mock Data — linked to Arjun Raghavan / APT-2024-1182 narrative ──────────

private object PayMockData {
    val patientName = "Arjun Raghavan"
    val patientId   = "APOL-PAT-20241182"

    val payments = listOf(
        // Current appointment — PENDING (just booked)
        Payment(
            id           = "PAY-001",
            invoiceId    = "INV-2024-1182",
            description  = "Consultation — Cardiology (Dr. Arjun Mehta)",
            category     = "Consultation",
            doctorName   = "Dr. Arjun Mehta",
            department   = "Cardiology",
            date         = "3 Jun 2025",
            amount       = 800,
            status       = PaymentStatus.PENDING,
            method       = null,
            referenceNo  = "APT-2024-1182"
        ),
        // Lab tests ordered — PENDING
        Payment(
            id           = "PAY-002",
            invoiceId    = "INV-2024-1183",
            description  = "Lipid Profile + HbA1c — Lab Services",
            category     = "Lab Tests",
            doctorName   = "Dr. Arjun Mehta",
            department   = "Cardiology / Pathology",
            date         = "28 May 2025",
            amount       = 1_200,
            status       = PaymentStatus.PENDING,
            method       = null,
            referenceNo  = "LR-2024-0598"
        ),
        // Past consultation — PAID
        Payment(
            id           = "PAY-003",
            invoiceId    = "INV-2024-1091",
            description  = "Consultation — Cardiology (Initial Review)",
            category     = "Consultation",
            doctorName   = "Dr. Arjun Mehta",
            department   = "Cardiology",
            date         = "14 Apr 2025",
            amount       = 800,
            status       = PaymentStatus.PAID,
            method       = PaymentMethod.UPI,
            referenceNo  = "CN-2024-0551"
        ),
        // ECG + Echo — PAID
        Payment(
            id           = "PAY-004",
            invoiceId    = "INV-2024-1092",
            description  = "ECG & Echocardiogram — Diagnostics",
            category     = "Diagnostics",
            doctorName   = "Dr. Arjun Mehta",
            department   = "Cardiology",
            date         = "14 Apr 2025",
            amount       = 2_500,
            status       = PaymentStatus.PAID,
            method       = PaymentMethod.CARD,
            referenceNo  = "SC-2024-0488"
        ),
        // General check-up — PAID
        Payment(
            id           = "PAY-005",
            invoiceId    = "INV-2024-0812",
            description  = "Annual Health Check-Up — General Medicine",
            category     = "Consultation",
            doctorName   = "Dr. Sunita Patel",
            department   = "General Medicine",
            date         = "2 Jan 2025",
            amount       = 500,
            status       = PaymentStatus.PAID,
            method       = PaymentMethod.NETBANKING,
            referenceNo  = "CN-2024-0320"
        ),
        // CBC + Blood Sugar — PAID
        Payment(
            id           = "PAY-006",
            invoiceId    = "INV-2024-0813",
            description  = "CBC, Blood Sugar & Vitamin D — Lab Tests",
            category     = "Lab Tests",
            doctorName   = "Dr. Sunita Patel",
            department   = "General Medicine",
            date         = "2 Jan 2025",
            amount       = 950,
            status       = PaymentStatus.PAID,
            method       = PaymentMethod.UPI,
            referenceNo  = "LR-2024-0301"
        ),
        // Failed payment attempt
        Payment(
            id           = "PAY-007",
            invoiceId    = "INV-2024-0799",
            description  = "Pharmacy — Atorvastatin & Supplements",
            category     = "Pharmacy",
            doctorName   = "Apollo Pharmacy",
            department   = "Pharmacy",
            date         = "3 Jun 2025",
            amount       = 680,
            status       = PaymentStatus.FAILED,
            method       = PaymentMethod.CARD,
            referenceNo  = "RX-2024-0612"
        ),
    )

    val totalPaid    = payments.filter { it.status == PaymentStatus.PAID }.sumOf { it.amount }
    val totalPending = payments.filter { it.status == PaymentStatus.PENDING }.sumOf { it.amount }
    val totalFailed  = payments.filter { it.status == PaymentStatus.FAILED }.sumOf { it.amount }

    val methodLabels = mapOf(
        PaymentMethod.UPI        to "UPI",
        PaymentMethod.CARD       to "Card",
        PaymentMethod.CASH       to "Cash",
        PaymentMethod.NETBANKING to "Net Banking",
        PaymentMethod.INSURANCE  to "Insurance",
    )
    val methodIcons = mapOf(
        PaymentMethod.UPI        to Icons.Outlined.PhoneAndroid,
        PaymentMethod.CARD       to Icons.Outlined.CreditCard,
        PaymentMethod.CASH       to Icons.Outlined.Payments,
        PaymentMethod.NETBANKING to Icons.Outlined.AccountBalance,
        PaymentMethod.INSURANCE  to Icons.Outlined.HealthAndSafety,
    )
}

// ─── Status style helper ──────────────────────────────────────────────────────

private data class StatusStyle(
    val color: Color,
    val bgColor: Color,
    val icon: ImageVector,
    val label: String
)

private fun statusStyle(status: PaymentStatus): StatusStyle = when (status) {
    PaymentStatus.PAID    -> StatusStyle(PayColors.Success,  PayColors.SuccessContainer, Icons.Outlined.CheckCircle,    "Paid")
    PaymentStatus.PENDING -> StatusStyle(PayColors.Warning,  PayColors.WarningContainer, Icons.Outlined.PendingActions, "Pending")
    PaymentStatus.FAILED  -> StatusStyle(PayColors.Error,    PayColors.ErrorContainer,   Icons.Outlined.Cancel,         "Failed")
}

// ════════════════════════════════════════════════════════════════════════════
//  ROOT SCREEN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var headerVisible  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); headerVisible = true }

    val filters  = listOf("All", "Paid", "Pending", "Failed")
    val payments = remember(selectedFilter) {
        when (selectedFilter) {
            "Paid"    -> PayMockData.payments.filter { it.status == PaymentStatus.PAID }
            "Pending" -> PayMockData.payments.filter { it.status == PaymentStatus.PENDING }
            "Failed"  -> PayMockData.payments.filter { it.status == PaymentStatus.FAILED }
            else      -> PayMockData.payments
        }
    }

    Scaffold(
        topBar = { PaymentsTopBar(onBackClick = onBackClick) },
        containerColor = PayColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Patient summary strip ───────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 3 }
                ) {
                    PatientSummaryStrip()
                }
            }

            // ── Three summary cards ─────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 100))
                ) {
                    SummaryCardsRow(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            // ── Pending payment alert ───────────────────────────────────────
            if (PayMockData.totalPending > 0) {
                item {
                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = fadeIn(tween(450, 150))
                    ) {
                        PendingPaymentAlert(
                            amount = PayMockData.totalPending,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Spending breakdown ──────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 200))
                ) {
                    SpendingBreakdownCard(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            // ── Filter chips + section header ───────────────────────────────
            item {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450, 250))
                ) {
                    Column {
                        FilterChipsRow(
                            filters = filters,
                            selected = selectedFilter,
                            onSelect = { selectedFilter = it }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Payment History",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PayColors.OnSurface
                                )
                            )
                            Text(
                                "${payments.size} records",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PayColors.TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // ── Payment cards ───────────────────────────────────────────────
            items(payments, key = { it.id }) { payment ->
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
                ) {
                    PaymentCard(
                        payment = payment,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                    )
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
private fun PaymentsTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(PayColors.GradientStart, PayColors.GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Payments & Billing",
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
                    Icon(Icons.Outlined.Download, contentDescription = "Export", tint = Color.White)
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
                    listOf(PayColors.GradientEnd, PayColors.Background),
                    startY = 0f, endY = 420f
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
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
                    PayMockData.patientName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    PayMockData.patientId,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
            // Total spend pill
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Text(
                    "₹${"%.0f".format((PayMockData.totalPaid + PayMockData.totalPending).toFloat())} Total",
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
//  SUMMARY CARDS ROW
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun SummaryCardsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryBox(
            icon     = Icons.Outlined.CheckCircle,
            label    = "Total Paid",
            amount   = "₹${"%,d".format(PayMockData.totalPaid)}",
            color    = PayColors.Success,
            bgColor  = PayColors.SuccessContainer,
            modifier = Modifier.weight(1f)
        )
        SummaryBox(
            icon     = Icons.Outlined.PendingActions,
            label    = "Pending",
            amount   = "₹${"%,d".format(PayMockData.totalPending)}",
            color    = PayColors.Warning,
            bgColor  = PayColors.WarningContainer,
            modifier = Modifier.weight(1f)
        )
        SummaryBox(
            icon     = Icons.Outlined.Cancel,
            label    = "Failed",
            amount   = "₹${"%,d".format(PayMockData.totalFailed)}",
            color    = PayColors.Error,
            bgColor  = PayColors.ErrorContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryBox(
    icon: ImageVector,
    label: String,
    amount: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PayColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(
                amount,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PayColors.TextSecondary
                )
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PENDING PAYMENT ALERT BANNER
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PendingPaymentAlert(amount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PayColors.ErrorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, PayColors.Error.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PayColors.Error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = PayColors.Error,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Payment Due",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PayColors.Error
                    )
                )
                Text(
                    "₹${"%,d".format(amount)} outstanding across ${PayMockData.payments.count { it.status == PaymentStatus.PENDING }} invoice${if (PayMockData.payments.count { it.status == PaymentStatus.PENDING } > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF7B1F1F)
                    )
                )
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PayColors.Error,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Pay Now",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  SPENDING BREAKDOWN CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun SpendingBreakdownCard(modifier: Modifier = Modifier) {
    val categories = listOf(
        Triple("Consultations", 2_100, PayColors.Primary),
        Triple("Lab Tests",     2_150, PayColors.Purple),
        Triple("Diagnostics",   2_500, PayColors.Secondary),
        Triple("Pharmacy",        680, PayColors.Warning),
    )
    val total = categories.sumOf { it.second }.toFloat()

    PremiumCard(modifier = modifier) {
        CardSectionHeader(
            icon    = Icons.Outlined.PieChart,
            title   = "Spending Breakdown",
            iconBg  = PayColors.PrimaryContainer,
            iconTint= PayColors.Primary
        )

        Spacer(Modifier.height(16.dp))

        categories.forEach { (label, amount, color) ->
            val fraction = amount / total
            val animated by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(900, easing = EaseOutCubic),
                label = "bar_$label"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PayColors.OnSurface
                    ),
                    modifier = Modifier.width(100.dp)
                )
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = PayColors.SurfaceVariant
                )
                Text(
                    "₹${"%,d".format(amount)}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    ),
                    modifier = Modifier.width(56.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  FILTER CHIPS ROW
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun FilterChipsRow(
    filters: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val chipColors = mapOf(
        "All"     to PayColors.Primary,
        "Paid"    to PayColors.Success,
        "Pending" to PayColors.Warning,
        "Failed"  to PayColors.Error,
    )
    val chipIcons = mapOf(
        "All"     to Icons.Default.GridView,
        "Paid"    to Icons.Outlined.CheckCircle,
        "Pending" to Icons.Outlined.PendingActions,
        "Failed"  to Icons.Outlined.Cancel,
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == selected
            val accent = chipColors[filter] ?: PayColors.Primary

            Surface(
                onClick = { onSelect(filter) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) accent else PayColors.Surface,
                border = BorderStroke(1.5.dp, if (isSelected) accent else PayColors.Outline),
                shadowElevation = if (isSelected) 4.dp else 0.dp,
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = chipIcons[filter] ?: Icons.Default.GridView,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else PayColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        filter,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) Color.White else PayColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PAYMENT CARD
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PaymentCard(payment: Payment, modifier: Modifier = Modifier) {
    val style = statusStyle(payment.status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (payment.status == PaymentStatus.PENDING)
                    Modifier.border(1.5.dp, PayColors.Warning.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                else if (payment.status == PaymentStatus.FAILED)
                    Modifier.border(1.5.dp, PayColors.Error.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (payment.status) {
                PaymentStatus.PENDING -> PayColors.WarningContainer.copy(alpha = 0.35f)
                PaymentStatus.FAILED  -> PayColors.ErrorContainer.copy(alpha = 0.35f)
                else                  -> PayColors.Surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (payment.status != PaymentStatus.PAID) 4.dp else 2.dp
        ),
        onClick = {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Top row ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Receipt icon box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(style.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Receipt,
                        contentDescription = null,
                        tint = style.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Category badge + status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PayColors.PrimaryContainer
                        ) {
                            Text(
                                payment.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PayColors.Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Status pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = style.bgColor,
                            border = BorderStroke(1.dp, style.color.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    style.icon,
                                    contentDescription = null,
                                    tint = style.color,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    style.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = style.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Description
                    Text(
                        payment.description,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PayColors.OnSurface
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Doctor + department
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.LocalHospital,
                            contentDescription = null,
                            tint = PayColors.Primary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            "${payment.doctorName} · ${payment.department}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PayColors.Primary,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ── Divider ─────────────────────────────────────────────────────
            HorizontalDivider(
                color = PayColors.Outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // ── Bottom row: amount + date + ref + method + actions ───────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "₹${"%,d".format(payment.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = style.color,
                            fontSize = 22.sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = PayColors.TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            payment.date,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PayColors.TextSecondary
                            )
                        )
                    }
                    payment.referenceNo?.let {
                        Text(
                            "Ref: $it",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PayColors.Outline,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Payment method tag
                    payment.method?.let { method ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PayColors.SurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    PayMockData.methodIcons[method] ?: Icons.Outlined.Payments,
                                    contentDescription = null,
                                    tint = PayColors.TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    PayMockData.methodLabels[method] ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PayColors.TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        when (payment.status) {
                            PaymentStatus.PAID -> {
                                Surface(
                                    onClick = {},
                                    shape = RoundedCornerShape(9.dp),
                                    color = PayColors.SuccessContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Download,
                                            contentDescription = null,
                                            tint = PayColors.Success,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            "Receipt",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PayColors.Success,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                            PaymentStatus.PENDING -> {
                                Button(
                                    onClick = {},
                                    shape = RoundedCornerShape(9.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PayColors.Warning,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    elevation = ButtonDefaults.buttonElevation(2.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Pay Now",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            PaymentStatus.FAILED -> {
                                Surface(
                                    onClick = {},
                                    shape = RoundedCornerShape(9.dp),
                                    color = PayColors.ErrorContainer,
                                    border = BorderStroke(1.dp, PayColors.Error.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Refresh,
                                            contentDescription = null,
                                            tint = PayColors.Error,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            "Retry",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PayColors.Error,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
        colors = CardDefaults.cardColors(containerColor = PayColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun CardSectionHeader(
    icon: ImageVector,
    title: String,
    iconBg: Color,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PayColors.OnSurface
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  PREVIEW
// ════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, showSystemUi = true, name = "Payments Screen")
@Composable
private fun PaymentsScreenPreview() {
    MaterialTheme {
        PaymentsScreen(onBackClick = {})
    }
}