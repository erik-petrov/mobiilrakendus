package com.example.honk.data.dao

import androidx.room.*
import com.example.honk.data.entities.GooseSoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GooseSoundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: GooseSoundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sounds: List<GooseSoundEntity>)

    // Getting all sounds
    @Query("SELECT * FROM goose_sounds ORDER BY name ASC")
    fun getAllSounds(): Flow<List<GooseSoundEntity>>

    // Getting sound by ID
    @Query("SELECT * FROM goose_sounds WHERE id = :soundId")
    suspend fun getSoundById(soundId: String): GooseSoundEntity?

    // Getting sound by key
    @Query("SELECT * FROM goose_sounds WHERE sound_key = :soundKey")
    suspend fun getSoundByKey(soundKey: String): GooseSoundEntity?
}