package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    // Getting all user folders
    @Query("SELECT * FROM folders WHERE user_id = :userId ORDER BY sort_order ASC")
    fun getFoldersByUser(userId: String): Flow<List<FolderEntity>>

    // Getting a folder by ID
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: String): FolderEntity?

    // Updating the sort order
    @Query("UPDATE folders SET sort_order = :sortOrder WHERE id = :folderId")
    suspend fun updateFolderOrder(folderId: String, sortOrder: Int)
}