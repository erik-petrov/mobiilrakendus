package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.LocationTriggerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationTriggerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationTrigger(trigger: LocationTriggerEntity)

    @Update
    suspend fun updateLocationTrigger(trigger: LocationTriggerEntity)

    @Delete
    suspend fun deleteLocationTrigger(trigger: LocationTriggerEntity)

    // Getting reminder triggers
    @Query("SELECT * FROM location_triggers WHERE reminder_id = :reminderId")
    fun getTriggersByReminder(reminderId: String): Flow<List<LocationTriggerEntity>>

    // Getting active geo-triggers (for the location service)
    @Query("SELECT * FROM location_triggers WHERE is_active = 1")
    fun getActiveLocationTriggers(): Flow<List<LocationTriggerEntity>>

    // Getting a trigger by ID
    @Query("SELECT * FROM location_triggers WHERE id = :triggerId")
    suspend fun getTriggerById(triggerId: String): LocationTriggerEntity?

    // Trigger activation/deactivation
    @Query("UPDATE location_triggers SET is_active = :isActive WHERE id = :triggerId")
    suspend fun setTriggerActive(triggerId: String, isActive: Boolean)

    // Search for triggers by location (for checking in radius)
    @Query("""
        SELECT * FROM location_triggers 
        WHERE is_active = 1 
        AND (latitude - :userLat) * (latitude - :userLat) + 
            (longitude - :userLng) * (longitude - :userLng) <= (:radius * :radius)
    """)
    suspend fun getTriggersInRadius(userLat: Double, userLng: Double, radius: Double): List<LocationTriggerEntity>
}