package com.example.medplus.ui.splash

import com.example.medplus.ui.auth.*
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

// =====================================================================================
// SPLASH SCREEN (LIGHT THEME ONLY)
// =====================================================================================

/**
 * Splash screen for MedPlus — clean, bright and brand-forward, built for a real hospital
 * app rather than a flashy launch screen. The background stays on the app's standard
 * light background; only the small logo badge carries brand-color gradient.
 *
 * Purely presentational + a single timing side-effect: after [durationMillis] it invokes
 * [onSplashFinished] so the caller can navigate onward (e.g. to Onboarding or Login). No
 * business logic, session checks, or ViewModel wiring live here — the caller decides what
 * "finished" means and where to go next.
 *
 * @param navController Navigation controller, exposed for graph wiring by the caller.
 * @param onSplashFinished Invoked once the splash animation/timer completes.
 * @param durationMillis How long the splash is shown before [onSplashFinished] fires.
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    onSplashFinished: () -> Unit,
    durationMillis: Long = 1800L,
) {
    // ---- Logo entrance: a single gentle scale + fade (no looping/flashy motion) -----
    var logoVisible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splash-logo-scale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "splash-logo-alpha",
    )

    // ---- Overall screen fade-out just before navigating away -----------------------
    var isExiting by remember { mutableStateOf(false) }
    val screenAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(300),
        label = "splash-screen-alpha",
    )

    LaunchedEffect(Unit) {
        logoVisible = true
        delay(durationMillis)
        isExiting = true
        delay(300) // let the fade-out play before handing off navigation
        onSplashFinished()
    }

    Scaffold(containerColor = MedPlusBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MedPlusBackground)
                .graphicsLayer { alpha = screenAlpha },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // ---- Logo badge — brand gradient confined to the small mark only ----
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                            alpha = logoAlpha
                        }
                        .shadow(elevation = 12.dp, shape = CircleShape, spotColor = MedPlusPrimary.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MedPlusPrimary, MedPlusAccent))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalHospital,
                        contentDescription = "MedPlus logo, medical cross icon",
                        tint = MedPlusBackground,
                        modifier = Modifier.size(56.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MedPlus",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MedPlusTextPrimary,
                    modifier = Modifier.graphicsLayer { alpha = logoAlpha },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Smart Hospital Queue Management",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedPlusTextSecondary,
                    modifier = Modifier.graphicsLayer { alpha = logoAlpha },
                )

                Spacer(modifier = Modifier.height(48.dp))

                // ---- Loading indicator ---------------------------------------------
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { alpha = logoAlpha },
                    color = MedPlusPrimary,
                    trackColor = MedPlusBorder,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}

// =====================================================================================
// PREVIEW
// =====================================================================================

@Preview(showBackground = true, showSystemUi = true, name = "Splash Screen — Light")
@Composable
private fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen(
            navController = rememberNavController(),
            onSplashFinished = {},
        )
    }
}