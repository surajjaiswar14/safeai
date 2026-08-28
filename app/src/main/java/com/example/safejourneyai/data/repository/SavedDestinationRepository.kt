package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.local.entities.SavedDestinationEntity
import com.example.safejourneyai.data.remote.FirestoreRepository
import com.example.safejourneyai.data.remote.FirestoreRepositoryImpl
import com.example.safejourneyai.data.sync.DataSyncManager
import com.example.safejourneyai.data.sync.DataSyncManagerImpl
import kotlinx.coroutines.flow.Flow

interface SavedDestinationRepository {
    fun getSavedDestinations(userId: String): Flow<List<SavedDestinationEntity>>
    suspend fun toggleSaveDestination(userId: String, destinationId: String): Boolean
    suspend fun syncSavedDestinations(userId: String): Result<Unit>
}

class SavedDestinationRepositoryImpl(
    private val db: SafeJourneyDatabase,
    private val firestoreRepository: FirestoreRepository = FirestoreRepositoryImpl(),
    private val syncManager: DataSyncManager = DataSyncManagerImpl(db, firestoreRepository)
) : SavedDestinationRepository {

    private val savedDestinationDao = db.savedDestinationDao()

    override fun getSavedDestinations(userId: String): Flow<List<SavedDestinationEntity>> {
        return if (userId.isNotBlank()) {
            savedDestinationDao.getSavedDestinationsByUserId(userId)
        } else {
            savedDestinationDao.getAllSavedDestinations()
        }
    }

    override suspend fun toggleSaveDestination(userId: String, destinationId: String): Boolean {
        val isSaved = savedDestinationDao.isDestinationSaved(destinationId)
        if (isSaved) {
            savedDestinationDao.unsaveDestination(destinationId)
            if (userId.isNotBlank()) {
                firestoreRepository.deleteSavedDestination(userId, destinationId)
            }
            return false
        } else {
            val entity = SavedDestinationEntity(
                destinationId = destinationId,
                savedAt = System.currentTimeMillis(),
                userId = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            savedDestinationDao.saveDestination(entity)
            if (userId.isNotBlank()) {
                firestoreRepository.saveSavedDestination(userId, entity)
            }
            return true
        }
    }

    override suspend fun syncSavedDestinations(userId: String): Result<Unit> {
        return syncManager.syncSavedDestinations(userId)
    }
}
