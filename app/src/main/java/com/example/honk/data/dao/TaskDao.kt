package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Getting all tasks in a folder
    @Query("SELECT * FROM tasks WHERE folder_id = :folderId ORDER BY created_at DESC")
    fun getTasksByFolder(folderId: String): Flow<List<TaskEntity>>

    // Getting a task by ID
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    // Getting tasks by status
    @Query("SELECT * FROM tasks WHERE folder_id = :folderId AND status = :status")
    fun getTasksByStatus(folderId: String, status: String): Flow<List<TaskEntity>>

    // Getting important tasks
    @Query("SELECT * FROM tasks WHERE folder_id = :folderId AND is_important = 1")
    fun getImportantTasks(folderId: String): Flow<List<TaskEntity>>

    // Receiving tasks with deadlines (for reminders)
    @Query("SELECT * FROM tasks WHERE deadline IS NOT NULL AND deadline > :currentTime")
    fun getTasksWithDeadline(currentTime: Long): Flow<List<TaskEntity>>

    // Update task status
    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: String)

    // Search for tasks
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTasks(query: String): Flow<List<TaskEntity>>
}