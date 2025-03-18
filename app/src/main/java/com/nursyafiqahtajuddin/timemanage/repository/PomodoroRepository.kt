package com.nursyafiqahtajuddin.timemanage.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PomodoroRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val timerCollection = firestore.collection("Timer")

    // Load settings from Firestore
    suspend fun getPomodoroSettings(): Map<String, Any>? {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return try {
            val document = timerCollection.document(userID).get().await()
            document.data
        } catch (e: Exception) {
            println("Error loading settings: ${e.message}")
            null
        }
    }

    // Save settings to Firestore
    suspend fun savePomodoroSettings(userID: String, updatedFields: Map<String, Any>): Boolean {
        val timerRef = FirebaseFirestore.getInstance().collection("Timer").document(userID)

        return try {
            timerRef.update(updatedFields).await() // Update only the changed fields
            println("Timer settings updated successfully for userID: $userID")
            true
        } catch (e: Exception) {
            println("Firestore update failed: ${e.message}")
            false
        }
    }

    suspend fun savePomodoroSessionCount(sessionCount: Int): Boolean {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        return try {
            FirebaseFirestore.getInstance()
                .collection("Timer")
                .document(userID)
                .update("sessionNumber", sessionCount)
                .await()
            true
        } catch (e: Exception) {
            println("Error updating session count: ${e.message}")
            false
        }
    }

    // Fetch focus times (today, weekly, monthly)
    suspend fun getFocusTimes(userID: String): Map<String, Any>? {
        return try {
            val document = timerCollection.document(userID).get().await()
            document.data
        } catch (e: Exception) {
            println("Error fetching focus times: ${e.message}")
            null
        }
    }

    // Update focus times after Pomodoro session
    suspend fun updateFocusTimes(userID: String, sessionDuration: Int): Boolean {
        val timerRef = timerCollection.document(userID)

        return try {
            // Fetch current values
            val currentData = getFocusTimes(userID) ?: return false
            val todayFocusTime = (currentData["todayFocusTime"] as? Long)?.toInt() ?: 0
            val weeklyFocusTime = (currentData["weeklyFocusTime"] as? Long)?.toInt() ?: 0
            val monthlyFocusTime = (currentData["monthlyFocusTime"] as? Long)?.toInt() ?: 0

            // Calculate updated focus times
            val updatedTodayFocusTime = todayFocusTime + sessionDuration
            val updatedWeeklyFocusTime = weeklyFocusTime + sessionDuration
            val updatedMonthlyFocusTime = monthlyFocusTime + sessionDuration

            // Update Firestore
            val updatedFields = mapOf(
                "todayFocusTime" to updatedTodayFocusTime,
                "weeklyFocusTime" to updatedWeeklyFocusTime,
                "monthlyFocusTime" to updatedMonthlyFocusTime
            )

            timerRef.update(updatedFields).await()

            println("Focus times updated successfully.")
            true
        } catch (e: Exception) {
            println("Error updating focus times: ${e.message}")
            false
        }
    }

}