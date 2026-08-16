package com.example.medplus.ui.onboarding

import com.example.medplus.ui.auth.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// =====================================================================================
// ONBOARDING SCREEN
// =====================================================================================

/** A single onboarding page's content — purely presentational data. */
private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Filled.CalendarMonth,
        title = "Book Appointments Instantly",
        description = "Find the right doctor and reserve your slot in seconds — no phone calls, no waiting on hold.",
    ),
    OnboardingPage(
        icon = Icons.Filled.MonitorHeart,
        title = "Track Your Live Queue",
        description = "See exactly where you stand in line and get a realistic estimate of your wait time.",
    ),
    OnboardingPage(
        icon = Icons.Filled.NotificationsActive,
        title = "Stay Informed, Always",
        description = "Get gentle reminders and real-time updates so you're never caught off guard.",
    ),
)

/**
 * Premium 3-page onboarding flow for MedPlus.
 *
 * Purely presentational: owns local pager state for UI purposes only and forwards user
 * intent via callbacks. No persistence (e.g. "has seen onboarding" flags) happens here —
 * the caller is responsible for remembering that decision and wiring navigation.
 *
 * @param navController Navigation controller, exposed for graph wiring by the caller.
 * @param onSkip Invoked when the user taps "Skip" from any page.
 * @param onGetStarted Invoked when the user taps "Get Started" on the final page.
 */
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    onSkip: () -> Unit,
    onGetStarted: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Scaffold(containerColor = MedPlusScreenBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MedPlusScreenBackground)
                .padding(innerPadding),
        ) {
            // ---- Skip button (top-right, hidden on the final page) ------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MedPlusTextSecondary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = "Skip onboarding",
                        ) { onSkip() },
                    )
                }
            }

            // ---- Pager: swipeable illustrated pages ----------------------------------
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { pageIndex ->
                OnboardingPageContent(page = onboardingPages[pageIndex])
            }

            // ---- Animated page indicator --------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                onboardingPages.indices.forEach { index ->
                    val isActive = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 28.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "onboarding-dot-width",
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) MedPlusPrimary else MedPlusTextSecondary.copy(alpha = 0.25f)),
                    )
                }
            }

            // ---- Next / Get Started CTA -----------------------------------------------
            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                AnimatedContent(
                    targetState = isLastPage,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    label = "onboarding-cta",
                ) { lastPage ->
                    MedPlusPrimaryButton(
                        text = if (lastPage) "Get Started" else "Next",
                        onClick = {
                            if (lastPage) {
                                onGetStarted()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Content for a single onboarding page: a large illustrated icon badge, title and
 * supporting description, centered with generous spacing.
 */
@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Large illustration placeholder built from the shared illustration badge,
        // enlarged for a hero-style presence on the onboarding pages.
        MedPlusIllustrationBadge(
            icon = page.icon,
            contentDescription = page.title,
            size = 176,
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MedPlusTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MedPlusTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

// =====================================================================================
// PREVIEW
// =====================================================================================

@Preview(showBackground = true, showSystemUi = true, name = "Onboarding Screen — Light")
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme {
        OnboardingScreen(
            navController = rememberNavController(),
            onSkip = {},
            onGetStarted = {},
        )
    }
}