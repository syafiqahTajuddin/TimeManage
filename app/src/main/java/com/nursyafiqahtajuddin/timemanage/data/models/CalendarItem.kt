package com.nursyafiqahtajuddin.timemanage.data.models

data class CalendarItem(
    val date: String,
    val isInCurrentMonth: Boolean,
    val tasks: List<String> = emptyList()
)