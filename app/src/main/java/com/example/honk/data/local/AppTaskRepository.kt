package com.example.honk.data.local

import androidx.lifecycle.MutableLiveData
import com.example.honk.model.Reminder

object AppTaskRepository {

    // Central list of ALL reminders in the app
    val tasks = MutableLiveData<MutableList<Reminder>>(mutableListOf())

    fun addTask(task: Reminder) {
        val list = tasks.value ?: mutableListOf()
        list.add(task)
        tasks.value = list
    }

    fun updateTask(updated: Reminder) {
        val list = tasks.value ?: return
        val index = list.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            list[index] = updated
            tasks.value = list
        }
    }

    fun deleteTask(task: Reminder) {
        val list = tasks.value ?: return
        list.removeAll { it.id == task.id }
        tasks.value = list
    }
}
