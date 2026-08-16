package com.example.medplus.model

import java.io.Serializable

data class MedicineReminder(
    val id: String,
    val medicineName: String,
    val time: String,
    val dosage: String,
    val isActive: Boolean = true
) : Serializable
