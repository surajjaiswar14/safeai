package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.local.entities.EmergencyContactEntity
import com.example.safejourneyai.data.remote.FirestoreRepository
import com.example.safejourneyai.data.remote.FirestoreRepositoryImpl
import com.example.safejourneyai.data.sync.DataSyncManager
import com.example.safejourneyai.data.sync.DataSyncManagerImpl
import kotlinx.coroutines.flow.Flow

interface EmergencyContactRepository {
    fun getEmergencyContacts(userId: String): Flow<List<EmergencyContactEntity>>
    suspend fun addEmergencyContact(userId: String, contact: EmergencyContactEntity): Result<Unit>
    suspend fun updateEmergencyContact(userId: String, contact: EmergencyContactEntity): Result<Unit>
    suspend fun deleteEmergencyContact(userId: String, contactId: Long): Result<Unit>
    suspend fun syncEmergencyContacts(userId: String): Result<Unit>
}

class EmergencyContactRepositoryImpl(
    private val db: SafeJourneyDatabase,
    private val firestoreRepository: FirestoreRepository = FirestoreRepositoryImpl(),
    private val syncManager: DataSyncManager = DataSyncManagerImpl(db, firestoreRepository)
) : EmergencyContactRepository {

    private val emergencyContactDao = db.emergencyContactDao()

    override fun getEmergencyContacts(userId: String): Flow<List<EmergencyContactEntity>> {
        return if (userId.isNotBlank()) {
            emergencyContactDao.getContactsByUserId(userId)
        } else {
            emergencyContactDao.getAllContacts()
        }
    }

    override suspend fun addEmergencyContact(userId: String, contact: EmergencyContactEntity): Result<Unit> {
        val entity = contact.copy(
            userId = userId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        emergencyContactDao.insertContact(entity)
        if (userId.isNotBlank()) {
            firestoreRepository.saveEmergencyContact(userId, entity)
        }
        return Result.success(Unit)
    }

    override suspend fun updateEmergencyContact(userId: String, contact: EmergencyContactEntity): Result<Unit> {
        val entity = contact.copy(
            userId = userId,
            updatedAt = System.currentTimeMillis()
        )
        emergencyContactDao.updateContact(entity)
        if (userId.isNotBlank()) {
            firestoreRepository.saveEmergencyContact(userId, entity)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteEmergencyContact(userId: String, contactId: Long): Result<Unit> {
        emergencyContactDao.deleteContact(contactId)
        if (userId.isNotBlank()) {
            firestoreRepository.deleteEmergencyContact(userId, contactId)
        }
        return Result.success(Unit)
    }

    override suspend fun syncEmergencyContacts(userId: String): Result<Unit> {
        return syncManager.syncEmergencyContacts(userId)
    }
}
