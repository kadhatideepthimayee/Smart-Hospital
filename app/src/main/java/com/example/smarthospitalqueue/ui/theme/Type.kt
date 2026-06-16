package com.example.smarthospitalqueue.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// Smart Hospital Queue Management System — Typography
//
// Font strategy:
//   Display / Headline  → Inter (clean, legible, enterprise-grade)
//   Body / Label        → Inter (consistent reading rhythm)
//
// To use Inter, add the font files to res/font/ OR switch to
// GoogleFonts provider (see companion comment at bottom of file).
//
// If you prefer zero-dependency setup, this file falls back gracefully
// to the system default sans-serif — which on Android 12+ is Google Sans,
// still an excellent choice for medical UIs.
// ─────────────────────────────────────────────────────────────────────────────

// ── Font Family Declaration ───────────────────────────────────────────────────
// Option A: Bundled font files in res/font/
//   Uncomment the block below and add the .ttf / .otf files to res/font/.
//
// val InterFontFamily = FontFamily(
//     Font(R.font.inter_thin,        FontWeight.Thin),
//     Font(R.font.inter_extralight,  FontWeight.ExtraLight),
//     Font(R.font.inter_light,       FontWeight.Light),
//     Font(R.font.inter_regular,     FontWeight.Normal),
//     Font(R.font.inter_medium,      FontWeight.Medium),
//     Font(R.font.inter_semibold,    FontWeight.SemiBold),
//     Font(R.font.inter_bold,        FontWeight.Bold),
//     Font(R.font.inter_extrabold,   FontWeight.ExtraBold),
//     Font(R.font.inter_black,       FontWeight.Black),
// )

// Option B: System default (production-safe fallback, no assets needed)
val InterFontFamily = FontFamily.Default

// ─────────────────────────────────────────────────────────────────────────────
// Material 3 Type Scale
// Reference: https://m3.material.io/styles/typography/type-scale-tokens
// ─────────────────────────────────────────────────────────────────────────────

val SmartHospitalTypography = Typography(

    // ── Display ──────────────────────────────────────────────────────────────
    // Used for: Hero sections, large numeric counters (queue number display)
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),

    // ── Headline ─────────────────────────────────────────────────────────────
    // Used for: Screen titles, card headers, section headings
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // ── Title ────────────────────────────────────────────────────────────────
    // Used for: List item headers, dialog titles, tab labels, app bar titles
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body ─────────────────────────────────────────────────────────────────
    // Used for: Patient info, descriptions, queue details, forms
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // ── Label ────────────────────────────────────────────────────────────────
    // Used for: Buttons, chips, badges, status tags, navigation items
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// Extended / Semantic Text Styles
// Reference these directly for domain-specific UI elements.
// ─────────────────────────────────────────────────────────────────────────────

/** Large queue token number shown on kiosk / patient screens */
val QueueTokenDisplay = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 72.sp,
    lineHeight = 80.sp,
    letterSpacing = (-1).sp,
)

/** Estimated wait time counter */
val WaitTimeLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.5).sp,
)

/** Department / specialty label (e.g. "Cardiology", "OPD") */
val DepartmentLabel = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.8.sp,
)

/** Status chip text (e.g. "WAITING", "IN PROGRESS") */
val StatusChipText = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.2.sp,
)

/** Patient name on queue cards */
val PatientNameStyle = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.15.sp,
)

/** Small metadata (timestamps, IDs, MR numbers) */
val MetaDataStyle = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
)

// ─────────────────────────────────────────────────────────────────────────────
// GoogleFonts alternative (add dependency: compose-material3-adaptive-navigation-suite)
// ─────────────────────────────────────────────────────────────────────────────
// If you prefer to pull Inter from Google Fonts at runtime, add this to
// your build.gradle(:app):
//
//   implementation "androidx.compose.ui:ui-text-google-fonts:<version>"
//
// Then replace InterFontFamily with:
//
// import androidx.compose.ui.text.googlefonts.Font
// import androidx.compose.ui.text.googlefonts.GoogleFont
//
// val provider = GoogleFont.Provider(
//     providerAuthority = "com.google.android.gms.fonts",
//     providerPackage   = "com.google.android.gms",
//     certificates      = R.array.com_google_android_gms_fonts_certs,
// )
// val InterFont = GoogleFont("Inter")
// val InterFontFamily = FontFamily(
//     Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
//     Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
//     Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold),
//     Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Bold),
// )
