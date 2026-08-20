package com.example.medplus.repository

import android.content.Context
import com.example.medplus.auth.model.User
import com.example.medplus.data.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository {

    private val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
    private val apiService: ApiService get() = RetrofitClient.getClient(context)

    fun saveUser(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val request = UpdateProfileRequest(fullName = user.fullName, phone = user.phone, profileImage = user.profileImage)
        apiService.updateUserProfile(user.uid, request).enqueue(object : Callback<MsgResponse> {
            override fun onResponse(call: Call<MsgResponse>, response: Response<MsgResponse>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(Exception(response.errorBody()?.string() ?: "Failed to save user profile"))
                }
            }

            override fun onFailure(call: Call<MsgResponse>, t: Throwable) {
                onFailure(Exception(t.message ?: "Network error", t))
            }
        })
    }

    fun getUser(
        uid: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        apiService.getUserProfile(uid).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val user = User(
                        uid = body.uid,
                        fullName = body.fullName,
                        email = body.email,
                        phone = body.phone,
                        role = body.role,
                        profileImage = body.profileImage ?: "",
                        status = body.status ?: "ACTIVE"
                    )
                    onSuccess(user)
                } else {
                    onSuccess(null)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                onFailure(Exception(t.message ?: "Network error", t))
            }
        })
    }

    fun getUserRole(
        uid: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUser(uid, { user ->
            onSuccess(user?.role)
        }, {
            onFailure(it)
        })
    }

    fun getUsersByRole(
        role: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (role.uppercase() == "PATIENT") {
            apiService.getAdminPatients().enqueue(object : Callback<List<UserResponse>> {
                override fun onResponse(call: Call<List<UserResponse>>, response: Response<List<UserResponse>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.map { body ->
                            User(
                                uid = body.uid,
                                fullName = body.fullName,
                                email = body.email,
                                phone = body.phone,
                                role = body.role,
                                profileImage = body.profileImage ?: "",
                                status = body.status ?: "ACTIVE"
                            )
                        }
                        onSuccess(list)
                    } else {
                        onSuccess(emptyList())
                    }
                }

                override fun onFailure(call: Call<List<UserResponse>>, t: Throwable) {
                    onFailure(Exception(t.message ?: "Network error", t))
                }
            })
        } else if (role.uppercase() == "DOCTOR") {
            apiService.getDoctors().enqueue(object : Callback<List<DoctorProfileResponse>> {
                override fun onResponse(call: Call<List<DoctorProfileResponse>>, response: Response<List<DoctorProfileResponse>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.map { body ->
                            User(
                                uid = body.uid,
                                fullName = body.fullName,
                                email = body.email,
                                phone = body.phone ?: "",
                                role = "DOCTOR",
                                profileImage = body.profileImage ?: "",
                                status = body.verificationStatus ?: "PENDING"
                            )
                        }
                        onSuccess(list)
                    } else {
                        onSuccess(emptyList())
                    }
                }

                override fun onFailure(call: Call<List<DoctorProfileResponse>>, t: Throwable) {
                    onFailure(Exception(t.message ?: "Network error", t))
                }
            })
        } else {
            onSuccess(emptyList())
        }
    }
}