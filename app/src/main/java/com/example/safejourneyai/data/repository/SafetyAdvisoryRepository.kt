package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.local.entities.SafetyAdvisoryEntity
import com.example.safejourneyai.data.remote.FirestoreRepository
import com.example.safejourneyai.data.remote.FirestoreRepositoryImpl
import com.example.safejourneyai.data.sync.DataSyncManager
import com.example.safejourneyai.data.sync.DataSyncManagerImpl
import kotlinx.coroutines.flow.Flow

interface SafetyAdvisoryRepository {
    fun getSafetyAdvisories(userId: String): Flow<List<SafetyAdvisoryEntity>>
    suspend fun markAsRead(userId: String, advisoryId: String): Result<Unit>
    suspend fun syncSafetyAdvisories(userId: String): Result<Unit>
}

class SafetyAdvisoryRepositoryImpl(
    private val db: SafeJourneyDatabase,
    private val firestoreRepository: FirestoreRepository = FirestoreRepositoryImpl(),
    private val syncManager: DataSyncManager = DataSyncManagerImpl(db, firestoreRepository)
) : SafetyAdvisoryRepository {

    private val safetyAdvisoryDao = db.safetyAdvisoryDao()

    override fun getSafetyAdvisories(userId: String): Flow<List<SafetyAdvisoryEntity>> {
        return if (userId.isNotBlank()) {
            safetyAdvisoryDao.getAdvisoriesByUserId(userId)
        } else {
            safetyAdvisoryDao.getAllAdvisories()
        }
    }

    override suspend fun markAsRead(userId: String, advisoryId: String): Result<Unit> {
        safetyAdvisoryDao.markAsRead(advisoryId)
        val list = safetyAdvisoryDao.getAllAdvisoriesSync()
        val item = list.find { it.id == advisoryId }
        if (item != null && userId.isNotBlank()) {
            firestoreRepository.saveSafetyAdvisory(userId, item.copy(isRead = true, updatedAt = System.currentTimeMillis()))
        }
        return Result.success(Unit)
    }

    override suspend fun syncSafetyAdvisories(userId: String): Result<Unit> {
        return syncManager.syncSafetyAdvisories(userId)
    }
}
