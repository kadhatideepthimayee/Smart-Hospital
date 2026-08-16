package com.example.medplus.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medplus.model.MedicalRecord
import com.example.medplus.repository.MedicalRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PatientMedicalRecordsUiState(
    val isLoading: Boolean = false,
    val records: List<MedicalRecord> = emptyList(),
    val selectedRecord: MedicalRecord? = null,
    val errorMessage: String? = null
)

class PatientMedicalRecordsViewModel : ViewModel() {

    private val repository = MedicalRecordRepository()

    private val _uiState = MutableStateFlow(PatientMedicalRecordsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMedicalRecords() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getPatientMedicalRecords(
            onSuccess = { list ->
                _uiState.update { it.copy(isLoading = false, records = list) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun loadRecordDetails(recordId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedRecord = null) }
        repository.getMedicalRecordDetails(
            recordId = recordId,
            onSuccess = { record ->
                _uiState.update { it.copy(isLoading = false, selectedRecord = record) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun clearErrors() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
