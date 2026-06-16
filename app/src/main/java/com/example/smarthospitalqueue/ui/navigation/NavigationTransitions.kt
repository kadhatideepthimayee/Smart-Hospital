package com.example.smarthospitalqueue.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

// ─────────────────────────────────────────────────────────────────────────────
// Navigation Transition Presets
// ─────────────────────────────────────────────────────────────────────────────

private const val SLIDE_DURATION_MS = 380
private const val FADE_DURATION_MS  = 260

// ── Horizontal slide (Login → Dashboard) ─────────────────────────────────────

/** Entering a new screen by sliding in from the right (push). */
val slideInFromRight: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec  = tween(SLIDE_DURATION_MS, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(FADE_DURATION_MS))

/** Exiting the current screen to the left during a push. */
val slideOutToLeft: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(SLIDE_DURATION_MS, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(FADE_DURATION_MS))

/** Entering the previous screen from the left (pop / back). */
val slideInFromLeft: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec  = tween(SLIDE_DURATION_MS, easing = EaseInOut),
    ) + fadeIn(animationSpec = tween(FADE_DURATION_MS))

/** Exiting the current screen to the right during a pop. */
val slideOutToRight: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(SLIDE_DURATION_MS, easing = EaseInOut),
    ) + fadeOut(animationSpec = tween(FADE_DURATION_MS))

// ── Fade only (tab switches / Dashboard) ─────────────────────────────────────

val fadeInTransition: EnterTransition =
    fadeIn(animationSpec = tween(FADE_DURATION_MS))

val fadeOutTransition: ExitTransition =
    fadeOut(animationSpec = tween(FADE_DURATION_MS))

// ── No transition (Splash → Login instant swap) ───────────────────────────────

val noEnter: EnterTransition = EnterTransition.None
val noExit:  ExitTransition  = ExitTransition.None