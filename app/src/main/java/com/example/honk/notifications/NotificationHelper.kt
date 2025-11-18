package com.example.honk.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.honk.R
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object NotificationHelper {

    // ID channel for all notif
    const val CHANNEL_TASK_REMINDERS = "channel_task_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val taskChannel = NotificationChannel(
                CHANNEL_TASK_REMINDERS,
                "Task reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming tasks"
            }

            manager.createNotificationChannel(taskChannel)
        }
    }

    fun showSimpleNotification(
        context: Context,
        title: String,
        message: String
    ) {
        // checking POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                return
            }
        }

        // unic id
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // may be changed
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}