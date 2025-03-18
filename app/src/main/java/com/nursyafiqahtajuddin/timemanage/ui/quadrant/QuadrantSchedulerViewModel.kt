package com.nursyafiqahtajuddin.timemanage.ui.quadrant

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository
import java.util.*

class QuadrantSchedulerViewModel : ViewModel() {

    private val taskRepository = TaskRepository()

    private val _importantUrgentTasks = MutableLiveData<List<Task>>()
    val importantUrgentTasks: LiveData<List<Task>> get() = _importantUrgentTasks

    private val _importantNotUrgentTasks = MutableLiveData<List<Task>>()
    val importantNotUrgentTasks: LiveData<List<Task>> get() = _importantNotUrgentTasks

    private val _notImportantUrgentTasks = MutableLiveData<List<Task>>()
    val notImportantUrgentTasks: LiveData<List<Task>> get() = _notImportantUrgentTasks

    private val _notImportantNotUrgentTasks = MutableLiveData<List<Task>>()
    val notImportantNotUrgentTasks: LiveData<List<Task>> get() = _notImportantNotUrgentTasks

    fun fetchAllTasks() {
        taskRepository.getAllTasks("User_ID") { tasks ->
            categorizeTasks(tasks)
        }
    }

    fun fetchTasksForRange(range: String) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time

        val startDate = when (range) {
            "Today" -> calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            "This Week" -> calendar.apply {
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            "This Month" -> calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            else -> Date(0) // Default to all tasks
        }

        calendar.time = endDate
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val adjustedEndDate = calendar.time

        Log.d("fetchTasksForRange", "Start Date: $startDate, End Date: $adjustedEndDate")

        taskRepository.getTasksByTimeRange(
            "User_ID",
            startDate,
            adjustedEndDate
        ) { tasks ->
            Log.d("fetchTasksForRange", "Fetched ${tasks.size} tasks")
            categorizeTasks(tasks)
        }
    }

    fun fetchTasksForCustomRange(startDate: Date, endDate: Date) {
        Log.d("fetchTasksForCustomRange", "Start: $startDate, End: $endDate")

        taskRepository.getTasksByTimeRange(
            "User_ID",
            startDate,
            endDate
        ) { tasks ->
            Log.d("fetchTasksForCustomRange", "Fetched ${tasks.size} tasks")
            categorizeTasks(tasks)
        }
    }

    private fun categorizeTasks(tasks: List<Task>) {
        val importantUrgent = mutableListOf<Task>()
        val importantNotUrgent = mutableListOf<Task>()
        val notImportantUrgent = mutableListOf<Task>()
        val notImportantNotUrgent = mutableListOf<Task>()

        tasks.forEach { task ->
            when (task.taskPriority) {
                "Important + Urgent" -> importantUrgent.add(task)
                "Important + Not Urgent" -> importantNotUrgent.add(task)
                "Not Important + Urgent" -> notImportantUrgent.add(task)
                "Not Important + Not Urgent" -> notImportantNotUrgent.add(task)
            }
        }

        _importantUrgentTasks.postValue(importantUrgent)
        _importantNotUrgentTasks.postValue(importantNotUrgent)
        _notImportantUrgentTasks.postValue(notImportantUrgent)
        _notImportantNotUrgentTasks.postValue(notImportantNotUrgent)
    }

    private fun categorizeTasksByMap(categorizedTasks: Map<String, List<Task>>) {
        _importantUrgentTasks.postValue(categorizedTasks["ImportantUrgent"] ?: emptyList())
        _importantNotUrgentTasks.postValue(categorizedTasks["ImportantNotUrgent"] ?: emptyList())
        _notImportantUrgentTasks.postValue(categorizedTasks["NotImportantUrgent"] ?: emptyList())
        _notImportantNotUrgentTasks.postValue(categorizedTasks["NotImportantNotUrgent"] ?: emptyList())
    }
}