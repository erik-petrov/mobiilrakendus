package com.example.honk.model

data class Reminder(
    var date: String,
    var time: String = "",
    var text: String = "",
    var category: String = "",
    var isDone: Boolean = false,
    var priority: String = "Medium",
    // Unique id for this reminder (used for notifications)
    var id: Long = System.currentTimeMillis(),
    var reminderOffset: ReminderOffset = ReminderOffset.NONE
)
