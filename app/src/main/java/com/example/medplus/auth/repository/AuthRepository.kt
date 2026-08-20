package com.example.medplus.auth.repository

import android.content.Context
import android.util.Log
import com.example.medplus.data.network.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    
    // We get application context dynamically to initialize SessionManager
    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val sessionManager = SessionManager.getInstance(context)

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
        Log.d("AUTH_DEBUG", "Firebase Registration started for $email with role $role")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                
                val userMap = hashMapOf(
                    "uid" to uid,
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone,
                    "role" to role,
                    "createdAt" to Timestamp.now()
                )

                firestore.collection("users").document(uid).set(userMap)
                    .addOnSuccessListener {
                        // Store local session details for compat with existing Compose UI
                        sessionManager.saveSession(
                            token = "firebase_session_token",
                            uid = uid,
                            email = email,
                            role = role,
                            name = fullName,
                            phone = phone,
                            profileImage = ""
                        )
                        Log.d("AUTH_DEBUG", "Firebase Registration successful (Session stored)")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("AUTH_DEBUG", "Firestore profile creation failed", e)
                        onFailure(e.message ?: "Failed to save user profile in Firestore")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AUTH_DEBUG", "Firebase Auth registration failed", e)
                onFailure(e.message ?: "Registration failed.")
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

        Log.d("AUTH_DEBUG", "Attempting Firebase Auth login for: $trimmedEmail")

        // Check if admin credentials are provided
        val isAdminEmail = trimmedEmail.equals("kadhatideepthimayee@gmail.com", ignoreCase = true) || 
                           trimmedEmail.equals("admin@medplus.com", ignoreCase = true)
        if (isAdminEmail && password == "deepthi@123") {
            Log.d("AUTH_DEBUG", "Admin credentials intercepted. Ensuring admin account setup in Firebase.")
            auth.signInWithEmailAndPassword(trimmedEmail, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: ""
                    val userMap = hashMapOf(
                        "uid" to uid,
                        "fullName" to "Admin Deepthi",
                        "email" to trimmedEmail,
                        "role" to "ADMIN",
                        "phone" to "1234567890",
                        "profileImage" to ""
                    )
                    firestore.collection("users").document(uid).set(userMap, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            sessionManager.saveSession(
                                token = "firebase_session_token",
                                uid = uid,
                                email = trimmedEmail,
                                role = "ADMIN",
                                name = "Admin Deepthi",
                                phone = "1234567890",
                                profileImage = ""
                            )
                            Log.d("AUTH_DEBUG", "Admin login successful (Existing account updated)")
                            onSuccess("ADMIN")
                        }
                        .addOnFailureListener { e ->
                            Log.e("AUTH_DEBUG", "Failed to update admin profile in Firestore", e)
                            onFailure(e.message ?: "Failed to update admin profile in Firestore")
                        }
                }
                .addOnFailureListener { signInError ->
                    Log.d("AUTH_DEBUG", "Admin sign-in failed, attempting to register: ${signInError.message}")
                    auth.createUserWithEmailAndPassword(trimmedEmail, password)
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid ?: ""
                            val userMap = hashMapOf(
                                "uid" to uid,
                                "fullName" to "Admin Deepthi",
                                "email" to trimmedEmail,
                                "role" to "ADMIN",
                                "phone" to "1234567890",
                                "profileImage" to ""
                            )
                            firestore.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener {
                                    sessionManager.saveSession(
                                        token = "firebase_session_token",
                                        uid = uid,
                                        email = trimmedEmail,
                                        role = "ADMIN",
                                        name = "Admin Deepthi",
                                        phone = "1234567890",
                                        profileImage = ""
                                    )
                                    Log.d("AUTH_DEBUG", "Admin login successful (New account registered)")
                                    onSuccess("ADMIN")
                                }
                                .addOnFailureListener { e ->
                                    onFailure(e.message ?: "Failed to save admin profile in Firestore")
                                }
                        }
                        .addOnFailureListener { signUpError ->
                            Log.e("AUTH_DEBUG", "Admin registration failed", signUpError)
                            if (signUpError is com.google.firebase.auth.FirebaseAuthUserCollisionException || 
                                signUpError.message?.contains("already in use", ignoreCase = true) == true) {
                                if (trimmedEmail.equals("kadhatideepthimayee@gmail.com", ignoreCase = true)) {
                                    onFailure("This email is already in use. Please sign in via Google to auto-upgrade to Admin, or use email admin@medplus.com with password deepthi@123.")
                                } else {
                                    onFailure("This email is already in use by another account.")
                                }
                            } else {
                                onFailure(signUpError.message ?: "Admin sign-in/registration failed.")
                            }
                        }
                }
            return
        }

        auth.signInWithEmailAndPassword(trimmedEmail, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val fullName = document.getString("fullName") ?: ""
                            val userEmail = document.getString("email") ?: trimmedEmail
                            val phone = document.getString("phone") ?: ""
                            val role = (document.getString("role") ?: "PATIENT").trim().uppercase()
                            val profileImage = document.getString("profileImage") ?: ""

                            sessionManager.saveSession(
                                token = "firebase_session_token",
                                uid = uid,
                                email = userEmail,
                                role = role,
                                name = fullName,
                                phone = phone,
                                profileImage = profileImage
                            )

                            Log.d("AUTH_DEBUG", "Firebase login SUCCESS | Role=$role")
                            onSuccess(role)
                        } else {
                            Log.w("AUTH_DEBUG", "User profile document not found. Attempting auto-recovery...")
                            firestore.collection("doctor_profiles").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    val detectedRole = if (doc.exists()) "DOCTOR" else "PATIENT"
                                    val fallbackName = if (doc.exists()) {
                                        doc.getString("fullName") ?: trimmedEmail.substringBefore("@")
                                    } else {
                                        trimmedEmail.substringBefore("@")
                                    }
                                    val fallbackPhone = if (doc.exists()) doc.getString("phone") ?: "" else ""
                                    
                                    val userMap = hashMapOf(
                                        "uid" to uid,
                                        "fullName" to fallbackName,
                                        "email" to trimmedEmail,
                                        "phone" to fallbackPhone,
                                        "role" to detectedRole,
                                        "createdAt" to Timestamp.now()
                                    )
                                    
                                    firestore.collection("users").document(uid).set(userMap)
                                        .addOnSuccessListener {
                                            sessionManager.saveSession(
                                                token = "firebase_session_token",
                                                uid = uid,
                                                email = trimmedEmail,
                                                role = detectedRole,
                                                name = fallbackName,
                                                phone = fallbackPhone,
                                                profileImage = ""
                                            )
                                            Log.d("AUTH_DEBUG", "Recovered user profile document. Role=$detectedRole")
                                            onSuccess(detectedRole)
                                        }
                                        .addOnFailureListener { e ->
                                            onFailure("Profile data not found, and recovery failed: ${e.message}")
                                        }
                                }
                                .addOnFailureListener {
                                    onFailure("User profile data not found in database.")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to load user profile from database.")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AUTH_DEBUG", "Firebase Auth sign-in failed", e)
                onFailure(e.message ?: "Invalid email or password.")
            }
    }

    /**
     * Forgot Password / Password Reset
     */
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to send reset email.")
            }
    }

    /**
     * Logout
     */
    fun logout() {
        auth.signOut()
        sessionManager.clearSession()
    }

    /**
     * Google Sign-In helper method
     */
    fun firebaseAuthWithGoogle(
        credential: Any,
        selectedRole: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val idToken = credential as? String
        if (idToken.isNullOrEmpty()) {
            onFailure("Invalid Google ID token.")
            return
        }

        Log.d("AUTH_DEBUG", "Attempting Firebase Google login for role: $selectedRole")
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(authCredential)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val email = authResult.user?.email ?: ""
                val name = authResult.user?.displayName ?: "Google User"

                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        val isTargetAdmin = email.equals("kadhatideepthimayee@gmail.com", ignoreCase = true)
                        
                        if (document.exists()) {
                            val fetchedRole = if (isTargetAdmin) "ADMIN" else (document.getString("role") ?: selectedRole).trim().uppercase()
                            val phone = document.getString("phone") ?: ""
                            val profileImage = document.getString("profileImage") ?: ""

                            if (isTargetAdmin && document.getString("role") != "ADMIN") {
                                Log.d("AUTH_DEBUG", "Upgrading existing Google user $email to ADMIN role in Firestore")
                                firestore.collection("users").document(uid).update("role", "ADMIN")
                            }

                            sessionManager.saveSession(
                                token = "firebase_session_token",
                                uid = uid,
                                email = email,
                                role = fetchedRole,
                                name = name,
                                phone = phone,
                                profileImage = profileImage
                            )

                            Log.d("AUTH_DEBUG", "Firebase Google Sign-In SUCCESS | Role=$fetchedRole")
                            onSuccess(fetchedRole)
                        } else {
                            // First time sign-in with Google, create profile doc
                            val finalRole = if (isTargetAdmin) "ADMIN" else selectedRole
                            val userMap = hashMapOf(
                                "uid" to uid,
                                "fullName" to name,
                                "email" to email,
                                "phone" to "",
                                "role" to finalRole,
                                "createdAt" to Timestamp.now()
                            )

                            firestore.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener {
                                    sessionManager.saveSession(
                                        token = "firebase_session_token",
                                        uid = uid,
                                        email = email,
                                        role = finalRole,
                                        name = name,
                                        phone = "",
                                        profileImage = ""
                                    )
                                    Log.d("AUTH_DEBUG", "New Google user Firestore profile created | Role=$finalRole")
                                    onSuccess(finalRole)
                                }
                                .addOnFailureListener { e ->
                                    onFailure(e.message ?: "Failed to initialize user profile.")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Failed to read profile data from database.")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AUTH_DEBUG", "Firebase Google authentication failed", e)
                onFailure(e.message ?: "Google Sign-in failed.")
            }
    }

    /**
     * Save user role (mock or compatibility helper)
     */
    fun saveUserRole(
        role: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: ""
        if (uid.isEmpty()) {
            onFailure("No user is logged in.")
            return
        }

        firestore.collection("users").document(uid).update("role", role)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to update role.")
            }
    }
}
