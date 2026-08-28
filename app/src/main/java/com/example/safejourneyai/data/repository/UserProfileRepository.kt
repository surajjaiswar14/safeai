package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.local.entities.UserProfileEntity
import com.example.safejourneyai.data.remote.FirestoreRepository
import com.example.safejourneyai.data.remote.FirestoreRepositoryImpl
import com.example.safejourneyai.data.sync.DataSyncManager
import com.example.safejourneyai.data.sync.DataSyncManagerImpl
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>
    suspend fun updateUserProfile(profile: UserProfileEntity): Result<Unit>
    suspend fun syncUserProfile(userId: String): Result<Unit>
}

class UserProfileRepositoryImpl(
    private val db: SafeJourneyDatabase,
    private val firestoreRepository: FirestoreRepository = FirestoreRepositoryImpl(),
    private val syncManager: DataSyncManager = DataSyncManagerImpl(db, firestoreRepository)
) : UserProfileRepository {

    private val userProfileDao = db.userProfileDao()

    override fun getUserProfile(userId: String): Flow<UserProfileEntity?> {
        return if (userId.isNotBlank()) {
            userProfileDao.getProfileByUserId(userId)
        } else {
            userProfileDao.getProfile()
        }
    }

    override suspend fun updateUserProfile(profile: UserProfileEntity): Result<Unit> {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        userProfileDao.createProfile(updated)

        val userId = updated.userId.ifEmpty { updated.id }
        if (userId.isNotBlank() && userId != "current_user" && userId != "guest_user") {
            firestoreRepository.saveUserProfile(updated)
        }
        return Result.success(Unit)
    }

    override suspend fun syncUserProfile(userId: String): Result<Unit> {
        return syncManager.syncUserProfile(userId)
    }
}
