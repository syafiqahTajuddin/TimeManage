package com.nursyafiqahtajuddin.timemanage.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nursyafiqahtajuddin.timemanage.data.models.Habit
import kotlinx.coroutines.tasks.await

class HabitRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun addHabit(habit: Habit): Boolean {
        return try {
            val docRef = db.collection("Habit").document()
            val updatedHabit = habit.copy(habitId = docRef.id)
            docRef.set(updatedHabit).await()
            true
        } catch (e: Exception) {
            println("Error adding habit: ${e.message}")
            false
        }
    }

    suspend fun getAllHabits(): List<Habit> {
        return try {
            val snapshot = db.collection("Habit")
                .whereEqualTo("userID", userID)
                .get()
                .await()
            snapshot.toObjects(Habit::class.java)
        } catch (e: Exception) {
            println("Error fetching habits: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateHabitCompletion(habitId: String, completionList: List<Boolean>): Boolean {
        return try {
            val docRef = db.collection("Habit").document(habitId)
            docRef.update("habitCompletion", completionList).await()
            true
        } catch (e: Exception) {
            println("Error updating habit completion: ${e.message}")
            false
        }
    }

}