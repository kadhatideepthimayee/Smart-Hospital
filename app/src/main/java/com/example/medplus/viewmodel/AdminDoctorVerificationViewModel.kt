package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.DoctorProfile
import com.example.medplus.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI State for the Admin Doctor Verification screen.
 */
data class AdminDoctorVerificationUiState(
    val isLoading: Boolean = false,
    val doctors: List<DoctorProfile> = emptyList(),
    val errorMessage: String? = null,
    val currentFilter: String = "PENDING"
)

/**
 * ViewModel for the Admin Doctor Verification screen.
 */
class AdminDoctorVerificationViewModel : ViewModel() {

    private val repository = AdminRepository()

    private val _uiState = MutableStateFlow(AdminDoctorVerificationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDoctors("PENDING")
    }

    /**
     * Loads doctors based on status and updates the current filter.
     */
    fun loadDoctors(status: String) {
        _uiState.update { it.copy(isLoading = true, currentFilter = status, errorMessage = null) }

        repository.getDoctorsByStatus(
            status = status,
            onSuccess = { list ->
                _uiState.update { it.copy(doctors = list, isLoading = false) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(errorMessage = error, isLoading = false) }
            }
        )
    }

    /**
     * Refreshes the current list.
     */
    fun refresh() {
        loadDoctors(_uiState.value.currentFilter)
    }
}
