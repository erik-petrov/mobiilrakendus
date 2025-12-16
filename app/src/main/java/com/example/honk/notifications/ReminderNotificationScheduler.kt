package com.example.honk.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.honk.model.Reminder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import org.json.JSONArray

object ReminderNotificationScheduler {

    // ---- PUBLIC API ----

    fun schedule(context: Context, reminder: Reminder) {
        val shouldSchedule = shouldSchedule(reminder)
        if (!shouldSchedule) {
            cancel(context, reminder.id)
            return
        }

        val taskTimeMillis = parseDateTimeToMillis(reminder.date, reminder.time) ?: run {
            cancel(context, reminder.id); return
        }

        val offsetMillis = reminder.reminderOffset.minutes * 60_000L
        val triggerAt = taskTimeMillis - offsetMillis
        val delay = triggerAt - System.currentTimeMillis()

        if (delay <= 0) { // late
            cancel(context, reminder.id)
            return
        }

        val message = buildString {
            append("Task: ${reminder.text}")
            if (reminder.time.isNotBlank()) append(" at ${reminder.time}")
            if (reminder.date.isNotBlank()) append(" on ${reminder.date}")
        }

        val data = workDataOf(
            ReminderNotificationWorker.KEY_TITLE to "Upcoming task",
            ReminderNotificationWorker.KEY_MESSAGE to message,
            ReminderNotificationWorker.KEY_OPEN_DATE to reminder.date,
            ReminderNotificationWorker.KEY_NOTIFICATION_ID to reminder.notificationId.toInt()
        )

        val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName(reminder.id),
            ExistingWorkPolicy.REPLACE, // for editing
            request
        )

        markScheduledIds(context, getScheduledIds(context) + reminder.id)
    }

    fun cancel(context: Context, reminderId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(reminderId))
        markScheduledIds(context, getScheduledIds(context) - reminderId)
    }

    /**
     * Call when getting list from Firestore (snapshot).
     * - recreates current ones
     * - cancels extra (deleted/offset/invalid)
     */
    fun syncFromFirestore(context: Context, reminders: List<Reminder>) {
        val activeIds = mutableSetOf<String>()

        for (r in reminders) {
            if (shouldSchedule(r) && parseDateTimeToMillis(r.date, r.time) != null) {
                // schedule do himself REPLACE
                schedule(context, r)
                activeIds.add(r.id)
            } else {
                cancel(context, r.id)
            }
        }

        // if something was planned earlier, but disappeared from the Firestore list
        val previously = getScheduledIds(context)
        val removed = previously - reminders.map { it.id }.toSet()
        for (id in removed) cancel(context, id)

        markScheduledIds(context, activeIds)
    }

    // ---- INTERNALS ----

    private fun shouldSchedule(r: Reminder): Boolean {
        return !r.isDone &&
                r.reminderOffset.minutes > 0 &&
                r.date.isNotBlank() &&
                r.time.isNotBlank()
    }

    private fun uniqueName(reminderId: String) = "reminder_$reminderId"

    private fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long? {
        return try {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            format.parse("$dateStr $timeStr")?.time
        } catch (_: Exception) {
            null
        }
    }

    // ---- PERSIST scheduled ids ----

    private const val PREFS = "notif_prefs"
    private const val KEY_IDS = "scheduled_ids"

    private fun getScheduledIds(context: Context): Set<String> {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_IDS, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableSetOf<String>()
        for (i in 0 until arr.length()) out.add(arr.getString(i))
        return out
    }

    private fun markScheduledIds(context: Context, ids: Set<String>) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        ids.forEach { arr.put(it) }
        sp.edit().putString(KEY_IDS, arr.toString()).apply()
    }
}