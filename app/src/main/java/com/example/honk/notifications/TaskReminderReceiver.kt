package com.example.honk.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: "unknown"
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task reminder"
        val message = intent.getStringExtra(EXTRA_TASK_MESSAGE) ?: ""

        NotificationHelper.showSimpleNotification(
            context = context,
            title = title,
            message = if (message.isNotBlank()) message else "Don't forget this task."
        )
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_MESSAGE = "extra_task_message"
    }
}
