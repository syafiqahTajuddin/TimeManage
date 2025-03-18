package com.nursyafiqahtajuddin.timemanage.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import java.util.Date

class TaskRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val taskCollection = firestore.collection("Task")

    fun addTask(task: Task, callback: (Boolean) -> Unit) {
        if (task.userID.isEmpty()) {
            Log.e("TaskRepository", "userID is empty. Cannot add task.")
            callback(false)
            return
        }

        val document = taskCollection.document()
        val taskWithID = task.copy(taskID = document.id)

        document.set(taskWithID)
            .addOnSuccessListener {
                Log.d("TaskRepository", "Task successfully added!")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("TaskRepository", "Error adding task", e)
                callback(false)
            }
    }

    fun updateTask(task: Task, callback: (Boolean) -> Unit) {
        if (task.taskID.isEmpty()) {
            callback(false)
            return
        }

        taskCollection.document(task.taskID).set(task, SetOptions.merge())
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("TaskRepository", "Update failed: ${exception.message}")
                callback(false)
            }
    }

    fun deleteTask(taskID: String, callback: (Boolean) -> Unit) {
        taskCollection.document(taskID).delete()
            .addOnSuccessListener {
                Log.d("TaskRepository", "Task deleted successfully: $taskID")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("TaskRepository", "Failed to delete task: $taskID", exception)
                callback(false)
            }
    }

    fun getAllTasks(userID: String, callback: (List<Task>) -> Unit) {
        taskCollection.whereEqualTo("userID", userID).get()
            .addOnSuccessListener { querySnapshot ->
                val tasks = querySnapshot.toObjects(Task::class.java)
                Log.d("TaskRepository", "Retrieved ${tasks.size} tasks for user $userID")
                tasks.forEach { task ->
                    Log.d("TaskRepository", "Task: ${task.taskTitle}, Priority: ${task.taskPriority}")
                }
                callback(tasks)
            }
            .addOnFailureListener { exception ->
                Log.e("TaskRepository", "Failed to fetch tasks: ${exception.message}")
                callback(emptyList())
            }
    }

    fun getTaskById(taskID: String, callback: (Task?, Boolean) -> Unit) {
        taskCollection.document(taskID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val task = document.toObject(Task::class.java)
                    callback(task, true)
                } else {
                    callback(null, false)
                }
            }
            .addOnFailureListener {
                callback(null, false)
            }
    }

    fun getTasksByTimeRange(
        userID: String,
        startDate: Date,
        endDate: Date,
        callback: (List<Task>) -> Unit
    ) {
        taskCollection
            .whereEqualTo("userID", userID)
            .whereGreaterThanOrEqualTo("taskDeadline", startDate)
            .whereLessThanOrEqualTo("taskDeadline", endDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tasks = querySnapshot.toObjects(Task::class.java)
                callback(tasks)
            }
            .addOnFailureListener { e ->
                Log.e("TaskRepository", "Failed to fetch tasks by range: ${e.message}")
                Log.e("TaskRepository", "Query Details: userID=$userID, startDate=$startDate, endDate=$endDate")
                callback(emptyList())
            }
    }
}