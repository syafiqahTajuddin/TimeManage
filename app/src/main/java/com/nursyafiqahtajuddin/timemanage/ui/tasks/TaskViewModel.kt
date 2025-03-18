package com.nursyafiqahtajuddin.timemanage.ui.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository

class TaskViewModel : ViewModel() {

    private val taskRepository = TaskRepository()
    val _tasks = MutableLiveData<List<Task>>()
    val _completedTasks = MutableLiveData<Int>()
    val _progress = MutableLiveData<Int>()

    // Add a new task
    fun addTask(task: Task) {
        taskRepository.addTask(task) { success ->
            if (success) {
                fetchTasks()
            } else {
                // Handle failure
            }
        }
    }

    // Fetch tasks from Firestore
    fun fetchTasks() {
        viewModelScope.launch {
            val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            taskRepository.getAllTasks(userID) { taskList ->
                Log.d("fetchTasks", "Task List: $taskList")
                _tasks.postValue(taskList)
                calculateProgress(taskList)
            }
        }
    }

    // Update an existing task
    fun updateTaskStatus(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task) { success ->
                if (success) {
                    fetchTasks()
                }
            }
        }
    }

    private fun calculateProgress(taskList: List<Task>) {
        // Count completed tasks
        val completed = taskList.count { it.taskStatus == "Completed" }
        _completedTasks.postValue(completed)

        // Calculate progress percentage
        val total = taskList.size
        val progress = if (total > 0) {
            (completed.toFloat() / total * 100).toInt()
        } else {
            0
        }
        _progress.postValue(progress)
    }
}