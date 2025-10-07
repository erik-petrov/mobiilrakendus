package com.example.honk.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo
import java.util.*

@Entity(
    tableName = "goose_sounds",
    indices = [Index(value = ["sound_key"], unique = true)]
)
data class GooseSoundEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    @ColumnInfo(name = "sound_key") val soundKey: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)