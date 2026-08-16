package com.google.firebase

import android.content.Context

/**
 * Lightweight mock implementation of FirebaseApp to store application context without
 * requiring the real Firebase Core SDK.
 */
class FirebaseApp private constructor(val applicationContext: Context) {
    companion object {
        private var instance: FirebaseApp? = null

        @JvmStatic
        fun getInstance(): FirebaseApp {
            return instance ?: throw IllegalStateException("FirebaseApp has not been initialized. Please call FirebaseApp.initializeApp(context) first.")
        }

        @JvmStatic
        fun initializeApp(context: Context): FirebaseApp {
            if (instance == null) {
                instance = FirebaseApp(context.applicationContext)
            }
            return instance!!
        }
    }
}
