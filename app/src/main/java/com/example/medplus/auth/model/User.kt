package com.example.medplus.auth.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val profileImage: String = "",
    val status: String = "ACTIVE"
)