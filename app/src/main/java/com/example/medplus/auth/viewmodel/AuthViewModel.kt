package com.example.medplus.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.medplus.auth.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential

import android.util.Log
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.GetCredentialException

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.registerUser(
            fullName,
            email,
            phone,
            password,
            role,
            onSuccess,
            onFailure
        )
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.loginUser(
            email,
            password,
            onSuccess,
            onFailure
        )
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.resetPassword(
            email,
            onSuccess,
            onFailure
        )
    }

    fun logout() {
        repository.logout()
    }

    fun googleSignIn(
        navController: NavHostController,
        selectedRole: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val context = navController.context
        val credentialManager = CredentialManager.create(context)

        val webClientId = context.getString(com.example.medplus.R.string.default_web_client_id)
        Log.d("GOOGLE_AUTH_DEBUG", "Google Sign-In started with Web Client ID: $webClientId")

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                Log.d("GOOGLE_AUTH_DEBUG", "Google account selected")

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    
                    Log.d("GOOGLE_AUTH_DEBUG", "Google credential received")
                    
                    firebaseAuthWithGoogle(googleIdTokenCredential, selectedRole, onSuccess, onFailure)
                } else {
                    Log.e("GOOGLE_AUTH_DEBUG", "Unexpected credential type: ${credential.type}")
                    onFailure("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d("GOOGLE_AUTH_DEBUG", "Google Sign-In cancelled")
                onFailure("CANCELLED")
            } catch (e: NoCredentialException) {
                Log.e("GOOGLE_AUTH_DEBUG", "No credentials available. This could be due to missing Google accounts or configuration mismatch.", e)
                onFailure("No Google accounts found or configuration error. Please ensure you are signed in.")
            } catch (e: GetCredentialException) {
                Log.e("GOOGLE_AUTH_DEBUG", "Credential Manager error: ${e.type}", e)
                onFailure("Sign-in error: ${e.message}")
            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH_DEBUG", "Google Sign-In failed", e)
                onFailure(e.message ?: "Google Sign-In failed")
            }
        }
    }

    fun firebaseAuthWithGoogle(
        credential: Any,
        selectedRole: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.firebaseAuthWithGoogle(
            credential,
            selectedRole,
            onSuccess,
            onFailure
        )
    }
    fun saveUserRole(
        role: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        repository.saveUserRole(
            role,
            onSuccess,
            onFailure
        )
    }
}