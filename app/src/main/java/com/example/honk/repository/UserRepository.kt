package com.example.honk.repository

import com.example.honk.data.dao.UserDao
import com.example.honk.data.entities.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {

    // Pass to DAO simple methods
    suspend fun createUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserById(userId: String): UserEntity? = userDao.getUserById(userId)

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun isEmailExists(email: String): Boolean = userDao.isEmailExists(email)

    // User registration with verification
    suspend fun registerUser(user: UserEntity): Result<Boolean> {
        return try {
            // Сheck if this email is already registered.
            if (userDao.isEmailExists(user.email)) {
                return Result.failure(Exception("User with this email already exists"))
            }

            // Create a user
            userDao.insertUser(user)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User Login
    suspend fun loginUser(email: String, password: String): Result<UserEntity> {
        return try {

            val user = userDao.getUserByEmail(email)
            when {
                user == null -> {
                    Result.failure(Exception("User with email '$email' not found"))
                }
                user.passwordHash == null -> {
                    Result.failure(Exception("This account uses Google Sign In"))
                }
                !checkPassword(password, user.passwordHash) -> {
                    Result.failure(Exception("Invalid password"))
                }
                else -> {
                    Result.success(user)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error logging in: ${e.message}"))
        }
    }

    // Change default sound
    suspend fun updateDefaultSound(userId: String, soundId: String): Result<Boolean> {
        return try {
            val user = userDao.getUserById(userId)
            user?.let {
                val updatedUser = it.copy(defaultSoundId = soundId)
                userDao.updateUser(updatedUser)
                Result.success(true)
            } ?: Result.failure(Exception("User Not Found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun checkPassword(inputPassword: String, storedHash: String?): Boolean {
        // TODO: REGISTRATION with password hashing
        return storedHash != null && inputPassword.isNotEmpty()
    }
}