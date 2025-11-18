package com.example.honk.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import com.google.firebase.firestore.PropertyName
import java.util.*

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["folder_id"]),
        Index(value = ["deadline"]),
        Index(value = ["status"]),
        Index(value = ["priority"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskEntity2(
    @PrimaryKey
    @get:PropertyName("id")
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "folder_id")
    @get:PropertyName("folderId")
    val folderId: String,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "start_time") val startTime: Long? = null,
    val deadline: Long? = null,
    val priority: String,
    val status: String,
    @ColumnInfo(name = "recurrence_pattern") val recurrencePattern: String? = null,
    val location: String? = null,
    @ColumnInfo(name = "is_important")
    @get:PropertyName("isImportant")
    val isImportant: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isSynced") val isSynced: Boolean = false
)