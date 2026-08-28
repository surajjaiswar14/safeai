package com.example.safejourneyai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.safejourneyai.data.local.dao.*
import com.example.safejourneyai.data.local.entities.*

@Database(
    entities = [
        DestinationEntity::class,
        SavedDestinationEntity::class,
        OfflinePackEntity::class,
        EmergencyContactEntity::class,
        EmergencyServiceEntity::class,
        ChecklistItemEntity::class,
        UserProfileEntity::class,
        SafetyAdvisoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun destinationDao(): DestinationDao
    abstract fun savedDestinationDao(): SavedDestinationDao
    abstract fun offlinePackDao(): OfflinePackDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun emergencyServiceDao(): EmergencyServiceDao
    abstract fun safetyChecklistDao(): SafetyChecklistDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun safetyAdvisoryDao(): SafetyAdvisoryDao
    fun advisoryDao(): SafetyAdvisoryDao = safetyAdvisoryDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safejourney.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

typealias SafeJourneyDatabase = AppDatabase
