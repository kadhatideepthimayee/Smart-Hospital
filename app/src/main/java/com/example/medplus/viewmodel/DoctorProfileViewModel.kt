package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.net.Uri

/**
 * State for document upload progress.
 */
data class DocumentUploadState(
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
    val error: String? = null,
    val storagePath: String? = null
)

/**
 * UI State for Doctor Profile Setup and Verification.
 */
data class DoctorProfileUiState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val profile: DoctorProfile? = null,
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false,
    val registrationCertificateState: DocumentUploadState = DocumentUploadState(),
    val qualificationCertificateState: DocumentUploadState = DocumentUploadState()
)

class DoctorProfileViewModel : ViewModel() {

    private val repository = DoctorRepository()
    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = com.example.medplus.data.network.SessionManager.getInstance(context)

    private val _uiState = MutableStateFlow(DoctorProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchProfile()
    }

    /**
     * Resets the UI state to empty.
     */
    fun resetState() {
        android.util.Log.d("DOCTOR_PROFILE_DEBUG", "Resetting DoctorProfileViewModel state")
        _uiState.value = DoctorProfileUiState()
    }

    /**
     * Fetches the current doctor's profile from Firestore.
     */
    fun fetchProfile() {
        val uid = sessionManager.getUserId()
        
        if (uid == null) {
            android.util.Log.d("DOCTOR_PROFILE_DEBUG", "fetchProfile: No user logged in")
            _uiState.update { it.copy(profile = null, isLoading = false, isLoaded = true) }
            return
        }

        android.util.Log.d("DOCTOR_PROFILE_DEBUG", "fetchProfile: Fetching for UID: $uid")

        // If current state has a profile for a different UID, reset it first
        val currentProfileUid = _uiState.value.profile?.uid
        if (currentProfileUid != null && currentProfileUid != uid) {
            android.util.Log.d("DOCTOR_PROFILE_DEBUG", "fetchProfile: UID mismatch ($currentProfileUid != $uid), resetting state")
            _uiState.value = DoctorProfileUiState()
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        repository.getDoctorProfile(
            uid = uid,
            onSuccess = { profile ->
                android.util.Log.d("DOCTOR_PROFILE_DEBUG", "fetchProfile: Success. Profile exists: ${profile != null}")
                if (profile != null) {
                    android.util.Log.d("DOCTOR_PROFILE_DEBUG", "fetchProfile: Profile UID: ${profile.uid}")
                }
                
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isLoaded = true,
                        profile = profile,
                        registrationCertificateState = if (profile?.registrationCertificateUrl.isNullOrBlank() == false) {
                            DocumentUploadState(isUploaded = true, storagePath = profile.registrationCertificateUrl)
                        } else DocumentUploadState(),
                        qualificationCertificateState = if (profile?.verificationDocumentUrl.isNullOrBlank() == false) {
                            DocumentUploadState(isUploaded = true, storagePath = profile.verificationDocumentUrl)
                        } else DocumentUploadState()
                    )
                }
            },
            onFailure = { error ->
                android.util.Log.e("DOCTOR_PROFILE_DEBUG", "fetchProfile: Failed to load profile: $error")
                _uiState.update { it.copy(isLoading = false, isLoaded = true, errorMessage = error) }
            }
        )
    }

    /**
     * Updates/Saves the doctor profile to Firestore.
     */
    fun submitProfile(profile: DoctorProfile) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.saveDoctorProfile(
            profile = profile,
            onSuccess = {
                _uiState.update { it.copy(isLoading = false, profile = profile) }
            },
            onFailure = { error ->
                android.util.Log.e("DOCTOR_PROFILE_DEBUG", "Failed to save profile: $error")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    /**
     * Specifically triggers the verification submission.
     */
    fun submitForVerification() {
        val uid = sessionManager.getUserId() ?: return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.submitForVerification(
            uid = uid,
            onSuccess = {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isSubmitted = true,
                        profile = state.profile?.copy(verificationStatus = "PENDING")
                    )
                }
            },
            onFailure = { error ->
                android.util.Log.e("DOCTOR_PROFILE_DEBUG", "Failed to submit for verification: $error")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    /**
     * Handles document processing to Base64 and updates Firestore.
     */
    fun uploadDocument(uri: Uri, documentType: String) {
        _uiState.update { state ->
            if (documentType == "registration") {
                state.copy(registrationCertificateState = state.registrationCertificateState.copy(isUploading = true, error = null))
            } else {
                state.copy(qualificationCertificateState = state.qualificationCertificateState.copy(isUploading = true, error = null))
            }
        }

        repository.uploadDoctorDocument(
            fileUri = uri,
            documentName = "${documentType}_certificate", 
            onSuccess = { base64Data ->
                var updatedProfile: DoctorProfile? = null
                _uiState.update { state ->
                    val newProfile = if (documentType == "registration") {
                        state.profile?.copy(registrationCertificateUrl = base64Data)
                    } else {
                        state.profile?.copy(verificationDocumentUrl = base64Data)
                    }
                    updatedProfile = newProfile
                    
                    if (documentType == "registration") {
                        state.copy(
                            registrationCertificateState = DocumentUploadState(isUploaded = true, storagePath = base64Data),
                            profile = newProfile
                        )
                    } else {
                        state.copy(
                            qualificationCertificateState = DocumentUploadState(isUploaded = true, storagePath = base64Data),
                            profile = newProfile
                        )
                    }
                }
                // Save the updated profile with Base64 data to Firestore
                updatedProfile?.let { submitProfile(it) }
            },
            onFailure = { error ->
                _uiState.update { state ->
                    if (documentType == "registration") {
                        state.copy(registrationCertificateState = state.registrationCertificateState.copy(isUploading = false, error = error))
                    } else {
                        state.copy(qualificationCertificateState = state.qualificationCertificateState.copy(isUploading = false, error = error))
                    }
                }
            }
        )
    }

    /**
     * Updates the local profile state.
     */
    fun updateProfile(profile: DoctorProfile) {
        _uiState.update { it.copy(profile = profile) }
        // Also save to Firestore to persist draft
        submitProfile(profile)
    }
}
