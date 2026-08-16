package com.example.medplus.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// =====================================================================================
// FORGOT PASSWORD SCREEN
// =====================================================================================

/**
 * Premium "Forgot Password" screen for MedPlus.
 *
 * Purely presentational: collects the registered email address and, on submit, shows a
 * lightweight local "sent" confirmation state before delegating to [onSendResetLink].
 * No network / backend call happens inside this composable — the caller decides what
 * "sending" actually means.
 *
 * @param navController Navigation controller, exposed for graph wiring by the caller.
 * @param onSendResetLink Invoked with the entered email once basic validation passes.
 * @param onBackToLogin Invoked when the user taps "Back to Login".
 */
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    onSendResetLink: (email: String) -> Unit,
    onBackToLogin: () -> Unit,
) {
    // ---- Local UI-only state ------------------------------------------------------
    var email by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var linkSent by rememberSaveable { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    fun validateAndSubmit() {
        emailError = when {
            email.isBlank() -> "Email address is required"
            !email.contains("@") -> "Enter a valid email address"
            else -> null
        }
        keyboardController?.hide()
        if (emailError == null) {
            linkSent = true
            onSendResetLink(email)
        }
    }

    Scaffold(containerColor = MedPlusScreenBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MedPlusScreenBackground)
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
        ) {
            // ---- Back button -----------------------------------------------------
            IconButton(
                onClick = onBackToLogin,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to login",
                    tint = MedPlusTextPrimary,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---- Illustration: animated between lock and success states ----------
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = linkSent,
                    transitionSpec = {
                        (fadeIn(tween(300)) + scaleIn(initialScale = 0.85f, animationSpec = tween(300)))
                            .togetherWith(fadeOut(tween(200)) + scaleOut(targetScale = 0.85f, animationSpec = tween(200)))
                    },
                    label = "forgot-password-illustration",
                ) { sent ->
                    MedPlusIllustrationBadge(
                        icon = if (sent) Icons.Filled.CheckCircle else Icons.Filled.LockReset,
                        contentDescription = if (sent) "Reset link sent" else "Forgot password illustration",
                        size = 120,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ---- Title / subtitle: animated content swap for sent state -----------
            AnimatedContent(
                targetState = linkSent,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "forgot-password-copy",
            ) { sent ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (sent) "Check Your Email" else "Forgot Password?",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedPlusTextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (sent) {
                            "We've sent a password reset link to $email"
                        } else {
                            "Enter your registered email."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedPlusTextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---- Email field + submit button (hidden once sent) --------------------
            AnimatedVisibility(
                visible = !linkSent,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    MedPlusTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        label = "Email",
                        leadingIcon = Icons.Filled.Email,
                        isError = emailError != null,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { validateAndSubmit() }),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MedPlusPrimaryButton(
                        text = "Send Reset Link",
                        onClick = { validateAndSubmit() },
                    )
                }
            }

            // ---- Success status card + resend action once sent ---------------------
            AnimatedVisibility(
                visible = linkSent,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    // Modern success placeholder card — purely presentational.
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MedPlusCardRadius),
                        colors = CardDefaults.cardColors(containerColor = MedPlusSuccess.copy(alpha = 0.10f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = MedPlusSuccess,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Reset link sent successfully. Please check your inbox.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedPlusTextPrimary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    MedPlusOutlinedButton(
                        text = "Resend Link",
                        onClick = { onSendResetLink(email) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---- Back to login footer ------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Back to ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedPlusTextSecondary,
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedPlusPrimary,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = "Back to login",
                    ) { onBackToLogin() },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =====================================================================================
// PREVIEW
// =====================================================================================

@Preview(showBackground = true, showSystemUi = true, name = "Forgot Password Screen — Light")
@Composable
private fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreen(
            navController = rememberNavController(),
            onSendResetLink = {},
            onBackToLogin = {},
        )
    }
}