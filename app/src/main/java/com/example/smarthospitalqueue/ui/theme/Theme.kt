package com.example.smarthospitalqueue.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
// Smart Hospital Queue Management System — Theme
// ─────────────────────────────────────────────────────────────────────────────

// ═════════════════════════════════════════════════════════════════════════════
// 1. Light Color Scheme
// ═════════════════════════════════════════════════════════════════════════════
private val LightColorScheme = lightColorScheme(

    // ── Primary ──────────────────────────────────────────────────────────────
    primary                 = Blue40,          // Main brand blue
    onPrimary               = Neutral100,      // White on blue
    primaryContainer        = Blue90,          // Soft blue chip / button bg
    onPrimaryContainer      = Blue10,          // Dark text on blue chip

    // ── Secondary ────────────────────────────────────────────────────────────
    secondary               = Teal40,          // Clinical teal
    onSecondary             = Neutral100,
    secondaryContainer      = Teal90,          // Teal tinted surface
    onSecondaryContainer    = Teal10,

    // ── Tertiary ─────────────────────────────────────────────────────────────
    tertiary                = Indigo40,        // Accent indigo (wayfinding)
    onTertiary              = Neutral100,
    tertiaryContainer       = Indigo90,
    onTertiaryContainer     = Indigo10,

    // ── Error ────────────────────────────────────────────────────────────────
    error                   = Red40,
    onError                 = Neutral100,
    errorContainer          = Red90,
    onErrorContainer        = Red10,

    // ── Background & Surface ─────────────────────────────────────────────────
    background              = Blue99,          // Near-white with blue tint
    onBackground            = Neutral10,

    surface                 = Neutral99,       // Card / sheet surface
    onSurface               = Neutral10,

    surfaceVariant          = NeutralVariant95,// Subtle tinted surface
    onSurfaceVariant        = NeutralVariant30,

    // ── Outline ──────────────────────────────────────────────────────────────
    outline                 = NeutralVariant50,
    outlineVariant          = NeutralVariant80,

    // ── Inverse ──────────────────────────────────────────────────────────────
    inverseSurface          = Neutral20,
    inverseOnSurface        = Neutral95,
    inversePrimary          = Blue80,

    // ── Scrim / Overlay ──────────────────────────────────────────────────────
    scrim                   = ScrimColor,

    // ── Surface Container Hierarchy ──────────────────────────────────────────
    // Used by navigation drawers, bottom sheets, cards, dialogs
    surfaceDim              = Neutral87,
    surfaceBright           = Neutral98,
    surfaceContainerLowest  = Neutral100,
    surfaceContainerLow     = Neutral96,
    surfaceContainer        = Neutral94,
    surfaceContainerHigh    = Neutral92,
    surfaceContainerHighest = Neutral90,
)

// ═════════════════════════════════════════════════════════════════════════════
// 2. Dark Color Scheme
// ═════════════════════════════════════════════════════════════════════════════
private val DarkColorScheme = darkColorScheme(

    // ── Primary ──────────────────────────────────────────────────────────────
    primary                 = Blue80,          // Soft blue on dark
    onPrimary               = Blue20,
    primaryContainer        = Blue30,          // Deeper blue container
    onPrimaryContainer      = Blue90,

    // ── Secondary ────────────────────────────────────────────────────────────
    secondary               = Teal80,
    onSecondary             = Teal20,
    secondaryContainer      = Teal30,
    onSecondaryContainer    = Teal90,

    // ── Tertiary ─────────────────────────────────────────────────────────────
    tertiary                = Indigo80,
    onTertiary              = Indigo20,
    tertiaryContainer       = Indigo30,
    onTertiaryContainer     = Indigo90,

    // ── Error ────────────────────────────────────────────────────────────────
    error                   = Red80,
    onError                 = Red20,
    errorContainer          = Red30,
    onErrorContainer        = Red90,

    // ── Background & Surface ─────────────────────────────────────────────────
    background              = Neutral6,        // Deep dark background
    onBackground            = Neutral90,

    surface                 = Neutral6,
    onSurface               = Neutral90,

    surfaceVariant          = NeutralVariant30,
    onSurfaceVariant        = NeutralVariant80,

    // ── Outline ──────────────────────────────────────────────────────────────
    outline                 = NeutralVariant60,
    outlineVariant          = NeutralVariant30,

    // ── Inverse ──────────────────────────────────────────────────────────────
    inverseSurface          = Neutral90,
    inverseOnSurface        = Neutral20,
    inversePrimary          = Blue40,

    // ── Scrim ────────────────────────────────────────────────────────────────
    scrim                   = ScrimColor,

    // ── Surface Container Hierarchy ──────────────────────────────────────────
    surfaceDim              = Neutral6,
    surfaceBright           = Neutral24,
    surfaceContainerLowest  = Neutral4,
    surfaceContainerLow     = Neutral10,
    surfaceContainer        = Neutral12,
    surfaceContainerHigh    = Neutral17,
    surfaceContainerHighest = Neutral22,
)

// ═════════════════════════════════════════════════════════════════════════════
// 3. Extended Colors
//    Domain-specific tokens not covered by Material 3 baseline scheme.
//    Access via: MaterialTheme.extendedColors.statusWaiting etc.
// ═════════════════════════════════════════════════════════════════════════════

@Immutable
data class ExtendedColors(
    // Queue status indicators
    val statusWaiting: Color,
    val onStatusWaiting: Color,
    val statusWaitingContainer: Color,

    val statusInProgress: Color,
    val onStatusInProgress: Color,
    val statusInProgressContainer: Color,

    val statusCompleted: Color,
    val onStatusCompleted: Color,
    val statusCompletedContainer: Color,

    val statusCancelled: Color,
    val onStatusCancelled: Color,
    val statusCancelledContainer: Color,

    val statusUrgent: Color,
    val onStatusUrgent: Color,
    val statusUrgentContainer: Color,

    // Card surfaces
    val cardSurface: Color,
    val cardBorder: Color,

    // Divider
    val divider: Color,
)

private val LightExtendedColors = ExtendedColors(
    statusWaiting           = Amber40,
    onStatusWaiting         = Neutral100,
    statusWaitingContainer  = Amber90,

    statusInProgress        = Blue40,
    onStatusInProgress      = Neutral100,
    statusInProgressContainer = Blue90,

    statusCompleted         = Green40,
    onStatusCompleted       = Neutral100,
    statusCompletedContainer = Green90,

    statusCancelled         = Red40,
    onStatusCancelled       = Neutral100,
    statusCancelledContainer = Red90,

    statusUrgent            = StatusUrgent,
    onStatusUrgent          = Neutral100,
    statusUrgentContainer   = Red90,

    cardSurface             = CardSurfaceLight,
    cardBorder              = CardBorderLight,
    divider                 = DividerLight,
)

private val DarkExtendedColors = ExtendedColors(
    statusWaiting           = Amber80,
    onStatusWaiting         = Amber20,
    statusWaitingContainer  = Amber40,

    statusInProgress        = Blue80,
    onStatusInProgress      = Blue20,
    statusInProgressContainer = Blue30,

    statusCompleted         = Green80,
    onStatusCompleted       = Green20,
    statusCompletedContainer = Green30,

    statusCancelled         = Red80,
    onStatusCancelled       = Red20,
    statusCancelledContainer = Red30,

    statusUrgent            = Red80,
    onStatusUrgent          = Red20,
    statusUrgentContainer   = Red30,

    cardSurface             = CardSurfaceDark,
    cardBorder              = CardBorderDark,
    divider                 = DividerDark,
)

// CompositionLocal for extended colors
val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

// Extension property for clean access inside Composables:
// MaterialTheme.extendedColors.statusWaiting
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

// ═════════════════════════════════════════════════════════════════════════════
// 4. Shapes (Rounded UI feel)
// ═════════════════════════════════════════════════════════════════════════════
// Material 3 default shapes already use rounded corners; we can import
// and customise via MaterialTheme.shapes if needed.
// For this theme we rely on the Material 3 defaults which provide:
//   ExtraSmall  →  4 dp
//   Small       →  8 dp
//   Medium      → 12 dp
//   Large       → 16 dp
//   ExtraLarge  → 28 dp
//   Full        → Circle
//
// To override, uncomment and pass `shapes = HospitalShapes` to MaterialTheme:
//
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.material3.Shapes
//
// val HospitalShapes = Shapes(
//     extraSmall  = RoundedCornerShape(6.dp),
//     small       = RoundedCornerShape(10.dp),
//     medium      = RoundedCornerShape(16.dp),
//     large       = RoundedCornerShape(20.dp),
//     extraLarge  = RoundedCornerShape(28.dp),
// )

// ═════════════════════════════════════════════════════════════════════════════
// 5. Root Theme Composable
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Root theme for Smart Hospital Queue Management System.
 *
 * @param darkTheme         Force dark mode; defaults to system setting.
 * @param dynamicColor      Enable Material You / dynamic color on Android 12+.
 *                          Recommended: false for branded healthcare apps so
 *                          the clinical color language is preserved.
 * @param content           Composable content inside the theme.
 */
@Composable
fun SmartHospitalQueueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // Keep false to preserve brand palette
    content: @Composable () -> Unit,
) {
    // ── Color scheme resolution ───────────────────────────────────────────────
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // ── System bars styling ───────────────────────────────────────────────────
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Enable edge-to-edge rendering
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Status bar color — transparent so the app background shows through
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()

            // Navigation bar color — transparent for edge-to-edge
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()

            // Light / dark status bar icons
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars    = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // ── Provide theme ─────────────────────────────────────────────────────────
    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = SmartHospitalTypography,
            // shapes   = HospitalShapes,  // Uncomment if using custom shapes
            content     = content,
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 6. Preview Helpers
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Lightweight wrapper for @Preview composables.
 * Example:
 *
 * @Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO)
 * @Preview(name = "Dark",  uiMode = UI_MODE_NIGHT_YES)
 * @Composable
 * private fun QueueCardPreview() {
 *     SmartHospitalPreviewTheme { QueueCard(...) }
 * }
 */
@Composable
fun SmartHospitalPreviewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    SmartHospitalQueueTheme(
        darkTheme    = darkTheme,
        dynamicColor = false,
        content      = content,
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. Design Token Reference Card (for developers)
// ═════════════════════════════════════════════════════════════════════════════
//
// ┌─────────────────────────────────────────────────────────────────────────┐
// │  UI Element                │ Token (MaterialTheme.colorScheme.*)         │
// ├─────────────────────────────────────────────────────────────────────────┤
// │  Primary action button     │ primary / onPrimary                         │
// │  Filled card               │ surfaceContainerLow                         │
// │  Elevated card             │ surfaceContainerHigh                        │
// │  Bottom navigation bar     │ surfaceContainer                            │
// │  Top app bar               │ surfaceContainerHigh                        │
// │  Navigation drawer         │ surfaceContainerLow                         │
// │  Dialog / bottom sheet     │ surfaceContainerHighest                     │
// │  Search bar                │ surfaceContainerHigh                        │
// │  Chip (selected)           │ secondaryContainer / onSecondaryContainer   │
// │  Chip (unselected)         │ surface / onSurfaceVariant                  │
// │  Text field outline        │ outline                                     │
// │  Dividers                  │ extendedColors.divider                      │
// │  Queue: Waiting            │ extendedColors.statusWaiting(Container)     │
// │  Queue: In Progress        │ extendedColors.statusInProgress(Container)  │
// │  Queue: Completed          │ extendedColors.statusCompleted(Container)   │
// │  Queue: Cancelled          │ extendedColors.statusCancelled(Container)   │
// │  Queue: Urgent / Emergency │ extendedColors.statusUrgent(Container)      │
// └─────────────────────────────────────────────────────────────────────────┘
