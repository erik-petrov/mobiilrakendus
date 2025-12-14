package com.example.honk.model

import com.example.honk.data.entities.ReminderEntity
import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    var date: String = "",
    var time: String = "",
    var text: String = "",
    var category: String = "",
    var isDone: Boolean = false,
    var priority: String = "Medium",

    // Unique id for this reminder (used for notifications)
    var notificationId: Long = System.currentTimeMillis(),
    var reminderOffset: ReminderOffset = ReminderOffset.NONE,

    var imageUri: String? = null
)
