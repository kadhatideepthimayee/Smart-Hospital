package com.example.smarthospitalqueue.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Smart Hospital Queue Management System — Color Palette
// Design language: Clinical Clarity · Calm Authority · Trusted Care
// ─────────────────────────────────────────────────────────────────────────────

// ── Primary · Medical Blue ───────────────────────────────────────────────────
val Blue10  = Color(0xFF001E3C)
val Blue20  = Color(0xFF003566)
val Blue30  = Color(0xFF004E8C)
val Blue40  = Color(0xFF0068B5)   // Light primary
val Blue50  = Color(0xFF0080D9)
val Blue60  = Color(0xFF3399E6)
val Blue70  = Color(0xFF66B3F0)
val Blue80  = Color(0xFF99CCF8)   // Dark primary
val Blue90  = Color(0xFFCCE5FC)
val Blue95  = Color(0xFFE8F4FF)
val Blue99  = Color(0xFFF5FAFF)

// ── Secondary · Clinical Teal ────────────────────────────────────────────────
val Teal10  = Color(0xFF001F1E)
val Teal20  = Color(0xFF003735)
val Teal30  = Color(0xFF00514F)
val Teal40  = Color(0xFF006C6A)   // Light secondary
val Teal50  = Color(0xFF008784)
val Teal60  = Color(0xFF00A3A0)
val Teal70  = Color(0xFF00BFBB)
val Teal80  = Color(0xFF4DD8D5)   // Dark secondary
val Teal90  = Color(0xFFB2F0EE)
val Teal95  = Color(0xFFD6FAF9)
val Teal99  = Color(0xFFF0FFFE)

// ── Tertiary · Soft Indigo (Status / Wayfinding) ────────────────────────────
val Indigo10 = Color(0xFF0A0060)
val Indigo20 = Color(0xFF150090)
val Indigo30 = Color(0xFF2800B5)
val Indigo40 = Color(0xFF4023CC)  // Light tertiary
val Indigo50 = Color(0xFF5B40E0)
val Indigo60 = Color(0xFF7A63EC)
val Indigo70 = Color(0xFF9B87F5)
val Indigo80 = Color(0xFFBDB0FF)  // Dark tertiary
val Indigo90 = Color(0xFFDDD8FF)
val Indigo95 = Color(0xFFEEEBFF)
val Indigo99 = Color(0xFFF8F7FF)

// ── Error · Alert Red ────────────────────────────────────────────────────────
val Red10  = Color(0xFF410002)
val Red20  = Color(0xFF690005)
val Red30  = Color(0xFF93000A)
val Red40  = Color(0xFFBA1A1A)
val Red80  = Color(0xFFFFB4AB)
val Red90  = Color(0xFFFFDAD6)

// ── Success · Healing Green ──────────────────────────────────────────────────
val Green10 = Color(0xFF002109)
val Green20 = Color(0xFF00390F)
val Green30 = Color(0xFF005318)
val Green40 = Color(0xFF006E24)  // Confirmed / discharged
val Green60 = Color(0xFF4CAF66)
val Green80 = Color(0xFF96D9A9)
val Green90 = Color(0xFFB8F5C8)

// ── Warning · Caution Amber ──────────────────────────────────────────────────
val Amber10 = Color(0xFF2B1700)
val Amber20 = Color(0xFF4A2800)
val Amber40 = Color(0xFF8B4800)  // Pending / waiting
val Amber60 = Color(0xFFE08000)
val Amber80 = Color(0xFFFFBB66)
val Amber90 = Color(0xFFFFDDB3)

// ── Neutral Surfaces ─────────────────────────────────────────────────────────
val Neutral0   = Color(0xFF000000)
val Neutral4   = Color(0xFF0C0E10)
val Neutral6   = Color(0xFF111417)
val Neutral10  = Color(0xFF1A1C1E)
val Neutral12  = Color(0xFF1E2124)
val Neutral17  = Color(0xFF272A2D)
val Neutral20  = Color(0xFF2E3134)
val Neutral22  = Color(0xFF323538)
val Neutral24  = Color(0xFF37393C)
val Neutral25  = Color(0xFF393C3F)
val Neutral30  = Color(0xFF44474A)
val Neutral35  = Color(0xFF505356)
val Neutral40  = Color(0xFF5C5F62)
val Neutral50  = Color(0xFF75787B)
val Neutral60  = Color(0xFF8F9194)
val Neutral70  = Color(0xFFA9ABAE)
val Neutral80  = Color(0xFFC5C6C9)
val Neutral87  = Color(0xFFD9DADD)
val Neutral90  = Color(0xFFE1E2E5)
val Neutral92  = Color(0xFFE7E8EB)
val Neutral94  = Color(0xFFECEEF1)
val Neutral95  = Color(0xFFF0F1F4)
val Neutral96  = Color(0xFFF2F3F6)
val Neutral98  = Color(0xFFF8F9FC)
val Neutral99  = Color(0xFFFBFCFF)
val Neutral100 = Color(0xFFFFFFFF)

// ── Neutral Variant ──────────────────────────────────────────────────────────
val NeutralVariant30 = Color(0xFF3E4757)
val NeutralVariant50 = Color(0xFF5E6779)
val NeutralVariant60 = Color(0xFF788192)
val NeutralVariant70 = Color(0xFF929BAB)
val NeutralVariant80 = Color(0xFFADB6C6)
val NeutralVariant90 = Color(0xFFD0D8E9)
val NeutralVariant95 = Color(0xFFE6EDF8)

// ─────────────────────────────────────────────────────────────────────────────
// Semantic / Domain-specific Aliases
// ─────────────────────────────────────────────────────────────────────────────

// Queue Status Colors (used in token chips and timeline indicators)
val StatusWaiting    = Amber40
val StatusInProgress = Blue40
val StatusCompleted  = Green40
val StatusCancelled  = Red40
val StatusUrgent     = Color(0xFFCC2929)  // Emergency red

// Card Surface Tints
val CardSurfaceLight    = Neutral99
val CardSurfaceDark     = Neutral22
val CardBorderLight     = NeutralVariant90
val CardBorderDark      = NeutralVariant30

// Status Bar & Navigation Bar
val StatusBarLight      = Blue99
val StatusBarDark       = Neutral6

// Divider Colors
val DividerLight        = Neutral90
val DividerDark         = Neutral24

// Shadow / Scrim
val ScrimColor          = Color(0x99000000)
