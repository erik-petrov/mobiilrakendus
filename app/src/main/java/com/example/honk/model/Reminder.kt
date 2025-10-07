package com.example.honk.model

data class Reminder(
    val date: String,
    var time: String = "",
    var text: String = "",
    var category: String = "",
    var isDone: Boolean = false,
    var priority: String = "Medium"
)
