package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.auth.model.User
import com.example.medplus.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DoctorPatientsUiState(
    val isLoading: Boolean = false,
    val patients: List<User> = emptyList(),
    val filteredPatients: List<User> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class DoctorPatientsViewModel : ViewModel() {

    private val repository = DoctorRepository()

    private val _uiState = MutableStateFlow(DoctorPatientsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getDoctorPatients(
            onSuccess = { list ->
                _uiState.update { it.copy(
                    isLoading = false,
                    patients = list,
                    filteredPatients = list
                ) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredPatients = if (query.isBlank()) {
                    state.patients
                } else {
                    state.patients.filter { 
                        it.fullName.contains(query, ignoreCase = true) || 
                        it.email.contains(query, ignoreCase = true) ||
                        it.phone.contains(query, ignoreCase = true)
                    }
                }
            )
        }
    }
}
