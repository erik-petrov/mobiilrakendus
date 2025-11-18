package com.example.honk

import com.example.honk.model.Reminder

class ReminderManager {

    private val reminders = mutableListOf<Reminder>()

    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)
    }

    fun deleteReminder(reminder: Reminder) {
        reminders.remove(reminder)
    }

    fun getReminders(): List<Reminder> = reminders
}
