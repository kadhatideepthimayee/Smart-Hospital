package com.example.smarthospitalqueue.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthospitalqueue.ui.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState
import com.example.smarthospitalqueue.ui.viewmodel.AuthState
// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Two-state screen:
 *  1. [ForgotPasswordState.Input]   — user enters their email address.
 *  2. [ForgotPasswordState.Success] — confirmation card shown after submission.
 */
private sealed interface ForgotPasswordState {
    data object Input   : ForgotPasswordState
    data object Success : ForgotPasswordState
}

@Composable
fun ForgotPasswordScreen(
    onSendResetLink: (email: String) -> Unit = {},
    onBackToLogin:   () -> Unit = {},
    isLoading:       Boolean = false,
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    // ── State ─────────────────────────────────────────────────────────────────
    var email        by rememberSaveable { mutableStateOf("") }
    var emailError   by remember { mutableStateOf<String?>(null) }
    var screenState  by remember { mutableStateOf<ForgotPasswordState>(ForgotPasswordState.Input) }
    LaunchedEffect(authState) {

        if (authState is AuthState.Success) {

            screenState = ForgotPasswordState.Success
        }
    }
    var contentVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()



    LaunchedEffect(Unit) { contentVisible = true }

    // ── Validation ────────────────────────────────────────────────────────────
    fun validate(): Boolean {
        emailError = when {
            email.isBlank() -> "Email address is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Enter a valid email address"
            else -> null
        }
        return emailError == null
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .imePadding(),
        ) {

            // ── Gradient header ───────────────────────────────────────────────
            ForgotPasswordHeader(
                visible        = contentVisible,
                onBackToLogin  = onBackToLogin,
                isLoading      = isLoading,
            )

            // ── Content card: switches between Input and Success ──────────────
            AnimatedVisibility(
                visible = contentVisible,
                enter   = fadeIn(tween(600)) + slideInVertically(
                    tween(600, easing = EaseInOut),
                    initialOffsetY = { it / 3 },
                ),
            ) {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    // Switch between form and success state
                    AnimatedVisibility(
                        visible = screenState is ForgotPasswordState.Input,
                        enter   = fadeIn(tween(300)),
                        exit    = fadeOut(tween(200)),
                    ) {
                        ForgotPasswordForm(
                            email          = email,
                            onEmailChange  = { email = it; emailError = null },
                            emailError     = emailError,
                            isLoading      = isLoading,
                            onSendClick = {
                                focusManager.clearFocus()
                                if (validate()) {
                                    authViewModel.sendPasswordReset(email)


                                }
                            },
                            onBackToLogin  = onBackToLogin,
                            focusManager   = focusManager,
                        )
                    }

                    AnimatedVisibility(
                        visible = screenState is ForgotPasswordState.Success,
                        enter   = fadeIn(tween(400)) + scaleIn(
                            tween(400, easing = EaseOut),
                            initialScale = 0.92f,
                        ),
                    ) {
                        ForgotPasswordSuccess(
                            email         = email,
                            onBackToLogin = onBackToLogin,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ForgotPasswordHeader(
    visible:       Boolean,
    onBackToLogin: () -> Unit,
    isLoading:     Boolean,
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(500)) + slideInVertically(
            tween(500, easing = EaseInOut),
            initialOffsetY = { -it / 2 },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0077B6),
                            Color(0xFF00B4D8),
                        ),
                    ),
                )
                .statusBarsPadding(),
        ) {
            // Back button
            IconButton(
                onClick  = onBackToLogin,
                enabled  = !isLoading,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector        = Icons.Filled.ArrowBack,
                    contentDescription = "Back to Login",
                    tint               = Color.White,
                )
            }

            // Icon + title centred
            Column(
                modifier              = Modifier.align(Alignment.Center),
                horizontalAlignment   = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier         = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier           = Modifier.size(44.dp),
                        tint               = Color.White,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text       = "Forgot Password?",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Email input form
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordForm(
    email:         String,
    onEmailChange: (String) -> Unit,
    emailError:    String?,
    isLoading:     Boolean,
    onSendClick:   () -> Unit,
    onBackToLogin: () -> Unit,
    focusManager:  androidx.compose.ui.focus.FocusManager,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {

        // Instruction text
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text       = "Reset your password",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = "Enter your registered email address and we'll send you a link to reset your password.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
            )
        }

        // Email field
        OutlinedTextField(
            value         = email,
            onValueChange = onEmailChange,
            modifier      = Modifier.fillMaxWidth(),
            label         = { Text("Email Address") },
            leadingIcon   = {
                Icon(
                    imageVector        = Icons.Filled.Email,
                    contentDescription = null,
                    tint               = if (emailError != null)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                )
            },
            isError         = emailError != null,
            supportingText  = emailError?.let { msg ->
                { Text(msg, color = MaterialTheme.colorScheme.error) }
            },
            singleLine      = true,
            enabled         = !isLoading,
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSendClick()
                },
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                cursorColor          = MaterialTheme.colorScheme.primary,
            ),
        )

        // Send button
        Button(
            onClick   = onSendClick,
            modifier  = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled   = !isLoading,
            shape     = RoundedCornerShape(14.dp),
            colors    = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    color       = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp,
                )
            } else {
                Icon(
                    imageVector        = Icons.Filled.MarkEmailRead,
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text       = "Send Reset Link",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                )
            }
        }

        // Back to login
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = "Remembered your password?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onBackToLogin, enabled = !isLoading) {
                Text(
                    text       = "Sign In",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success state card content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ForgotPasswordSuccess(
    email:         String,
    onBackToLogin: () -> Unit,
) {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {

        // Success icon
        Box(
            modifier         = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text       = "Check your email!",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
            textAlign  = TextAlign.Center,
        )

        Text(
            text      = "We've sent a password reset link to\n$email",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        // Divider hint
        Text(
            text      = "Didn't receive it? Check your spam folder or try again.",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick   = onBackToLogin,
            modifier  = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape     = RoundedCornerShape(14.dp),
            colors    = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text       = "Back to Sign In",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ForgotPasswordScreenDarkPreview() {
    MaterialTheme {
        ForgotPasswordScreen()
    }
}
