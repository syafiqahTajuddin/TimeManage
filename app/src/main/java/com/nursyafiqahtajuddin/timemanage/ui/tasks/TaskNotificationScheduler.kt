package com.nursyafiqahtajuddin.timemanage.ui.tasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.nursyafiqahtajuddin.timemanage.data.models.Task

class TaskNotificationScheduler(private val context: Context) {

    fun scheduleTaskNotification(task: Task, notificationTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("taskTitle", task.taskTitle)
            putExtra("taskMessage", task.taskDescription)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, task.taskID.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent
        )
    }
}