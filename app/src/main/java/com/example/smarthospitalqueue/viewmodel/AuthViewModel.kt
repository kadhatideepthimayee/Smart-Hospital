package com.example.smarthospitalqueue.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────────────────────
// AuthViewModel.kt
//
// Handles all Firebase Authentication operations for the Smart Hospital Queue
// Management System. Exposes state via StateFlow so Compose screens can
// collect and react to changes efficiently.
//
// Architecture : MVVM (no Hilt, no repository layer)
// Dependencies : Firebase Auth, Kotlin Coroutines, kotlinx-coroutines-play-services
// ─────────────────────────────────────────────────────────────────────────────

class AuthViewModel : ViewModel() {

    // ── Firebase Auth instance ────────────────────────────────────────────────

    /** Singleton FirebaseAuth instance used across all operations. */
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // ── UI state ──────────────────────────────────────────────────────────────

    /**
     * Backing mutable state — only mutated inside this ViewModel.
     * The UI collects [authState] (the public read-only version).
     */
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)

    /** Read-only StateFlow observed by Compose screens via collectAsState(). */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // ── Current user ──────────────────────────────────────────────────────────

    /**
     * Exposes the currently signed-in Firebase user (or null if signed out).
     * Screens can check this to decide the start destination.
     */
    val currentUser get() = auth.currentUser

    /** Convenience check: true when a user is signed in. */
    val isUserLoggedIn get() = auth.currentUser != null

    // ── Coroutine exception handler ───────────────────────────────────────────

    /**
     * Global fallback for any uncaught exception inside a coroutine launched
     * in this ViewModel. Converts the throwable to a user-facing error state.
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _authState.value = AuthState.Error(
            throwable.localizedMessage ?: "An unexpected error occurred. Please try again."
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    // ── 1. Login ──────────────────────────────────────────────────────────────

    /**
     * Signs the user in with email and password.
     *
     * Validation order:
     *  1. Empty field check
     *  2. Email format check
     *  3. Password minimum length
     *
     * On success → [AuthState.Success]
     * On failure → [AuthState.Error] with a mapped Firebase message
     *
     * @param email    The user's email address.
     * @param password The user's plaintext password (never stored locally).
     */
    fun loginUser(email: String, password: String) {
        // ── Validate inputs before hitting the network ────────────────────────
        val validationError = validateEmailAndPassword(email, password)
        if (validationError != null) {
            _authState.value = AuthState.Error(validationError)
            return
        }

        // ── Fire the coroutine ────────────────────────────────────────────────
        viewModelScope.launch(exceptionHandler) {
            _authState.value = AuthState.Loading

            try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
                _authState.value = AuthState.Success("Login successful. Welcome back!")
            } catch (e: FirebaseAuthInvalidUserException) {
                // Account doesn't exist or has been disabled
                _authState.value = AuthState.Error("No account found with this email address.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                // Wrong password or malformed email at the Auth level
                _authState.value = AuthState.Error("Incorrect email or password. Please try again.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Login failed. Please check your connection."
                )
            }
        }
    }

    // ── 2. Register ───────────────────────────────────────────────────────────

    /**
     * Creates a new Firebase user account and updates the display name.
     *
     * Validation order:
     *  1. Name empty check
     *  2. Email empty + format check
     *  3. Password minimum length (8 characters)
     *
     * On success → display name is set → [AuthState.Success]
     * On failure → [AuthState.Error] with a mapped Firebase message
     *
     * @param name     The user's full name (stored as Firebase display name).
     * @param email    The user's email address.
     * @param password The chosen password (min 8 characters enforced locally).
     */
    fun registerUser(name: String, email: String, password: String) {
        // ── Validate all fields ───────────────────────────────────────────────
        val nameError = validateName(name)
        if (nameError != null) {
            _authState.value = AuthState.Error(nameError)
            return
        }

        val credentialError = validateEmailAndPassword(email, password)
        if (credentialError != null) {
            _authState.value = AuthState.Error(credentialError)
            return
        }

        // ── Fire the coroutine ────────────────────────────────────────────────
        viewModelScope.launch(exceptionHandler) {
            _authState.value = AuthState.Loading

            try {
                // Step 1: Create the account
                val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()

                // Step 2: Set the display name on the newly created user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                result.user?.updateProfile(profileUpdates)?.await()

                _authState.value = AuthState.Success(
                    "Account created successfully! Welcome to Smart Hospital Queue."
                )
            } catch (e: FirebaseAuthWeakPasswordException) {
                // Firebase server-side weak password rejection
                _authState.value = AuthState.Error(
                    "Password is too weak. Please choose a stronger password."
                )
            } catch (e: FirebaseAuthUserCollisionException) {
                // Email already registered
                _authState.value = AuthState.Error(
                    "An account with this email already exists. Please sign in instead."
                )
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                // Malformed email rejected by Firebase
                _authState.value = AuthState.Error("The email address format is invalid.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Registration failed. Please try again."
                )
            }
        }
    }

    // ── 3. Password reset ─────────────────────────────────────────────────────

    /**
     * Sends a password-reset email to the given address.
     *
     * Firebase silently succeeds even if the address isn't registered
     * (security best practice — don't reveal which emails exist).
     *
     * On success → [AuthState.Success] with instructions
     * On failure → [AuthState.Error]
     *
     * @param email The email address to send the reset link to.
     */
    fun sendPasswordReset(email: String) {
        // ── Validate email ────────────────────────────────────────────────────
        when {
            email.isBlank() -> {
                _authState.value = AuthState.Error("Please enter your email address.")
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                _authState.value = AuthState.Error("Please enter a valid email address.")
                return
            }
        }

        // ── Fire the coroutine ────────────────────────────────────────────────
        viewModelScope.launch(exceptionHandler) {
            _authState.value = AuthState.Loading

            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                _authState.value = AuthState.Success(
                    "Reset link sent! Check your inbox (and spam folder)."
                )
            } catch (e: FirebaseAuthInvalidUserException) {
                // Firebase may throw this if the address is unknown in some configs
                // We surface a generic message to avoid user enumeration
                _authState.value = AuthState.Success(
                    "If this email is registered, a reset link has been sent."
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Could not send reset link. Please try again."
                )
            }
        }
    }

    // ── 4. Logout ─────────────────────────────────────────────────────────────

    /**
     * Signs the current user out and resets the [authState] to [AuthState.Idle].
     * This is synchronous — FirebaseAuth.signOut() does not require a coroutine.
     */
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    // ── 5. Reset state ────────────────────────────────────────────────────────

    /**
     * Resets [authState] back to [AuthState.Idle].
     *
     * Call this after the UI has consumed a Success or Error state
     * (e.g. after showing a Snackbar) to prevent re-triggering on recomposition.
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates the full name field.
     * @return An error message string, or null if the name is valid.
     */
    private fun validateName(name: String): String? = when {
        name.isBlank()  -> "Full name cannot be empty."
        name.trim().length < 3 -> "Name must be at least 3 characters long."
        else            -> null
    }

    /**
     * Validates both the email and password fields together.
     * @return The first error message found, or null if both fields are valid.
     */
    private fun validateEmailAndPassword(email: String, password: String): String? {
        if (email.isBlank()) return "Email address cannot be empty."
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "Please enter a valid email address."
        }
        if (password.isBlank()) return "Password cannot be empty."
        if (password.length < 8) return "Password must be at least 8 characters long."
        return null
    }
}
