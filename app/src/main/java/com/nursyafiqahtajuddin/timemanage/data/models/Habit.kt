package com.nursyafiqahtajuddin.timemanage.data.models

import java.util.Date

data class Habit(
    val habitId: String = "",
    val userID: String = "",
    val habitTitle: String = "",
    val habitFrequency: List<String> = listOf(),
    val habitCompletion: MutableList<Boolean> = mutableListOf(),
    val habitReminders: Date? = null,
    val habitDescription: String = ""
)