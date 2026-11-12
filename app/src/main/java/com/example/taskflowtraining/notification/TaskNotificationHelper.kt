package com.example.taskflowtraining.notification

import com.example.taskflowtraining.R
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.taskflowtraining.MainActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TaskNotificationHelper(
    private val context: Context
) {
    private fun createTaskPendingIntent(taskId: Long) =
        NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.detailFragment)
            .setArguments(
                bundleOf("taskId" to taskId)
            )
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()

    fun showTaskReminder(taskId: Long, taskTitle: String): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permission != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        val notification = NotificationCompat.Builder(
            context,
            "task_reminders_high"
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder))
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createTaskPendingIntent(taskId))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(
                "task_reminder_$taskId",
                0,
                notification
            )

        return true
    }
}