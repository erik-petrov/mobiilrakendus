package com.example.honk.notifications

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DailySummaryScheduler {
    private const val UNIQUE_NAME = "daily_summary_work"

    fun scheduleNext(ctx: Context) {
        val appCtx = ctx.applicationContext

        if (!DailySummaryPrefs.isEnabled(appCtx)) {
            cancel(appCtx)
            return
        }

        val hour = DailySummaryPrefs.getHour(appCtx)
        val minute = DailySummaryPrefs.getMinute(appCtx)
        val delayMs = computeDelayMs(hour, minute)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Firestore
            .build()

        val request = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(appCtx).enqueueUniqueWork(
            UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(ctx: Context) {
        WorkManager.getInstance(ctx.applicationContext).cancelUniqueWork(UNIQUE_NAME)
    }

    private fun computeDelayMs(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (next.timeInMillis <= now.timeInMillis) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis - now.timeInMillis
    }
}