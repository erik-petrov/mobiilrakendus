package com.example.honk.repository

import com.example.honk.data.dao.FolderDao
import com.example.honk.data.dao.TaskDao
import com.example.honk.data.entities.FolderEntity
import kotlinx.coroutines.flow.Flow

class FolderRepository(
    private val folderDao: FolderDao,
    private val taskDao: TaskDao
) {

    suspend fun createFolder(folder: FolderEntity) = folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: FolderEntity) = folderDao.updateFolder(folder)

    suspend fun deleteFolder(folder: FolderEntity) = folderDao.deleteFolder(folder)

    fun getFoldersByUser(userId: String): Flow<List<FolderEntity>> =
        folderDao.getFoldersByUser(userId)

    // Deleting folder with all tasks
    suspend fun deleteFolderWithTasks(folderId: String): Result<Boolean> {
        return try {
            val folder = folderDao.getFolderById(folderId)
            folder?.let {
                folderDao.deleteFolder(it)
                Result.success(true)
            } ?: Result.failure(Exception("Folder Not Found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Moving tasks between folders
    suspend fun moveTasksToFolder(taskIds: List<String>, newFolderId: String): Result<Boolean> {
        return try {
            // TODO: Implement batch task folder update logic
            // For each task ID, update the folder_id to newFolderId
            // Consider using transaction for data consistency
            // Validate that newFolderId exists before proceeding
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}