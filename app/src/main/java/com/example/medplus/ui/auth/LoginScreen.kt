package com.example.medplus.ui.auth

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.medplus.ui.theme.*
import com.example.medplus.data.network.SessionManager

// ════════════════════════════════════════════════════════════════════════════
//  LOGIN SCREEN
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(
    navController: NavHostController,
    isEmailLoading: Boolean = false,
    isGoogleLoading: Boolean = false,
    onLoginClick: (email: String, password: String, rememberMe: Boolean) -> Unit,
    onGoogleLogin: (String) -> Unit,
    onForgotPassword: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    var tempServerUrl by remember { mutableStateOf(sessionManager.getApiUrl()) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("remember_me", false)) {
            email = prefs.getString("remembered_email", "") ?: ""
            rememberMe = true
        }
    }

    fun validateAndSubmit() {
        if (isEmailLoading || isGoogleLoading) return
        emailError = if (email.isBlank()) "Email address is required" else null
        passwordError = if (password.isBlank()) "Password is required" else null
        keyboardController?.hide()
        if (emailError == null && passwordError == null) {
            val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            if (rememberMe) {
                prefs.edit()
                    .putString("remembered_email", email.trim())
                    .putBoolean("remember_me", true)
                    .apply()
            } else {
                prefs.edit()
                    .remove("remembered_email")
                    .putBoolean("remember_me", false)
                    .apply()
            }
            onLoginClick(email.trim(), password, rememberMe)
        }
    }

    Scaffold(
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── BRANDING ──────────────────────────────────────────────────
            BrandHeader()

            Spacer(modifier = Modifier.height(40.dp))

            // ── WELCOME TEXT ──────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in to continue managing your healthcare.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── INPUT FIELDS ──────────────────────────────────────────────
            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                isError = emailError != null,
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label = "Password",
                isError = passwordError != null,
                errorMessage = passwordError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { validateAndSubmit() }),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── REMEMBER ME & FORGOT PASSWORD ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { if (!isEmailLoading && !isGoogleLoading) rememberMe = !rememberMe }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { if (!isEmailLoading && !isGoogleLoading) rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = Primary),
                        enabled = !isEmailLoading && !isGoogleLoading
                    )
                    Text(
                        text = "Remember Me",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryText
                    )
                }

                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Primary,
                    modifier = Modifier.clickable(enabled = !isEmailLoading && !isGoogleLoading) { onForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── SIGN IN BUTTON ────────────────────────────────────────────
            Button(
                onClick = { validateAndSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = AppShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isEmailLoading && !isGoogleLoading
            ) {
                if (isEmailLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── DIVIDER ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Outline)
                Text(
                    text = "OR CONTINUE WITH",
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Outline)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── GOOGLE SIGN IN ────────────────────────────────────────────
            OutlinedButton(
                onClick = { if (!isEmailLoading && !isGoogleLoading) showRoleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = AppShapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
                enabled = !isEmailLoading && !isGoogleLoading
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Login, // Placeholder for Google icon
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryText
                    )
                }
            }

            if (showRoleDialog) {
                AlertDialog(
                    onDismissRequest = { showRoleDialog = false },
                    title = { Text("Select Account Type", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you signing in as a Patient or a Doctor?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRoleDialog = false
                                onGoogleLogin("PATIENT")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Patient")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showRoleDialog = false
                                onGoogleLogin("DOCTOR")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Doctor")
                        }
                    }
                )
            }

            if (showServerConfigDialog) {
                AlertDialog(
                    onDismissRequest = { showServerConfigDialog = false },
                    title = { Text("Server Configuration", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                "Enter the backend API server URL (include http:// and port). Default is resolved dynamically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = tempServerUrl,
                                onValueChange = { tempServerUrl = it },
                                label = { Text("Server URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    context.getSharedPreferences("medplus_session", Context.MODE_PRIVATE).edit().remove("custom_api_url").apply()
                                    tempServerUrl = sessionManager.getApiUrl()
                                }
                            ) {
                                Text("Reset to Default")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tempServerUrl.isNotBlank()) {
                                    sessionManager.saveApiUrl(tempServerUrl.trim())
                                }
                                showServerConfigDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showServerConfigDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── FOOTER: CREATE ACCOUNT ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText
                )
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (!isEmailLoading && !isGoogleLoading) Primary else Primary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            enabled = !isEmailLoading && !isGoogleLoading
                        ) { onRegisterClick() }
                        .padding(horizontal = 4.dp, vertical = 8.dp) // Increase touch target
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Server Configuration",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = !isEmailLoading && !isGoogleLoading) {
                        tempServerUrl = sessionManager.getApiUrl()
                        showServerConfigDialog = true
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  COMPONENTS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun BrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "MedPlus",
            style = MaterialTheme.typography.displayMedium,
            color = PrimaryText
        )
        Text(
            text = "Smart Hospital Queue Management",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = if (isError) Error else Primary) },
            isError = isError,
            singleLine = true,
            shape = AppShapes.medium,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Outline,
                errorBorderColor = Error,
                focusedLabelColor = Primary,
                unfocusedLabelColor = SecondaryText
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var isVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (isError) Error else Primary) },
            trailingIcon = {
                IconButton(onClick = { isVisible = !isVisible }) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = SecondaryText
                    )
                }
            },
            isError = isError,
            singleLine = true,
            shape = AppShapes.medium,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password, imeAction = imeAction),
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Outline,
                errorBorderColor = Error,
                focusedLabelColor = Primary,
                unfocusedLabelColor = SecondaryText
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
