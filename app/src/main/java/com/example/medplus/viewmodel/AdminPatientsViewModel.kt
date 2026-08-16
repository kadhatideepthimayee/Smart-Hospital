package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.auth.model.User
import com.example.medplus.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdminPatientsUiState(
    val patients: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminPatientsViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AdminPatientsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        userRepository.getUsersByRole(
            role = "PATIENT",
            onSuccess = { users ->
                _uiState.update { it.copy(patients = users, isLoading = false) }
            },
            onFailure = { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        )
    }
}
