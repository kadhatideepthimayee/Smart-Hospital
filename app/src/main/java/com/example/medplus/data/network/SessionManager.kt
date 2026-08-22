package com.example.medplus.data.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("medplus_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_UID = "user_uid"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_ROLE = "user_role"
        private const val KEY_NAME = "user_name"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_PROFILE_IMAGE = "user_profile_image"
        private const val KEY_API_URL = "custom_api_url"
        
        // Single instance helper
        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun saveSession(token: String, uid: String, email: String, role: String, name: String, phone: String, profileImage: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_UID, uid)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role)
            putString(KEY_NAME, name)
            putString(KEY_PHONE, phone)
            putString(KEY_PROFILE_IMAGE, profileImage)
            apply()
        }
    }

    fun getApiUrl(): String {
        val saved = prefs.getString(KEY_API_URL, null)
        return saved ?: getDefaultApiUrl()
    }

    fun saveApiUrl(url: String) {
        // Ensure trailing slash
        val cleanUrl = if (url.endsWith("/")) url else "$url/"
        prefs.edit().putString(KEY_API_URL, cleanUrl).apply()
    }

    private fun getDefaultApiUrl(): String {
        if (isEmulator()) {
            return "http://10.0.2.2:5000/"
        }
        return try {
            val resId = context.resources.getIdentifier("default_api_url", "string", context.packageName)
            if (resId != 0) {
                context.getString(resId)
            } else {
                "http://10.0.2.2:5000/"
            }
        } catch (e: Exception) {
            "http://10.0.2.2:5000/"
        }
    }

    private fun isEmulator(): Boolean {
        val buildFingerprint = android.os.Build.FINGERPRINT ?: ""
        val buildModel = android.os.Build.MODEL ?: ""
        val buildManufacturer = android.os.Build.MANUFACTURER ?: ""
        val buildProduct = android.os.Build.PRODUCT ?: ""
        return (buildFingerprint.startsWith("generic")
                || buildFingerprint.startsWith("unknown")
                || buildModel.contains("google_sdk")
                || buildModel.contains("Emulator")
                || buildModel.contains("Android SDK built for x86")
                || buildManufacturer.contains("Genymotion")
                || buildProduct.contains("sdk_gphone")
                || buildProduct.contains("google_sdk")
                || buildProduct.contains("sdk")
                || buildProduct.contains("sdk_x86")
                || buildProduct.contains("vbox86p")
                || buildProduct.contains("emulator")
                || buildProduct.contains("simulator"))
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): String? = prefs.getString(KEY_UID, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getName(): String? = prefs.getString(KEY_NAME, null)
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)
    fun getProfileImage(): String? = prefs.getString(KEY_PROFILE_IMAGE, null)

    fun updateProfile(name: String, phone: String) {
        prefs.edit().apply {
            putString(KEY_NAME, name)
            putString(KEY_PHONE, phone)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
