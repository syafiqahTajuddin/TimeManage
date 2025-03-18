package com.nursyafiqahtajuddin.timemanage.data.models

data class Reminder(
    val reminderId: String = "",
    val reminderTitle: String = "",
    val reminderMessage: String = "",
    val userID: String = "",
    val reminderTime: Long = System.currentTimeMillis()
)