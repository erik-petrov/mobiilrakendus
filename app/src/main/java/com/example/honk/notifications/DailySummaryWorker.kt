package com.example.honk.notifications

import android.Manifest
import android.app.PendingIntent
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
import com.example.honk.data.firebase.FirebaseModule
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailySummaryWorker(
    appContext: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val appCtx = applicationContext

        if (!DailySummaryPrefs.isEnabled(appCtx)) return Result.success()

        // Android 13+ permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appCtx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                DailySummaryScheduler.scheduleNext(appCtx)
                return Result.success()
            }
        }

        val uid = FirebaseModule.auth.currentUser?.uid
        if (uid == null) {
            DailySummaryScheduler.scheduleNext(appCtx)
            return Result.success()
        }

        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        return try {
            val snap = FirebaseModule.firestore
                .collection("users")
                .document(uid)
                .collection("reminders")
                .whereEqualTo("date", today)
                .get()
                .await()

            val count = snap.documents.count { doc ->
                doc.getBoolean("isDone") != true
            }

            if (count > 0) {
                showSummaryNotification(today, count)
            }

            DailySummaryScheduler.scheduleNext(appCtx)
            Result.success()
        } catch (e: Exception) {
            DailySummaryScheduler.scheduleNext(appCtx)
            Result.retry()
        }
    }

    private fun showSummaryNotification(date: String, count: Int) {
        val appCtx = applicationContext

        // Android 13+ permission check (lint wants it here)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appCtx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(appCtx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_DATE, date)
        }

        val pendingIntent = PendingIntent.getActivity(
            appCtx,
            20001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (count == 1) "You have 1 task today" else "You have $count tasks today"

        val notif = NotificationCompat.Builder(appCtx, NotificationHelper.CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Today's tasks")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(appCtx).notify(20001, notif)
        } catch (_: SecurityException) {
            // permission revoked while app running
        }
    }
}