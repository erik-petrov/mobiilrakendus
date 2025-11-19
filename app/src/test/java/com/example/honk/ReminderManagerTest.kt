package com.example.honk

import com.example.honk.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReminderManagerTest {

    private lateinit var manager: ReminderManager

    @Before
    fun setup() {
        manager = ReminderManager()
    }

    @Test
    fun addReminder_addsReminderToList() {
        val r = Reminder(date = "2025-01-20", text = "Test reminder")
        manager.addReminder(r)

        assertEquals(1, manager.getReminders().size)
        assertEquals("Test reminder", manager.getReminders()[0].text)
    }

    @Test
    fun deleteReminder_removesReminderFromList() {
        val r = Reminder(date = "2025-01-20", text = "Delete me")
        manager.addReminder(r)

        manager.deleteReminder(r)

        assertEquals(0, manager.getReminders().size)
    }
}
