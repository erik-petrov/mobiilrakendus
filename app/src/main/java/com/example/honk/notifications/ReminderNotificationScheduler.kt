package com.example.honk.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.honk.model.Reminder
import com.example.honk.model.ReminderOffset
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import org.json.JSONArray

object ReminderNotificationScheduler {

    // ---- PUBLIC API ----

    fun schedule(context: Context, reminder: Reminder) {
        val appCtx = context.applicationContext

        // date+time must exist
        val taskTimeMillis = parseDateTimeToMillis(reminder.date, reminder.time) ?: run {
            cancel(appCtx, reminder.id)
            return
        }

        // Collect offsets (new multi list OR fallback to old single field)
        val offsets = collectOffsets(reminder)

        // If no offsets -> cancel all for this reminder
        if (reminder.isDone || offsets.isEmpty()) {
            cancel(appCtx, reminder.id)
            return
        }

        // Cancel ALL old works for this reminder first (important for editing)
        WorkManager.getInstance(appCtx).cancelAllWorkByTag(tag(reminder.id))

        var scheduledAny = false

        for (off in offsets) {
            val triggerAt = taskTimeMillis - off.minutes * 60_000L
            val delay = triggerAt - System.currentTimeMillis()
            if (delay <= 0) continue

            val message = buildString {
                append("Task: ${reminder.text}")
                append(" (${formatOffsetShort(off)})")
                if (reminder.time.isNotBlank()) append(" at ${reminder.time}")
                if (reminder.date.isNotBlank()) append(" on ${reminder.date}")
            }

            val notifId = notificationId(reminder.id, off.minutes)

            val data = workDataOf(
                ReminderNotificationWorker.KEY_TITLE to "Upcoming task",
                ReminderNotificationWorker.KEY_MESSAGE to message,
                ReminderNotificationWorker.KEY_OPEN_DATE to reminder.date,
                ReminderNotificationWorker.KEY_NOTIFICATION_ID to notifId
            )

            val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tag(reminder.id)) // so we can cancel all offsets together
                .build()

            WorkManager.getInstance(appCtx).enqueueUniqueWork(
                uniqueName(reminder.id, off.minutes),
                ExistingWorkPolicy.REPLACE,
                request
            )

            scheduledAny = true
        }

        if (scheduledAny) {
            markScheduledIds(appCtx, getScheduledIds(appCtx) + reminder.id)
        } else {
            // all triggers were in the past -> cancel
            cancel(appCtx, reminder.id)
        }
    }

    fun cancel(context: Context, reminderId: String) {
        val appCtx = context.applicationContext

        // cancel all works related to this reminder (for all offsets)
        WorkManager.getInstance(appCtx).cancelAllWorkByTag(tag(reminderId))

        // remove from persisted set
        markScheduledIds(appCtx, getScheduledIds(appCtx) - reminderId)
    }

    /**
     * Call when getting list from Firestore (snapshot).
     * - recreates current ones
     * - cancels extra (deleted/offset/invalid)
     */
    fun syncFromFirestore(context: Context, reminders: List<Reminder>) {
        val appCtx = context.applicationContext

        val activeIds = mutableSetOf<String>()

        for (r in reminders) {
            val hasValidTime = parseDateTimeToMillis(r.date, r.time) != null
            val hasOffsets = collectOffsets(r).isNotEmpty()

            if (!r.isDone && hasValidTime && hasOffsets) {
                schedule(appCtx, r)     // schedule uses REPLACE and cancels old
                activeIds.add(r.id)
            } else {
                cancel(appCtx, r.id)
            }
        }

        // if something was planned earlier, but disappeared from the Firestore list
        val previously = getScheduledIds(appCtx)
        val removed = previously - reminders.map { it.id }.toSet()
        for (id in removed) cancel(appCtx, id)

        markScheduledIds(appCtx, activeIds)
    }

    // ---- INTERNALS ----

    private fun collectOffsets(reminder: Reminder): List<ReminderOffset> {
        val fromList = if (reminder.reminderOffsets.isNotEmpty()) {
            reminder.reminderOffsets.mapNotNull { name ->
                runCatching { ReminderOffset.valueOf(name) }.getOrNull()
            }
        } else {
            listOf(reminder.reminderOffset)
        }

        return fromList
            .filter { it.minutes > 0 }
            .distinctBy { it.minutes }
            .sortedBy { it.minutes }
    }

    private fun uniqueName(reminderId: String, offsetMin: Int) =
        "reminder_${reminderId}_$offsetMin"

    private fun tag(reminderId: String) =
        "reminder_tag_$reminderId"

    private fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long? {
        return try {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            format.parse("$dateStr $timeStr")?.time
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Deterministic notification id so "1 day" and "1 hour" do not overwrite each other.
     */
    private fun notificationId(reminderId: String, offsetMin: Int): Int {
        val raw = reminderId.hashCode() * 31 + offsetMin
        return abs(raw.coerceAtLeast(1))
    }

    private fun formatOffsetShort(off: ReminderOffset): String =
        when (off) {
            ReminderOffset.ONE_HOUR -> "in 1h"
            ReminderOffset.TWO_HOURS -> "in 2h"
            ReminderOffset.ONE_DAY -> "in 1d"
            ReminderOffset.TWO_DAYS -> "in 2d"
            ReminderOffset.ONE_WEEK -> "in 1w"
            else -> ""
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