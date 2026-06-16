package com.example.smarthospitalqueue.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.smarthospitalqueue.ui.theme.SmartHospitalQueueTheme

/**
 * SplashScreen for Smart Hospital Queue Management System.
 *
 * Displays a full-screen branded splash with:
 * - Smooth fade-in entrance animation
 * - Logo placeholder with "SH" initials
 * - App title and subtitle
 * - Animated circular loading indicator
 *
 * Usage: Call [SplashScreen] as your start destination in NavHost and
 * pass an [onSplashFinished] callback to navigate to the next screen
 * once the delay completes.
 *
 * @param onSplashFinished Lambda invoked after the splash delay has elapsed.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    // ── Animation states ──────────────────────────────────────────────────────
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.80f) }
    val logoScaleAnim = remember { Animatable(0.70f) }

    LaunchedEffect(Unit) {
        // Staggered entrance: logo first, then content
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = EaseOutCubic)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = EaseOutBack)
        )
        logoScaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutBack)
        )
        delay(1_800L)
        onSplashFinished()
    }

    // ── Root surface ──────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Subtle decorative circles for depth
        DecorativeBackground()

        // ── Main content column ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo container ────────────────────────────────────────────────
            LogoPlaceholder(
                modifier = Modifier.scale(logoScaleAnim.value)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Divider accent ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f)
                    )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── App title ─────────────────────────────────────────────────────
            Text(
                text = "Smart Hospital",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 38.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Subtitle ──────────────────────────────────────────────────────
            Text(
                text = "Queue Management System",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // ── Loading indicator ─────────────────────────────────────────────
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.20f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Loading, please wait…",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 0.4.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.60f)
            )
        }

        // ── Footer version label ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim.value)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v1.0.0  •  Powered by SmartHealth",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Logo Placeholder
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Circular logo placeholder displaying the "SH" initials.
 * Replace this composable with your actual [Image] or vector asset when ready.
 */
@Composable
private fun LogoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SH",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Decorative Background Circles
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders large, semi-transparent circles behind the content to add
 * visual depth without distracting from the main UI.
 */
@Composable
private fun DecorativeBackground() {
    val overlayColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f)

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left large circle
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-100).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(overlayColor)
        )

        // Bottom-right large circle
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 100.dp)
                .clip(CircleShape)
                .background(overlayColor)
        )

        // Mid-left small circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-40).dp, y = 80.dp)
                .clip(CircleShape)
                .background(overlayColor)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {

    SmartHospitalQueueTheme {
        SplashScreen()
    }
}