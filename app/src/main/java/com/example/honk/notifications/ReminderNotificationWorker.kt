package com.example.honk.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.honk.MainActivity
import com.example.honk.R

class ReminderNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Android 13+ — if there is no permission, leave quietly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return Result.success()
        }

        val title = inputData.getString(KEY_TITLE) ?: "Upcoming task"
        val message = inputData.getString(KEY_MESSAGE) ?: ""
        val openDate = inputData.getString(KEY_OPEN_DATE) ?: ""
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_DATE, openDate)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(
            applicationContext,
            NotificationHelper.CHANNEL_TASK_REMINDERS
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notif)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_OPEN_DATE = "open_date"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}