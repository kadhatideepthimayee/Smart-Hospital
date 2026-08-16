package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdminProfileUiState(
    val adminName: String = "",
    val email: String = "",
    val role: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminProfileViewModel : ViewModel() {

    private val dashboardRepository = DashboardRepository()

    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAdminProfile()
    }

    fun loadAdminProfile() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        dashboardRepository.getCurrentUser(
            onSuccess = { user ->
                _uiState.update {
                    it.copy(
                        adminName = user.fullName,
                        email = user.email,
                        role = user.role,
                        isLoading = false
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }
}
