package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.CustomReminderTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomReminderTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CustomReminderTemplateEntity)

    @Update
    suspend fun updateTemplate(template: CustomReminderTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: CustomReminderTemplateEntity)

    // Getting user templates
    @Query("SELECT * FROM custom_reminder_templates WHERE user_id = :userId")
    fun getTemplatesByUser(userId: String): Flow<List<CustomReminderTemplateEntity>>

    // Getting a template by ID
    @Query("SELECT * FROM custom_reminder_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: String): CustomReminderTemplateEntity?

    // Getting the default template for a user
    @Query("SELECT * FROM custom_reminder_templates WHERE user_id = :userId AND is_default = 1")
    suspend fun getDefaultTemplate(userId: String): CustomReminderTemplateEntity?

    // Setting the default template
    @Query("UPDATE custom_reminder_templates SET is_default = 0 WHERE user_id = :userId")
    suspend fun clearDefaultTemplates(userId: String)

    @Query("UPDATE custom_reminder_templates SET is_default = 1 WHERE id = :templateId")
    suspend fun setTemplateAsDefault(templateId: String)

    // Search for templates by event type
    @Query("SELECT * FROM custom_reminder_templates WHERE user_id = :userId AND event_type = :eventType")
    fun getTemplatesByEventType(userId: String, eventType: String): Flow<List<CustomReminderTemplateEntity>>
}