package com.example.honk.ui.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.honk.model.Reminder
import com.example.honk.repository.ReminderRepositoryTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel : ViewModel() {
    val repository = ReminderRepositoryTest()

    val reminders: StateFlow<List<Reminder>> =
        repository.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(reminder: Reminder){
        viewModelScope.launch(Dispatchers.IO){
            repository.add(reminder.id, reminder)
        }
    }

    fun getAll() = repository.getAll()

    fun delete(reminderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(reminderId)
        }
    }
}