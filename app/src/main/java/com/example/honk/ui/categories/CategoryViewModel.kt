package com.example.honk.ui.categories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.honk.model.Reminder
import com.example.honk.repository.ReminderRepositoryTest

data class Category(
    var name: String,
    var color: Int,
    var reminders: MutableList<Reminder> = mutableListOf()
)

class CategoryViewModel : ViewModel() {
    val categories = MutableLiveData<MutableList<Category>>(mutableListOf())

    fun renameCategory(index: Int, newName: String) {
        categories.value?.let {
            if (index in it.indices) {
                it[index].name = newName
                categories.value = it // trigger observers
            }
        }
    }

    fun updateCategory(index: Int, newName: String, newColor: Int) {
        categories.value?.let {
            if (index in it.indices) {
                val category = it[index]
                category.name = newName
                category.color = newColor
                categories.value = it // triggers LiveData observers
            }
        }
    }


    fun deleteCategory(index: Int) {
        categories.value?.let {
            if (index in it.indices) {
                it.removeAt(index)
                categories.value = it
            }
        }
    }

    fun addCategory(category: Category) {
        categories.value?.let {
            it.add(category)
            categories.value = it
        }
    }

    fun addReminderToCategory(categoryIndex: Int, reminder: Reminder) {
        categories.value?.let {
            if (categoryIndex in it.indices) {
                it[categoryIndex].reminders.add(reminder)
                categories.value = it // triggers LiveData observers
            }
        }
    }

}
