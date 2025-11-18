package com.example.honk.data

import androidx.lifecycle.MutableLiveData
import com.example.honk.model.Reminder

object TaskRepository {

    // internal mutable list
    private val internalTasks = MutableLiveData<MutableList<Reminder>>(mutableListOf())

    // public LiveData
    val tasks get() = internalTasks

    fun addTask(task: Reminder) {
        val list = internalTasks.value ?: mutableListOf()
        list.add(task)
        internalTasks.value = list
    }

    // old index-based delete kept for compatibility (if used somewhere else)
    fun deleteTask(index: Int) {
        val list = internalTasks.value ?: return
        if (index in list.indices) {
            list.removeAt(index)
            internalTasks.value = list
        }
    }

    // new: delete by Reminder object
    fun deleteTask(task: Reminder) {
        deleteTaskById(task.id)
    }

    // new: delete by id
    fun deleteTaskById(id: String) {
        val list = internalTasks.value ?: return
        val idx = list.indexOfFirst { it.id == id }
        if (idx != -1) {
            list.removeAt(idx)
            internalTasks.value = list
        }
    }

    // new: update task by id
    fun updateTask(updated: Reminder) {
        val list = internalTasks.value ?: return
        val idx = list.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            list[idx] = updated
            internalTasks.value = list
        }
    }

    // helper to force notify observers if you manually mutate list
    fun notifyChanged() {
        internalTasks.value = internalTasks.value
    }
}
