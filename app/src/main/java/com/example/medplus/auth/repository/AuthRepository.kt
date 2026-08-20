package com.example.medplus.auth.repository

import android.content.Context
import android.util.Log
import com.example.medplus.data.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = SessionManager.getInstance(context)
    private val apiService: ApiService get() = RetrofitClient.getClient(context)

    /**
     * Register a new user
     */
    fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("AUTH_DEBUG", "REST API Registration started for $email with role $role")

        val request = RegisterRequest(fullName, email, phone, password, role)
        apiService.registerUser(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val uid = response.body()!!.uid
                    // Store local session details
                    sessionManager.saveSession(
                        token = "local_jwt_session_token", // Placeholder, will login next anyway
                        uid = uid,
                        email = email,
                        role = role.uppercase(),
                        name = fullName,
                        phone = phone,
                        profileImage = ""
                    )
                    Log.d("AUTH_DEBUG", "REST Registration successful")
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Registration failed."
                    Log.e("AUTH_DEBUG", "Registration failed: $errorMsg")
                    onFailure(parseErrorMessage(errorMsg))
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("AUTH_DEBUG", "Network registration failure", t)
                onFailure(t.message ?: "Network error. Please try again.")
            }
        })
    }

    /**
     * Login existing user with Email and Password
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            onFailure("Email and password are required.")
            return
        }

        Log.d("AUTH_DEBUG", "Attempting REST API login for: $trimmedEmail")

        val request = LoginRequest(trimmedEmail, password)
        apiService.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val role = body.role.trim().uppercase()

                    sessionManager.saveSession(
                        token = body.token,
                        uid = body.uid,
                        email = body.email,
                        role = role,
                        name = body.fullName,
                        phone = body.phone,
                        profileImage = body.profileImage
                    )

                    Log.d("AUTH_DEBUG", "REST login SUCCESS | Role=$role")
                    onSuccess(role)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Invalid email or password."
                    Log.e("AUTH_DEBUG", "Login failed: $errorMsg")
                    onFailure(parseErrorMessage(errorMsg))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("AUTH_DEBUG", "Network login failure", t)
                onFailure(t.message ?: "Network error. Please try again.")
            }
        })
    }

    /**
     * Forgot Password / Password Reset
     */
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Since local backend does not send emails, we mock it as successful
        onSuccess()
    }

    /**
     * Logout
     */
    fun logout() {
        sessionManager.clearSession()
    }

    fun firebaseAuthWithGoogle(
        credential: Any,
        selectedRole: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("GOOGLE_AUTH_DEBUG", "firebaseAuthWithGoogle started with selectedRole: $selectedRole")
        
        val idToken: String
        val email: String
        val fullName: String

        if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
            idToken = credential.idToken
            email = credential.id
            fullName = credential.displayName ?: "Google User"
        } else {
            idToken = credential.toString()
            email = "google_user_${System.currentTimeMillis() % 100000}@medplus.com"
            fullName = "Google Test User"
        }

        Log.d("GOOGLE_AUTH_DEBUG", "Sending Google authentication request for $email as $selectedRole")

        val request = GoogleLoginRequest(
            idToken = idToken,
            email = email,
            fullName = fullName,
            role = selectedRole.uppercase()
        )

        apiService.loginWithGoogle(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val role = body.role.trim().uppercase()

                    sessionManager.saveSession(
                        token = body.token,
                        uid = body.uid,
                        email = body.email,
                        role = role,
                        name = body.fullName,
                        phone = body.phone,
                        profileImage = body.profileImage
                    )

                    Log.d("GOOGLE_AUTH_DEBUG", "Google login SUCCESS | Role=$role")
                    onSuccess(role)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Google authentication failed."
                    Log.e("GOOGLE_AUTH_DEBUG", "Google login failed: $errorMsg")
                    onFailure(parseErrorMessage(errorMsg))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("GOOGLE_AUTH_DEBUG", "Network Google login failure", t)
                onFailure(t.message ?: "Network error. Please try again.")
            }
        })
    }

    /**
     * Save user role (mock or compatibility helper)
     */
    fun saveUserRole(
        role: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = sessionManager.getUserId() ?: ""
        if (uid.isEmpty()) {
            onFailure("No user is logged in.")
            return
        }

        // Just update role in session since registration already configures it
        sessionManager.saveSession(
            token = sessionManager.getToken() ?: "",
            uid = uid,
            email = sessionManager.getEmail() ?: "",
            role = role.uppercase(),
            name = sessionManager.getName() ?: "",
            phone = sessionManager.getPhone() ?: "",
            profileImage = sessionManager.getProfileImage() ?: ""
        )
        onSuccess()
    }

    private fun parseErrorMessage(rawError: String): String {
        try {
            val jsonObject = org.json.JSONObject(rawError)
            if (jsonObject.has("error")) {
                val errorVal = jsonObject.getString("error")
                if (errorVal.contains("auth/email-already-in-use")) {
                    return "This email address is already in use. Please sign in."
                }
                if (errorVal.contains("auth/weak-password")) {
                    return "Password is too weak. It should be at least 6 characters."
                }
                if (errorVal.contains("auth/invalid-email")) {
                    return "Invalid email address format."
                }
                return errorVal.replace("Firebase: Error ", "").replace("(", "").replace(")", "").trim()
            }
        } catch (e: Exception) {
            // fallback
        }
        return rawError
    }
}
