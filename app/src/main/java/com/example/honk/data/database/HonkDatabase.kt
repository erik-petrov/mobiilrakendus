package com.example.honk.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.honk.data.entities.*
import com.example.honk.data.dao.*

@Database(
    entities = [
        UserEntity::class,
        FolderEntity::class,
        TaskEntity::class,
        GooseSoundEntity::class,
        ReminderEntity::class,
        LocationTriggerEntity::class,
        CustomReminderTemplateEntity::class,
        DndScheduleEntity::class,
        TaskAttachmentEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(HonkTypeConverters::class)
abstract class HonkDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun folderDao(): FolderDao
    abstract fun taskDao(): TaskDao
    abstract fun gooseSoundDao(): GooseSoundDao
    abstract fun reminderDao(): ReminderDao
    abstract fun locationTriggerDao(): LocationTriggerDao
    abstract fun customReminderTemplateDao(): CustomReminderTemplateDao
    abstract fun dndScheduleDao(): DndScheduleDao
    abstract fun taskAttachmentDao(): TaskAttachmentDao

    companion object {
        @Volatile
        private var INSTANCE: HonkDatabase? = null

        fun getDatabase(context: Context): HonkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HonkDatabase::class.java,
                    "honk_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}