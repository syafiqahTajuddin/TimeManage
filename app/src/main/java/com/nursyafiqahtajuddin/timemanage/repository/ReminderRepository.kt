package com.nursyafiqahtajuddin.timemanage.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nursyafiqahtajuddin.timemanage.data.models.Reminder

class ReminderRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getUserReminders(userID: String, onResult: (List<Reminder>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("Reminders")
            .whereEqualTo("userID", userID)
            .get()
            .addOnSuccessListener { snapshot ->
                val reminders = snapshot.documents.mapNotNull { it.toObject(Reminder::class.java) }
                onResult(reminders)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}