package com.example.safejourneyai.data.local

import androidx.room.*
import com.example.safejourneyai.data.local.entities.OfflinePackEntity
import com.example.safejourneyai.data.local.entities.SavedDestinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {

    @Query("SELECT * FROM saved_destinations")
    fun getAllSavedDestinations(): Flow<List<SavedDestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedDestination(saved: SavedDestinationEntity)

    @Query("DELETE FROM saved_destinations WHERE destinationId = :id")
    suspend fun deleteSavedDestination(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_destinations WHERE destinationId = :id)")
    suspend fun isDestinationSaved(id: String): Boolean

    @Query("SELECT * FROM offline_packs")
    fun getAllOfflinePacks(): Flow<List<OfflinePackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflinePack(pack: OfflinePackEntity)

    @Query("DELETE FROM offline_packs WHERE destinationId = :id")
    suspend fun deleteOfflinePack(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM offline_packs WHERE destinationId = :id)")
    suspend fun isOfflinePackDownloaded(id: String): Boolean
}
