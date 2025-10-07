package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.TaskAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskAttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: TaskAttachmentEntity)

    @Update
    suspend fun updateAttachment(attachment: TaskAttachmentEntity)

    @Delete
    suspend fun deleteAttachment(attachment: TaskAttachmentEntity)

    // Getting task attachments
    @Query("SELECT * FROM task_attachments WHERE task_id = :taskId")
    fun getAttachmentsByTask(taskId: String): Flow<List<TaskAttachmentEntity>>

    // Getting an attachment by ID
    @Query("SELECT * FROM task_attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: String): TaskAttachmentEntity?

    // Getting attachments by file type
    @Query("SELECT * FROM task_attachments WHERE task_id = :taskId AND file_type = :fileType")
    fun getAttachmentsByType(taskId: String, fileType: String): Flow<List<TaskAttachmentEntity>>

    // Getting the number of attachments for a task
    @Query("SELECT COUNT(*) FROM task_attachments WHERE task_id = :taskId")
    suspend fun getAttachmentCount(taskId: String): Int

    // Getting attachments created by the camera
    @Query("SELECT * FROM task_attachments WHERE source_type = 'camera'")
    fun getCameraAttachments(): Flow<List<TaskAttachmentEntity>>

    // Updating the thumbnail path
    @Query("UPDATE task_attachments SET thumbnail_path = :thumbnailPath WHERE id = :attachmentId")
    suspend fun updateThumbnailPath(attachmentId: String, thumbnailPath: String)
}