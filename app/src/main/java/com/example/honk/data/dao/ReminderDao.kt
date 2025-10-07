package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    // Get all reminders for a task
    @Query("SELECT * FROM reminders WHERE task_id = :taskId")
    fun getRemindersByTask(taskId: String): Flow<List<ReminderEntity>>

    // Receive reminders by type
    @Query("SELECT * FROM reminders WHERE reminder_type = :reminderType")
    fun getRemindersByType(reminderType: String): Flow<List<ReminderEntity>>

    // Receive upcoming reminders (for the notification service)
    @Query("SELECT * FROM reminders WHERE scheduled_time > :currentTime AND is_triggered = 0")
    fun getUpcomingReminders(currentTime: Long): Flow<List<ReminderEntity>>

    // Mark the reminder as triggered
    @Query("UPDATE reminders SET is_triggered = 1, triggered_at = :triggeredAt WHERE id = :reminderId")
    suspend fun markAsTriggered(reminderId: String, triggeredAt: Long)
}