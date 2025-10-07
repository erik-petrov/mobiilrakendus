package com.example.honk.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import java.util.*

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["task_id"]),
        Index(value = ["scheduled_time"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GooseSoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["sound_id"]
        )
    ]
)
data class ReminderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "reminder_type") val reminderType: String,
    @ColumnInfo(name = "scheduled_time") val scheduledTime: Long? = null,
    @ColumnInfo(name = "sound_id") val soundId: String,
    val volume: Int = 80,
    @ColumnInfo(name = "vibration_pattern") val vibrationPattern: String? = null,
    @ColumnInfo(name = "is_triggered") val isTriggered: Boolean = false,
    @ColumnInfo(name = "triggered_at") val triggeredAt: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)