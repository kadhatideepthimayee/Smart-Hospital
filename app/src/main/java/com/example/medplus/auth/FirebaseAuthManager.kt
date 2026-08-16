package com.example.medplus.auth

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser() = auth.currentUser

    fun logout() {
        auth.signOut()
    }
}