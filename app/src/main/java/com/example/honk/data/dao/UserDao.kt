package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // User creation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Getting a user by ID
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    // Getting a user by email
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Getting a user by Google ID
    @Query("SELECT * FROM users WHERE google_id = :googleId")
    suspend fun getUserByGoogleId(googleId: String): UserEntity?

    // Getting all users (for admin)
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    // User update
    @Update
    suspend fun updateUser(user: UserEntity)

    // User delite
    @Delete
    suspend fun deleteUser(user: UserEntity)

    // Checking the existence of an email
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Boolean
}