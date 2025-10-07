package com.example.honk.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import java.util.*

@Entity(
    tableName = "custom_reminder_templates",
    indices = [Index(value = ["user_id"])],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GooseSoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["sound_id"]
        )
    ]
)
data class CustomReminderTemplateEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    val offsets: String,
    @ColumnInfo(name = "sound_id") val soundId: String,
    @ColumnInfo(name = "event_type") val eventType: String? = null,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)