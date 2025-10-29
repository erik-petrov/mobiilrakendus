package com.example.honk.model

data class Reminder(
    var date: String,
    var time: String = "",
    var text: String = "",
    var category: String = "",
    var isDone: Boolean = false,
    var priority: String = "Medium"
)
