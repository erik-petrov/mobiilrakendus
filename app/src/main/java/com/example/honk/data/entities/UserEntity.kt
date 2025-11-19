package com.example.honk.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo
import java.util.*

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["google_id"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    @ColumnInfo(name = "auth_provider") val authProvider: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String? = null,
    @ColumnInfo(name = "google_id") val googleId: String? = null,
    @ColumnInfo(name = "storage_preference") val storagePreference: String,
    @ColumnInfo(name = "default_sound_id") val defaultSoundId: String,
    @ColumnInfo(name = "dnd_enabled") val dndEnabled: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
){
    constructor() : this(
        username = "",
        email = "",
        authProvider = "",
        storagePreference = "",
        defaultSoundId = ""

)
}