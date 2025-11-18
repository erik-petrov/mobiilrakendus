package com.example.honk.model
import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    var date: String = "",
    var time: String = "",
    var text: String = "",
    var category: String = "",
    var isDone: Boolean = false,
    var priority: String = "Medium"
)

