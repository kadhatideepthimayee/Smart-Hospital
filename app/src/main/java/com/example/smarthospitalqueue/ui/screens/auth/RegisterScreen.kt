package com.example.smarthospitalqueue.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthospitalqueue.ui.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState
import com.example.smarthospitalqueue.ui.viewmodel.AuthState
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
// ─────────────────────────────────────────────────────────────────────────────
// RegisterScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RegisterScreen(
    onRegisterClick: (
        fullName: String,
        email: String,
        phone: String,
        password: String,
    ) -> Unit = { _, _, _, _ -> },
    onGoogleSignIn: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    isLoading: Boolean = false,
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(authState) {

        if (authState is AuthState.Success) {

            Toast.makeText(
                context,
                "Account Created Successfully",
                Toast.LENGTH_LONG
            ).show()

            onRegisterSuccess()
        }
    }
    // ── State ─────────────────────────────────────────────────────────────────
    var fullName        by rememberSaveable { mutableStateOf("") }
    var email           by rememberSaveable { mutableStateOf("") }
    var phone           by rememberSaveable { mutableStateOf("") }
    var password        by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var passwordVisible        by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var fullNameError        by remember { mutableStateOf<String?>(null) }
    var emailError           by remember { mutableStateOf<String?>(null) }
    var phoneError           by remember { mutableStateOf<String?>(null) }
    var passwordError        by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var contentVisible by remember { mutableStateOf(false) }
    val scrollState  = rememberScrollState()

    // Trigger enter animation
    LaunchedEffect(Unit) { contentVisible = true }

    // ── Validation ────────────────────────────────────────────────────────────
    fun validate(): Boolean {
        var valid = true

        fullNameError = when {
            fullName.isBlank()    -> "Full name is required".also { valid = false }
            fullName.length < 3   -> "Name must be at least 3 characters".also { valid = false }
            else                  -> null
        }
        emailError = when {
            email.isBlank()                          -> "Email is required".also { valid = false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                -> "Enter a valid email address".also { valid = false }
            else                                     -> null
        }
        phoneError = when {
            phone.isBlank()       -> "Phone number is required".also { valid = false }
            phone.length < 10     -> "Enter a valid phone number".also { valid = false }
            else                  -> null
        }
        passwordError = when {
            password.isBlank()    -> "Password is required".also { valid = false }
            password.length < 8   -> "Password must be at least 8 characters".also { valid = false }
            else                  -> null
        }
        confirmPasswordError = when {
            confirmPassword.isBlank()       -> "Please confirm your password".also { valid = false }
            confirmPassword != password     -> "Passwords do not match".also { valid = false }
            else                            -> null
        }
        return valid
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
            RegisterHeader(visible = contentVisible)

            // ── Form card ─────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = contentVisible,
                enter   = fadeIn(tween(600)) + slideInVertically(
                    tween(600, easing = EaseInOut),
                    initialOffsetY = { it / 3 },
                ),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {

                        // Google sign-in
                        GoogleSignInButton(
                            onClick  = onGoogleSignIn,
                            enabled  = !isLoading,
                        )

                        // Divider
                        RegisterDivider()

                        // Full Name
                        RegisterTextField(
                            value         = fullName,
                            onValueChange = { fullName = it; fullNameError = null },
                            label         = "Full Name",
                            leadingIcon   = Icons.Filled.Person,
                            errorMessage  = fullNameError,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction      = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                            enabled = !isLoading,
                        )

                        // Email
                        RegisterTextField(
                            value         = email,
                            onValueChange = { email = it; emailError = null },
                            label         = "Email Address",
                            leadingIcon   = Icons.Filled.Email,
                            errorMessage  = emailError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                            enabled = !isLoading,
                        )

                        // Phone
                        RegisterTextField(
                            value         = phone,
                            onValueChange = { phone = it; phoneError = null },
                            label         = "Phone Number",
                            leadingIcon   = Icons.Filled.Phone,
                            errorMessage  = phoneError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction    = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                            enabled = !isLoading,
                        )

                        // Password
                        PasswordTextField(
                            value           = password,
                            onValueChange   = { password = it; passwordError = null },
                            label           = "Password",
                            visible         = passwordVisible,
                            onToggleVisible = { passwordVisible = !passwordVisible },
                            errorMessage    = passwordError,
                            imeAction       = ImeAction.Next,
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                            enabled = !isLoading,
                        )

                        // Confirm Password
                        PasswordTextField(
                            value           = confirmPassword,
                            onValueChange   = { confirmPassword = it; confirmPasswordError = null },
                            label           = "Confirm Password",
                            visible         = confirmPasswordVisible,
                            onToggleVisible = { confirmPasswordVisible = !confirmPasswordVisible },
                            errorMessage    = confirmPasswordError,
                            imeAction       = ImeAction.Done,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (validate()) {

                                        authViewModel.registerUser(
                                            fullName,
                                            email,
                                            password
                                        )
                                    }
                                },
                            ),
                            enabled = !isLoading,
                        )

                        Spacer(Modifier.height(4.dp))

                        // Register button
                        Button(
                            onClick = {
                                focusManager.clearFocus()

                                if (validate()) {

                                    authViewModel.registerUser(
                                        fullName,
                                        email,
                                        password
                                    )


                                }
                            },
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
                                    modifier  = Modifier.size(22.dp),
                                    color     = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp,
                                )
                            } else {
                                Text(
                                    text       = "Create Account",
                                    style      = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp,
                                )
                            }
                        }

                        // Sign in link
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text  = "Already have an account?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
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
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RegisterHeader(visible: Boolean) {
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
                .height(240.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0077B6),
                            Color(0xFF00B4D8),
                        ),
                    ),
                )
                .statusBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            // Decorative circle behind icon
            Box(
                modifier         = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.LocalHospital,
                    contentDescription = null,
                    modifier           = Modifier.size(52.dp),
                    tint               = Color.White,
                )
            }

            Column(
                modifier              = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 24.dp),
            ) {
                Text(
                    text       = "Create Account",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Register to manage your hospital visits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable text field
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label) },
        leadingIcon   = {
            Icon(
                imageVector        = leadingIcon,
                contentDescription = null,
                tint               = if (errorMessage != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
            )
        },
        isError         = errorMessage != null,
        supportingText  = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine      = true,
        enabled         = enabled,
        shape           = RoundedCornerShape(12.dp),
        colors          = registerTextFieldColors(),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable password field
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    errorMessage: String?,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label) },
        leadingIcon   = {
            Icon(
                imageVector        = Icons.Filled.Lock,
                contentDescription = null,
                tint               = if (errorMessage != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector        = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError              = errorMessage != null,
        supportingText       = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        keyboardOptions      = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction    = imeAction,
        ),
        keyboardActions = keyboardActions,
        singleLine      = true,
        enabled         = enabled,
        shape           = RoundedCornerShape(12.dp),
        colors          = registerTextFieldColors(),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Google sign-in button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled   = enabled,
        shape     = RoundedCornerShape(14.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Icon(
            imageVector        = Icons.Filled.Person, // Replace with Google SVG asset if available
            contentDescription = "Google",
            tint               = MaterialTheme.colorScheme.onSurface,
            modifier           = Modifier.size(20.dp),
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text  = "Continue with Google",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Divider with "or" label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RegisterDivider() {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text      = "  or  ",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared text field colors
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun registerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor    = MaterialTheme.colorScheme.primary,
    cursorColor          = MaterialTheme.colorScheme.primary,
)

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegisterScreenDarkPreview() {
    MaterialTheme {
        RegisterScreen()
    }
}
