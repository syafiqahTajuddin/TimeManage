package com.nursyafiqahtajuddin.timemanage.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Timer(
    val focusDuration: Int = 25, // Focus duration in minutes
    val shortBreakDuration: Int = 5, // Short break duration in minutes
    val longBreakDuration: Int = 15, // Long break duration in minutes
    val sessionNumber: Int = 0, // Current session number
    val totalSessions: Int = 4, // Total sessions before a long break
    val isBreak: Boolean = false, // Whether it's a break period
    val taskID: String? = null, // Selected task ID
    val userID: String = "", // User's ID
    val todayFocusTime: Int = 0, // Focus time for today in minutes
    val weeklyFocusTime: Int = 0, // Focus time for the current week in minutes
    val monthlyFocusTime: Int = 0, // Focus time for the current month in minutes
    @ServerTimestamp val startTime: Date? = null, // Start time of session
    @ServerTimestamp val endTime: Date? = null // End time of session
)