package com.example.medplus.repository

import com.example.medplus.auth.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    fun saveUser(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(user.uid).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUser(
        uid: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(document.toObject(User::class.java))
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserRole(
        uid: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                onSuccess(document.getString("role"))
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUsersByRole(
        role: String,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .whereEqualTo("role", role)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
                onSuccess(users)
            }
            .addOnFailureListener { onFailure(it) }
    }
}