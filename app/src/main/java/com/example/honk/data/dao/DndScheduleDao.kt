package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.DndScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DndScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDndSchedule(schedule: DndScheduleEntity)

    @Update
    suspend fun updateDndSchedule(schedule: DndScheduleEntity)

    @Delete
    suspend fun deleteDndSchedule(schedule: DndScheduleEntity)

    // Getting user DND schedules
    @Query("SELECT * FROM dnd_schedules WHERE user_id = :userId")
    fun getDndSchedulesByUser(userId: String): Flow<List<DndScheduleEntity>>

    // Getting active DND schedules
    @Query("SELECT * FROM dnd_schedules WHERE is_active = 1")
    fun getActiveDndSchedules(): Flow<List<DndScheduleEntity>>

    // Getting current active DND periods
    @Query("SELECT * FROM dnd_schedules WHERE is_active = 1 AND :currentTime BETWEEN start_time AND end_time")
    suspend fun getCurrentDndSchedules(currentTime: Long): List<DndScheduleEntity>

    // Getting a schedule by ID
    @Query("SELECT * FROM dnd_schedules WHERE id = :scheduleId")
    suspend fun getDndScheduleById(scheduleId: String): DndScheduleEntity?

    // Activating/deactivating a schedule
    @Query("UPDATE dnd_schedules SET is_active = :isActive WHERE id = :scheduleId")
    suspend fun setDndScheduleActive(scheduleId: String, isActive: Boolean)

    // Getting recurring schedules
    @Query("SELECT * FROM dnd_schedules WHERE is_recurring = 1 AND is_active = 1")
    fun getRecurringDndSchedules(): Flow<List<DndScheduleEntity>>
}