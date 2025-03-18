package com.nursyafiqahtajuddin.timemanage.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class User(val username: String, val email: String)

class ProfileViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> get() = _userData

    fun fetchUserData() {
        val user = firebaseAuth.currentUser
        user?.let {
            Log.d("ProfileViewModel", "Fetching data for user ID: ${it.uid}")

            firestore.collection("Users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "Unknown"
                        val email = document.getString("userEmail") ?: "Unknown"

                        Log.d("ProfileViewModel", "Fetched username: $username, email: $email")
                        _userData.value = User(username, email)
                    } else {
                        Log.e("ProfileViewModel", "Document does not exist for user ID: ${it.uid}")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error fetching user data: ${e.message}", e)
                }
        }
    }

    // Sign out function
    fun signOut() {
        firebaseAuth.signOut()
    }

    // Update user data
    fun updateUserData(username: String, email: String, onComplete: (Boolean) -> Unit) {
        val user = firebaseAuth.currentUser
        user?.let {
            Log.d("ProfileViewModel", "Updating user data: username=$username, email=$email for user ID=${it.uid}")

            val userUpdates = mapOf(
                "username" to username,
                "userEmail" to email
            )

            firestore.collection("Users").document(it.uid).update(userUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ProfileViewModel", "User data updated successfully")
                    } else {
                        Log.e("ProfileViewModel", "User data update failed")
                    }
                    onComplete(task.isSuccessful)
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error updating user data: ${e.message}", e)
                    onComplete(false)
                }
        } ?: run {
            Log.e("ProfileViewModel", "User is null. Cannot update data.")
            onComplete(false)
        }
    }
}