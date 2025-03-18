package com.nursyafiqahtajuddin.timemanage.ui.tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nursyafiqahtajuddin.timemanage.MainActivity
import com.nursyafiqahtajuddin.timemanage.R

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val taskTitle = intent?.getStringExtra("taskTitle") ?: "Task"
        val taskMessage = intent?.getStringExtra("taskMessage") ?: "You have a task due soon!"

        showNotification(context, taskTitle, taskMessage)
    }

    private fun showNotification(context: Context, taskTitle: String, taskMessage: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tasks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(taskTitle)
            .setContentText(taskMessage)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
