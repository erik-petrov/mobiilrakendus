package com.example.honk.model

//how long before the task app show a notification
enum class ReminderOffset(val minutes: Int) {
    NONE(0),
    ONE_HOUR(60),
    TWO_HOURS(120),
    ONE_DAY(60 * 24),
    TWO_DAYS(60 * 24 * 2),
    ONE_WEEK(60 * 24 * 7)
}