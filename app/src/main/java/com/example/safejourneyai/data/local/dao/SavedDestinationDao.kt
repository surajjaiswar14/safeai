package com.example.safejourneyai.data.local.dao

import androidx.room.*
import com.example.safejourneyai.data.local.entities.SavedDestinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDestinationDao {
    @Query("SELECT * FROM saved_destinations ORDER BY createdAt DESC")
    fun getAllSavedDestinations(): Flow<List<SavedDestinationEntity>>

    @Query("SELECT * FROM saved_destinations ORDER BY createdAt DESC")
    suspend fun getAllSavedDestinationsSync(): List<SavedDestinationEntity>

    @Query("SELECT * FROM saved_destinations WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSavedDestinationsByUserId(userId: String): Flow<List<SavedDestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDestination(saved: SavedDestinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedDestination(saved: SavedDestinationEntity)

    @Update
    suspend fun updateSavedDestination(saved: SavedDestinationEntity)

    @Query("DELETE FROM saved_destinations WHERE destinationId = :id")
    suspend fun unsaveDestination(id: String)

    @Delete
    suspend fun deleteSavedDestinationEntity(saved: SavedDestinationEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_destinations WHERE destinationId = :id)")
    suspend fun isDestinationSaved(id: String): Boolean
}
