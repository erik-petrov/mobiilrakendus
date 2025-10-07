package com.example.honk.repository

import com.example.honk.data.dao.TaskDao
import com.example.honk.data.dao.FolderDao
import com.example.honk.data.dao.ReminderDao
import com.example.honk.data.entities.TaskEntity
import com.example.honk.data.entities.ReminderEntity
import com.example.honk.data.enums.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository(
    private val taskDao: TaskDao,
    private val folderDao: FolderDao,
    private val reminderDao: ReminderDao
) {

    // Simple Methods
    suspend fun createTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    fun getTasksByFolder(folderId: String): Flow<List<TaskEntity>> =
        taskDao.getTasksByFolder(folderId)


    // Creating a task with a reminder
    suspend fun createTaskWithReminder(
        task: TaskEntity,
        reminderTime: Long?,
        soundId: String
    ): Result<Boolean> {
        return try {

            taskDao.insertTask(task)

            reminderTime?.let { time ->
                val reminder = ReminderEntity(
                    taskId = task.id,
                    scheduledTime = time,
                    soundId = soundId,
                    reminderType = "time_based"
                )
                reminderDao.insertReminder(reminder)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Getting tasks with folder information
    // TODO later
    // suspend fun getTasksWithFolderInfo(folderId: String): Flow<List<TaskWithFolder>> {
    //     return taskDao.getTasksByFolder(folderId)
    // }

    // Mark the task as completed
    suspend fun completeTask(taskId: String): Result<Boolean> {
        return try {
            taskDao.updateTaskStatus(taskId, TaskStatus.COMPLETED.name)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Getting upcoming tasks (with deadlines)
    fun getUpcomingTasks(): Flow<List<TaskEntity>> =
        taskDao.getTasksWithDeadline(System.currentTimeMillis())

    // Search tasks by text
    fun searchTasks(query: String): Flow<List<TaskEntity>> =
        taskDao.searchTasks(query)

    // Getting tasks statistics
    suspend fun getTaskStats(folderId: String): TaskStats {
        val allTasks = taskDao.getTasksByFolder(folderId).first()

        return TaskStats(
            total = allTasks.size,
            completed = allTasks.count { it.status == TaskStatus.COMPLETED.name },
            important = allTasks.count { it.isImportant },
            overdue = allTasks.count { it.deadline != null && it.deadline < System.currentTimeMillis() }
        )
    }

    data class TaskStats(
        val total: Int,
        val completed: Int,
        val important: Int,
        val overdue: Int
    )
}