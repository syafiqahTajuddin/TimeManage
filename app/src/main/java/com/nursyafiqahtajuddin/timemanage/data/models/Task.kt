package com.nursyafiqahtajuddin.timemanage.data.models

import java.util.Date

data class Task(
    val taskID: String = "",
    val userID: String = "",
    val taskTitle: String = "",
    val pomodoroEstimated: Int? = null,
    val taskCreationDate: Date? = null,
    val taskDeadline: Date? = null,
    val notificationDate: Date? = null,
    val taskPriority: String = "",
    var taskStatus: String = "Pending",
    val taskDescription: String = ""
)