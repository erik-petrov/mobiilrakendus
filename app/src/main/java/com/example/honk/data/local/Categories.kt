package com.example.honk.data.local

object Categories {
    private val defaultCategories = listOf(
        "No category",
        "WORK",
        "SCHOOL",
        "PETS",
        "HOME",
        "HOLIDAY"
    )

    fun getAll(): List<String> = defaultCategories
}
