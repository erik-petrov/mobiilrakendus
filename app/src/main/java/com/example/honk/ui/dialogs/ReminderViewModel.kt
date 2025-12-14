package com.example.honk.ui.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.honk.model.Reminder
import com.example.honk.repository.ReminderRepositoryTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderViewModel : ViewModel() {
    val repository = ReminderRepositoryTest()

    fun add(reminder: Reminder){
        viewModelScope.launch(Dispatchers.IO){
            repository.add(reminder.id, reminder)
        }
    }

    fun getAll() = repository.getAll()
}