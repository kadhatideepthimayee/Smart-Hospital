package com.example.medplus.repository

import com.example.medplus.auth.model.User
import com.example.medplus.data.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService = RetrofitClient.getApiService(context)

    fun saveUser(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Backend handles saving user profile during register, so we just return success
        onSuccess()
    }

    fun getUser(
        uid: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getMe()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure(Exception("Failed to get user details"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }

    fun getUserRole(
        uid: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getMe()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess(response.body()?.role)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure(Exception("Failed to get user role"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }

    fun getUsersByRole(
        role: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (role == "DOCTOR") {
                    val response = apiService.getAllDoctorProfiles()
                    if (response.isSuccessful) {
                        val users = response.body()?.map { doc ->
                            User(
                                uid = doc.uid,
                                fullName = doc.fullName,
                                email = doc.email,
                                phone = doc.phone,
                                role = "DOCTOR",
                                profileImage = doc.profileImage
                            )
                        } ?: emptyList()
                        withContext(Dispatchers.Main) {
                            onSuccess(users)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onFailure(Exception("Failed to fetch doctors"))
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onSuccess(emptyList())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }
}