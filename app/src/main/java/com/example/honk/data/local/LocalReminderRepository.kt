package com.example.honk.local

import androidx.lifecycle.MutableLiveData
import com.example.honk.model.Reminder

object LocalReminderRepository {

    private val internal = MutableLiveData<MutableList<Reminder>>(mutableListOf())

    val reminders get() = internal

    fun add(reminder: Reminder) {
        val list = internal.value ?: mutableListOf()
        list.add(reminder)
        internal.value = list
    }

    fun delete(reminder: Reminder) {
        deleteById(reminder.id)
    }

    fun deleteById(id: String) {
        val list = internal.value ?: return
        val idx = list.indexOfFirst { it.id == id }
        if (idx != -1) {
            list.removeAt(idx)
            internal.value = list
        }
    }

    fun update(updated: Reminder) {
        val list = internal.value ?: return
        val idx = list.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            list[idx] = updated
            internal.value = list
        }
    }

    fun notifyChanged() {
        internal.value = internal.value
    }
}
