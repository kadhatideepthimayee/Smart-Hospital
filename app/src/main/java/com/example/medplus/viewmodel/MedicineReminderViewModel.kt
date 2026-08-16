package com.example.medplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.medplus.model.MedicineReminder
import com.example.medplus.repository.MedicineReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class MedicineReminderUiState(
    val reminders: List<MedicineReminder> = emptyList()
)

class MedicineReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MedicineReminderRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(MedicineReminderUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        _uiState.update { it.copy(reminders = repository.getReminders()) }
    }

    fun addReminder(medicineName: String, time: String, dosage: String) {
        val newReminder = MedicineReminder(
            id = UUID.randomUUID().toString(),
            medicineName = medicineName,
            time = time,
            dosage = dosage,
            isActive = true
        )
        repository.addReminder(newReminder)
        loadReminders()
    }

    fun deleteReminder(id: String) {
        repository.deleteReminder(id)
        loadReminders()
    }

    fun toggleReminder(id: String) {
        repository.toggleReminder(id)
        loadReminders()
    }
}
