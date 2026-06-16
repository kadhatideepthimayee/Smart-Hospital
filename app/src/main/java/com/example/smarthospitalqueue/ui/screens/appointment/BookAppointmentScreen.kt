package com.example.smarthospitalqueue.ui.screens.appointment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthospitalqueue.ui.viewmodel.BookAppointmentViewModel
import com.example.smarthospitalqueue.ui.viewmodel.BookingState

// ─── Data Models ────────────────────────────────────────────────────────────

data class Doctor(
    val id: Int,
    val name: String,
    val specialization: String,
    val department: String,
    val experience: Int,
    val consultationFee: Int,
    val rating: Float,
    val reviewCount: Int,
    val hospital: String,
    val isAvailable: Boolean,
    val avatarInitials: String,
    val avatarColor: Color,
    val nextAvailable: String = ""
)

data class TimeSlot(
    val time: String,
    val period: String,
    val isAvailable: Boolean
)

// ─── Mock Data ───────────────────────────────────────────────────────────────

val mockDoctors = listOf(
    Doctor(1, "Dr. Arjun Mehta", "Senior Cardiologist", "Cardiology", 15,
        800, 4.9f, 312, "Apollo Smart Hospital", true, "AM",
        Color(0xFF1565C0)),
    Doctor(2, "Dr. Priya Sharma", "Neurologist", "Neurology", 12,
        950, 4.8f, 278, "Apollo Smart Hospital", true, "PS",
        Color(0xFF6A1B9A)),
    Doctor(3, "Dr. Rajesh Kumar", "Orthopedic Surgeon", "Orthopedics", 18,
        750, 4.7f, 445, "Apollo Smart Hospital", false, "RK",
        Color(0xFF00695C)),
    Doctor(4, "Dr. Sunita Patel", "General Physician", "General Medicine", 10,
        500, 4.6f, 890, "Apollo Smart Hospital", true, "SP",
        Color(0xFFE65100)),
    Doctor(5, "Dr. Kavitha Nair", "Pediatrician", "Pediatrics", 8,
        600, 4.9f, 562, "Apollo Smart Hospital", true, "KN",
        Color(0xFF558B2F)),
    Doctor(6, "Dr. Vikram Singh", "Dermatologist", "Dermatology", 11,
        700, 4.5f, 234, "Apollo Smart Hospital", false, "VS",
        Color(0xFFC62828))
)

val departments = listOf(
    "All", "Cardiology", "Neurology", "Orthopedics",
    "General Medicine", "Pediatrics", "Dermatology"
)

val morningSlots = listOf(
    TimeSlot("09:00 AM", "Morning", true),
    TimeSlot("09:30 AM", "Morning", false),
    TimeSlot("10:00 AM", "Morning", true),
    TimeSlot("10:30 AM", "Morning", true),
    TimeSlot("11:00 AM", "Morning", false),
    TimeSlot("11:30 AM", "Morning", true)
)

val afternoonSlots = listOf(
    TimeSlot("01:00 PM", "Afternoon", true),
    TimeSlot("01:30 PM", "Afternoon", true),
    TimeSlot("02:00 PM", "Afternoon", false),
    TimeSlot("02:30 PM", "Afternoon", true),
    TimeSlot("03:00 PM", "Afternoon", true),
    TimeSlot("03:30 PM", "Afternoon", false)
)

val eveningSlots = listOf(
    TimeSlot("05:00 PM", "Evening", true),
    TimeSlot("05:30 PM", "Evening", false),
    TimeSlot("06:00 PM", "Evening", true),
    TimeSlot("06:30 PM", "Evening", true),
    TimeSlot("07:00 PM", "Evening", false),
    TimeSlot("07:30 PM", "Evening", true)
)

// ─── Color Palette ───────────────────────────────────────────────────────────

object AppColors {
    val Primary = Color(0xFF0B3D91)
    val PrimaryLight = Color(0xFF1565C0)
    val PrimaryContainer = Color(0xFFDCE8FF)
    val Secondary = Color(0xFF00897B)
    val SecondaryContainer = Color(0xFFB2DFDB)
    val Background = Color(0xFFF4F7FF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF0F4FF)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF0D1B3E)
    val OnSurface = Color(0xFF1A2744)
    val Outline = Color(0xFFCED8F5)
    val Success = Color(0xFF2E7D32)
    val SuccessContainer = Color(0xFFE8F5E9)
    val Warning = Color(0xFFF57F17)
    val Error = Color(0xFFC62828)
    val StarGold = Color(0xFFFFC107)
    val TextSecondary = Color(0xFF5A6A8A)
    val GradientStart = Color(0xFF0B3D91)
    val GradientEnd = Color(0xFF1976D2)
    val CardShadow = Color(0x1A0B3D91)
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookAppointmentScreen(
    onBackClick: () -> Unit = {},
    onAppointmentBooked: (Doctor, LocalDate, TimeSlot) -> Unit = { _, _, _ -> },
    onBookingSuccess: (String, String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    // ── ViewModel & state ─────────────────────────────────────────────────
    val viewModel: BookAppointmentViewModel = viewModel()
    val bookingState by viewModel.bookingState.collectAsState()
    val context = LocalContext.current

    // ── UI state ──────────────────────────────────────────────────────────
    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("All") }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedSlot by remember { mutableStateOf<TimeSlot?>(null) }
    var showSummary by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val filteredDoctors = mockDoctors.filter { doctor ->
        val matchesDepartment =
            selectedDepartment == "All" || doctor.department == selectedDepartment
        val matchesSearch = searchQuery.isEmpty() ||
                doctor.name.contains(searchQuery, ignoreCase = true) ||
                doctor.specialization.contains(searchQuery, ignoreCase = true)
        matchesDepartment && matchesSearch
    }

    val summaryVisible = selectedDoctor != null && selectedDate != null && selectedSlot != null

    // ── Observe booking result ────────────────────────────────────────────
    LaunchedEffect(bookingState) {
        when (val state = bookingState) {
            is BookingState.Success -> {
                val appt = state.appointment
                onBookingSuccess(
                    appt.doctorName,
                    appt.specialization,
                    appt.department,
                    appt.hospital,
                    appt.date,
                    appt.time,
                    appt.consultationFee.toString()
                )
                viewModel.resetState()
            }
            is BookingState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // ── Scaffold ──────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            AppointmentTopBar(onBackClick = onBackClick, onSearchClick = {})
        },
        containerColor = AppColors.Background
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {

            // ── Main scrollable content ───────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    bottom = if (summaryVisible) 220.dp else 24.dp
                )
            ) {
                // Search & Filter
                item {
                    SearchFilterSection(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onFilterClick = { showFilterSheet = true }
                    )
                }

                // Department Chips
                item {
                    DepartmentChipsSection(
                        departments = departments,
                        selected = selectedDepartment,
                        onSelect = { selectedDepartment = it }
                    )
                }

                // Section Header
                item {
                    SectionHeader(
                        title = "Available Doctors",
                        subtitle = "${filteredDoctors.size} doctors found",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // Doctor Cards
                items(filteredDoctors, key = { it.id }) { doctor ->
                    DoctorCard(
                        doctor = doctor,
                        isSelected = selectedDoctor?.id == doctor.id,
                        onClick = {
                            selectedDoctor =
                                if (selectedDoctor?.id == doctor.id) null else doctor
                            selectedDate = null
                            selectedSlot = null
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .animateItemPlacement()
                    )
                }

                // Date Picker (shown when doctor selected)
                if (selectedDoctor != null) {
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            DatePickerSection(
                                selectedDate = selectedDate,
                                onDateSelected = {
                                    selectedDate = it
                                    selectedSlot = null
                                },
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                    }
                }

                // Time Slots (shown when date selected)
                if (selectedDoctor != null && selectedDate != null) {
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            TimeSlotsSection(
                                selectedSlot = selectedSlot,
                                onSlotSelect = { selectedSlot = it },
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                    }
                }
            } // end LazyColumn

            // ── Floating summary card ─────────────────────────────────────
            AnimatedVisibility(
                visible = summaryVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                AppointmentSummaryCard(
                    doctor = selectedDoctor,
                    date = selectedDate,
                    slot = selectedSlot,
                    onBook = { showSummary = true }
                )
            }

            // ── Loading overlay ───────────────────────────────────────────
            if (bookingState is BookingState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = AppColors.Primary)
                            Text(
                                text = "Confirming your booking...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppColors.OnSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            } // end loading overlay

        } // end Box
    } // end Scaffold

    // ── Booking confirmation dialog ────────────────────────────────────────
    if (showSummary) {
        BookingConfirmationDialog(
            doctor = selectedDoctor,
            date = selectedDate,
            slot = selectedSlot,
            onDismiss = { showSummary = false },
            onConfirm = {
                showSummary = false
                selectedDoctor?.let { d ->
                    selectedDate?.let { date ->
                        selectedSlot?.let { s ->
                            viewModel.bookAppointment(d, date, s)
                        }
                    }
                }
            }
        )
    }

} // end BookAppointmentScreen ← THIS WAS THE MISSING BRACE

// ─── Top App Bar ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentTopBar(onBackClick: () -> Unit, onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(AppColors.GradientStart, AppColors.GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Book Appointment",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Apollo Smart Hospital",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

// ─── Search & Filter ─────────────────────────────────────────────────────────

@Composable
fun SearchFilterSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Search doctor or specialization...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AppColors.TextSecondary
                    )
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = AppColors.Primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = AppColors.TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Outline,
                focusedContainerColor = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface
            ),
            singleLine = true
        )

        Surface(
            onClick = onFilterClick,
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Primary,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ─── Department Chips ─────────────────────────────────────────────────────────

@Composable
fun DepartmentChipsSection(
    departments: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val departmentIcons = mapOf(
        "All" to Icons.Default.GridView,
        "Cardiology" to Icons.Default.Favorite,
        "Neurology" to Icons.Default.Psychology,
        "Orthopedics" to Icons.Default.AccessibilityNew,
        "General Medicine" to Icons.Default.LocalHospital,
        "Pediatrics" to Icons.Default.ChildCare,
        "Dermatology" to Icons.Default.Face
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(departments) { dept ->
            val isSelected = dept == selected
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(200),
                label = "chip_alpha"
            )

            Surface(
                onClick = { onSelect(dept) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) AppColors.Primary else AppColors.Surface,
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) AppColors.Primary else AppColors.Outline
                ),
                shadowElevation = if (isSelected) 4.dp else 0.dp,
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = departmentIcons[dept] ?: Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else AppColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = dept,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else AppColors.TextSecondary
                        )
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AppColors.TextSecondary
                )
            )
        }
        Text(
            text = "View All",
            style = MaterialTheme.typography.labelMedium.copy(
                color = AppColors.Primary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

// ─── Doctor Card ──────────────────────────────────────────────────────────────

@Composable
fun DoctorCard(
    doctor: Doctor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Primary else Color.Transparent,
        animationSpec = tween(250),
        label = "border_color"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(250),
        label = "elevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    doctor.avatarColor,
                                    doctor.avatarColor.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = doctor.avatarInitials,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 22.sp
                        )
                    )
                    if (doctor.isAvailable) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AppColors.Success)
                                .border(2.dp, AppColors.Surface, CircleShape)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doctor.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.OnSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = doctor.specialization,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AppColors.Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (doctor.isAvailable)
                                AppColors.SuccessContainer else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (doctor.isAvailable) "Available" else "Busy",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (doctor.isAvailable)
                                        AppColors.Success else AppColors.Error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.StarGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = doctor.rating.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppColors.OnSurface
                            )
                        )
                        Text(
                            text = "(${doctor.reviewCount})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AppColors.TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Outlined.WorkHistory,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${doctor.experience} yrs",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AppColors.TextSecondary
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = AppColors.Outline.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = doctor.hospital,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AppColors.TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AppColors.PrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CurrencyRupee,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${doctor.consultationFee}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Primary
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSelected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.PrimaryContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Doctor Selected – Choose Date & Time Below",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AppColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

// ─── Date Picker ──────────────────────────────────────────────────────────────

@Composable
fun DatePickerSection(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val availableDays = setOf(0, 1, 3, 5, 7, 8, 10, 12, 14)
    val dates = (0..13).map { today.plusDays(it.toLong()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Select Date",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.OnSurface
                        )
                    )
                }
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AppColors.TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(dates) { index, date ->
                    val isSelected = date == selectedDate
                    val isAvailable = index in availableDays
                    val dayName = date.format(DateTimeFormatter.ofPattern("EEE"))
                    val dayNum = date.dayOfMonth.toString()

                    val bgColor = when {
                        isSelected -> AppColors.Primary
                        isAvailable -> AppColors.SurfaceVariant
                        else -> Color(0xFFF5F5F5)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .then(
                                if (isAvailable) Modifier.clickable { onDateSelected(date) }
                                else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when {
                                    isSelected -> Color.White.copy(alpha = 0.85f)
                                    isAvailable -> AppColors.TextSecondary
                                    else -> AppColors.TextSecondary.copy(alpha = 0.4f)
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNum,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isSelected -> Color.White
                                    isAvailable -> AppColors.OnSurface
                                    else -> AppColors.TextSecondary.copy(alpha = 0.4f)
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> Color.White
                                        isAvailable -> AppColors.Success
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                LegendItem(color = AppColors.Success, label = "Available")
                LegendItem(color = AppColors.Primary, label = "Selected")
                LegendItem(color = Color(0xFFE0E0E0), label = "Unavailable")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = AppColors.TextSecondary
            )
        )
    }
}

// ─── Time Slots ───────────────────────────────────────────────────────────────

@Composable
fun TimeSlotsSection(
    selectedSlot: TimeSlot?,
    onSlotSelect: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Select Time Slot",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TimeSlotGroup(
                title = "🌅 Morning",
                slots = morningSlots,
                selectedSlot = selectedSlot,
                onSlotSelect = onSlotSelect
            )

            Spacer(modifier = Modifier.height(14.dp))

            TimeSlotGroup(
                title = "☀️ Afternoon",
                slots = afternoonSlots,
                selectedSlot = selectedSlot,
                onSlotSelect = onSlotSelect
            )

            Spacer(modifier = Modifier.height(14.dp))

            TimeSlotGroup(
                title = "🌆 Evening",
                slots = eveningSlots,
                selectedSlot = selectedSlot,
                onSlotSelect = onSlotSelect
            )
        }
    }
}

@Composable
fun TimeSlotGroup(
    title: String,
    slots: List<TimeSlot>,
    selectedSlot: TimeSlot?,
    onSlotSelect: (TimeSlot) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextSecondary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(slots) { slot ->
                val isSelected = slot == selectedSlot
                val bgColor = when {
                    isSelected -> AppColors.Primary
                    slot.isAvailable -> AppColors.Surface
                    else -> Color(0xFFF5F5F5)
                }
                val borderColor = when {
                    isSelected -> AppColors.Primary
                    slot.isAvailable -> AppColors.Outline
                    else -> Color.Transparent
                }

                Surface(
                    onClick = { if (slot.isAvailable) onSlotSelect(slot) },
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    border = BorderStroke(1.dp, borderColor),
                    enabled = slot.isAvailable
                ) {
                    Text(
                        text = slot.time,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected -> Color.White
                                slot.isAvailable -> AppColors.OnSurface
                                else -> AppColors.TextSecondary.copy(alpha = 0.4f)
                            }
                        )
                    )
                }
            }
        }
    }
}

// ─── Appointment Summary ──────────────────────────────────────────────────────

@Composable
fun AppointmentSummaryCard(
    doctor: Doctor?,
    date: LocalDate?,
    slot: TimeSlot?,
    onBook: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Surface,
        shadowElevation = 24.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Outline)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Appointment Summary",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnSurface
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.SurfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    icon = Icons.Default.Person,
                    label = "Doctor",
                    value = doctor?.name?.substringAfter("Dr. ")?.split(" ")?.firstOrNull()
                        ?.let { "Dr. $it" } ?: "—"
                )
                SummaryItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Date",
                    value = date?.format(DateTimeFormatter.ofPattern("d MMM")) ?: "—"
                )
                SummaryItem(
                    icon = Icons.Default.Schedule,
                    label = "Time",
                    value = slot?.time ?: "—"
                )
                SummaryItem(
                    icon = Icons.Outlined.CurrencyRupee,
                    label = "Fee",
                    value = "₹${doctor?.consultationFee ?: "—"}"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBook,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm Appointment",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SummaryItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = AppColors.Primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AppColors.OnSurface
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = AppColors.TextSecondary
            )
        )
    }
}

// ─── Booking Confirmation Dialog ──────────────────────────────────────────────

@Composable
fun BookingConfirmationDialog(
    doctor: Doctor?,
    date: LocalDate?,
    slot: TimeSlot?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AppColors.SuccessContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Success,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Confirm Booking",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.SurfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConfirmRow("Doctor", doctor?.name ?: "—", Icons.Default.Person)
                ConfirmRow("Department", doctor?.department ?: "—", Icons.Default.MedicalServices)
                ConfirmRow(
                    "Date",
                    date?.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")) ?: "—",
                    Icons.Default.CalendarToday
                )
                ConfirmRow("Time", slot?.time ?: "—", Icons.Default.Schedule)
                ConfirmRow("Hospital", doctor?.hospital ?: "—", Icons.Outlined.LocationOn)
                HorizontalDivider(color = AppColors.Outline)
                ConfirmRow(
                    "Consultation Fee",
                    "₹${doctor?.consultationFee}",
                    Icons.Outlined.CurrencyRupee,
                    valueColor = AppColors.Primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) {
                Text(
                    "Book Appointment",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AppColors.Outline)
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = AppColors.TextSecondary
                    )
                )
            }
        }
    )
}

@Composable
fun ConfirmRow(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = AppColors.OnSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AppColors.TextSecondary
                )
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 180.dp)
        )
    }
}