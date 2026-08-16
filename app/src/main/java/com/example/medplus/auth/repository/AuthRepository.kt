package com.example.medplus.auth.repository

import com.example.medplus.auth.model.User
import com.example.medplus.data.network.*
import com.example.medplus.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = RetrofitClient.getApiService(context)
    private val sessionManager = SessionManager.getInstance(context)
    private val userRepository = UserRepository()

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
        android.util.Log.d("DOCTOR_REG_DEBUG", "Registration started for $email with role $role")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val registerRequest = RegisterRequest(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    password = password,
                    role = role
                )
                val response = apiService.register(registerRequest)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        sessionManager.saveSession(
                            token = loginResponse.token ?: "",
                            uid = loginResponse.user?.uid ?: "",
                            email = loginResponse.user?.email ?: "",
                            role = loginResponse.user?.role ?: "",
                            name = loginResponse.user?.fullName ?: "",
                            phone = loginResponse.user?.phone ?: "",
                            profileImage = loginResponse.user?.profileImage ?: ""
                        )
                    }
                    android.util.Log.d("DOCTOR_REG_DEBUG", "Registration successful in MongoDB (Session stored)")
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Registration failed"
                    android.util.Log.e("DOCTOR_REG_DEBUG", "Registration failed: $errorMsg")
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DOCTOR_REG_DEBUG", "Network error during registration", e)
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Registration Failed. Check your network.")
                }
            }
        }
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

        android.util.Log.d("AUTH_DEBUG", "Attempting email login for: $trimmedEmail")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginRequest = LoginRequest(trimmedEmail, password)
                val response = apiService.login(loginRequest)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Store the session
                        sessionManager.saveSession(
                            token = loginResponse.token ?: "",
                            uid = loginResponse.user?.uid ?: "",
                            email = loginResponse.user?.email ?: "",
                            role = loginResponse.user?.role ?: "",
                            name = loginResponse.user?.fullName ?: "",
                            phone = loginResponse.user?.phone ?: "",
                            profileImage = loginResponse.user?.profileImage ?: ""
                        )
                        
                        val role = loginResponse.user.role.trim().uppercase()
                        android.util.Log.d("AUTH_DEBUG", "MongoDB login SUCCESS | Role=$role")
                        withContext(Dispatchers.Main) {
                            onSuccess(role)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onFailure("Login response was empty.")
                        }
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Invalid email or password."
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AUTH_DEBUG", "Network error during login", e)
                withContext(Dispatchers.Main) {
                    onFailure("Connection failed. Check your internet.")
                }
            }
        }
    }

    /**
     * Forgot Password
     */
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Can be mocked since standard SMTP is not set up on basic server,
        // or we can implement mock success
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
        val idToken = credential as? String
        if (idToken == null || idToken.isEmpty()) {
            onFailure("Invalid Google ID token.")
            return
        }

        android.util.Log.d("AUTH_DEBUG", "Attempting Google login for role: $selectedRole")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val googleRequest = GoogleLoginRequest(idToken = idToken, role = selectedRole)
                val response = apiService.googleLogin(googleRequest)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Store the session
                        sessionManager.saveSession(
                            token = loginResponse.token ?: "",
                            uid = loginResponse.user?.uid ?: "",
                            email = loginResponse.user?.email ?: "",
                            role = loginResponse.user?.role ?: "",
                            name = loginResponse.user?.fullName ?: "",
                            phone = loginResponse.user?.phone ?: "",
                            profileImage = loginResponse.user?.profileImage ?: ""
                        )
                        
                        val role = loginResponse.user.role.trim().uppercase()
                        android.util.Log.d("AUTH_DEBUG", "MongoDB Google login SUCCESS | Role=$role")
                        withContext(Dispatchers.Main) {
                            onSuccess(role)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onFailure("Google Login response was empty.")
                        }
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: ""
                    val errorMsg = try {
                        org.json.JSONObject(errorBodyString).getString("msg")
                    } catch (e: Exception) {
                        "Google login failed"
                    }
                    android.util.Log.e("AUTH_DEBUG", "Google Login API Error: $errorBodyString")
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AUTH_DEBUG", "Google Login connection exception", e)
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "Failed to connect to server during Google login")
                }
            }
        }
    }

    fun saveUserRole(
        role: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Mock save user role (normally handled at registration)
        onSuccess()
    }
}
