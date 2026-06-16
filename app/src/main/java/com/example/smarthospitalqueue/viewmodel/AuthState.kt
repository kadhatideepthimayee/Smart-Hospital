package com.example.smarthospitalqueue.ui.viewmodel

// ─────────────────────────────────────────────────────────────────────────────
// AuthState.kt
//
// Represents every possible state the authentication flow can be in.
// The UI observes this via StateFlow and reacts accordingly.
// ─────────────────────────────────────────────────────────────────────────────

sealed class AuthState {

    /** Initial state — no action has been triggered yet. */
    data object Idle : AuthState()

    /** A Firebase operation is currently in progress. Show a loading indicator. */
    data object Loading : AuthState()

    /**
     * The operation completed successfully.
     * @param message Human-readable success message shown to the user.
     */
    data class Success(val message: String) : AuthState()

    /**
     * The operation failed.
     * @param message Human-readable error message shown to the user.
     */
    data class Error(val message: String) : AuthState()
}
