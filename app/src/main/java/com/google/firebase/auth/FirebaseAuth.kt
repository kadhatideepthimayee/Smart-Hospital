package com.google.firebase.auth

import com.example.medplus.data.network.SessionManager

class FirebaseAuth private constructor() {
    companion object {
        @Volatile
        private var instance: FirebaseAuth? = null

        @JvmStatic
        fun getInstance(): FirebaseAuth {
            return instance ?: synchronized(this) {
                instance ?: FirebaseAuth().also { instance = it }
            }
        }
    }

    val currentUser: FirebaseUser?
        get() {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            val sessionManager = SessionManager.getInstance(context)
            return if (sessionManager.isLoggedIn()) {
                FirebaseUser(
                    uid = sessionManager.getUserId() ?: "",
                    email = sessionManager.getEmail(),
                    displayName = sessionManager.getName()
                )
            } else {
                null
            }
        }

    fun signOut() {
        val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
        SessionManager.getInstance(context).clearSession()
    }
}

data class FirebaseUser(
    val uid: String,
    val email: String?,
    val displayName: String?
)
