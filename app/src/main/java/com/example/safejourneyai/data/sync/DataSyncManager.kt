package com.example.safejourneyai.data.sync

import android.util.Log
import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.local.entities.EmergencyContactEntity
import com.example.safejourneyai.data.local.entities.SafetyAdvisoryEntity
import com.example.safejourneyai.data.local.entities.SavedDestinationEntity
import com.example.safejourneyai.data.local.entities.UserProfileEntity
import com.example.safejourneyai.data.remote.FirestoreRepository
import com.example.safejourneyai.data.remote.FirestoreRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DataSyncManager {
    suspend fun syncAllUserData(userId: String): Result<Unit>
    suspend fun syncUserProfile(userId: String): Result<Unit>
    suspend fun syncEmergencyContacts(userId: String): Result<Unit>
    suspend fun syncSavedDestinations(userId: String): Result<Unit>
    suspend fun syncSafetyAdvisories(userId: String): Result<Unit>
}

class DataSyncManagerImpl(
    private val db: SafeJourneyDatabase,
    private val firestoreRepository: FirestoreRepository = FirestoreRepositoryImpl()
) : DataSyncManager {

    private val TAG = "DataSyncManager"

    private val userProfileDao = db.userProfileDao()
    private val emergencyContactDao = db.emergencyContactDao()
    private val savedDestinationDao = db.savedDestinationDao()
    private val safetyAdvisoryDao = db.safetyAdvisoryDao()

    override suspend fun syncAllUserData(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Cannot sync with empty userId"))
        }

        val profileRes = syncUserProfile(userId)
        val contactsRes = syncEmergencyContacts(userId)
        val savedRes = syncSavedDestinations(userId)
        val advisoriesRes = syncSafetyAdvisories(userId)

        if (profileRes.isSuccess && contactsRes.isSuccess && savedRes.isSuccess && advisoriesRes.isSuccess) {
            Result.success(Unit)
        } else {
            val errors = listOfNotNull(
                profileRes.exceptionOrNull()?.message,
                contactsRes.exceptionOrNull()?.message,
                savedRes.exceptionOrNull()?.message,
                advisoriesRes.exceptionOrNull()?.message
            ).joinToString("; ")
            Log.w(TAG, "Partial sync failure for $userId: $errors")
            Result.failure(Exception("Sync completed with errors: $errors"))
        }
    }

    override suspend fun syncUserProfile(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteResult = firestoreRepository.fetchUserProfile(userId)
            if (remoteResult.isFailure) {
                return@withContext Result.failure(remoteResult.exceptionOrNull() ?: Exception("Fetch user profile failed"))
            }
            val remoteProfile = remoteResult.getOrNull()
            val localProfile = userProfileDao.getProfileSync()

            if (remoteProfile != null) {
                if (localProfile != null && localProfile.updatedAt > remoteProfile.updatedAt) {
                    // Local is newer: push to Firestore
                    firestoreRepository.saveUserProfile(localProfile.copy(userId = userId))
                } else {
                    // Remote is newer or initial: update Room
                    userProfileDao.createProfile(remoteProfile.copy(userId = userId))
                }
            } else if (localProfile != null) {
                // No remote record yet: upload local to Firestore
                firestoreRepository.saveUserProfile(localProfile.copy(userId = userId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "syncUserProfile failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun syncEmergencyContacts(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteResult = firestoreRepository.fetchEmergencyContacts(userId)
            if (remoteResult.isFailure) {
                return@withContext Result.failure(remoteResult.exceptionOrNull() ?: Exception("Fetch emergency contacts failed"))
            }
            val remoteContacts = remoteResult.getOrDefault(emptyList())
            val localContacts = emergencyContactDao.getAllContactsSync()

            val remoteMap = remoteContacts.associateBy { it.id }
            val localMap = localContacts.associateBy { it.id }

            // 1. Sync remote items into local DB
            for (remote in remoteContacts) {
                val local = localMap[remote.id]
                if (local != null && local.updatedAt > remote.updatedAt) {
                    // Push updated local item to Firestore
                    firestoreRepository.saveEmergencyContact(userId, local.copy(userId = userId))
                } else {
                    // Insert or update remote item into Room
                    emergencyContactDao.insertContact(remote.copy(userId = userId))
                }
            }

            // 2. Upload local items not present in Firestore
            for (local in localContacts) {
                if (!remoteMap.containsKey(local.id)) {
                    firestoreRepository.saveEmergencyContact(userId, local.copy(userId = userId))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "syncEmergencyContacts failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun syncSavedDestinations(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteResult = firestoreRepository.fetchSavedDestinations(userId)
            if (remoteResult.isFailure) {
                return@withContext Result.failure(remoteResult.exceptionOrNull() ?: Exception("Fetch saved destinations failed"))
            }
            val remoteSaved = remoteResult.getOrDefault(emptyList())
            val localSaved = savedDestinationDao.getAllSavedDestinationsSync()

            val remoteMap = remoteSaved.associateBy { it.destinationId }
            val localMap = localSaved.associateBy { it.destinationId }

            for (remote in remoteSaved) {
                val local = localMap[remote.destinationId]
                if (local != null && local.updatedAt > remote.updatedAt) {
                    firestoreRepository.saveSavedDestination(userId, local.copy(userId = userId))
                } else {
                    savedDestinationDao.saveDestination(remote.copy(userId = userId))
                }
            }

            for (local in localSaved) {
                if (!remoteMap.containsKey(local.destinationId)) {
                    firestoreRepository.saveSavedDestination(userId, local.copy(userId = userId))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "syncSavedDestinations failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun syncSafetyAdvisories(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteResult = firestoreRepository.fetchSafetyAdvisories(userId)
            if (remoteResult.isFailure) {
                return@withContext Result.failure(remoteResult.exceptionOrNull() ?: Exception("Fetch safety advisories failed"))
            }
            val remoteAdvisories = remoteResult.getOrDefault(emptyList())
            val localAdvisories = safetyAdvisoryDao.getAllAdvisoriesSync()

            val remoteMap = remoteAdvisories.associateBy { it.id }
            val localMap = localAdvisories.associateBy { it.id }

            for (remote in remoteAdvisories) {
                val local = localMap[remote.id]
                if (local != null && local.updatedAt > remote.updatedAt) {
                    firestoreRepository.saveSafetyAdvisory(userId, local.copy(userId = userId))
                } else {
                    safetyAdvisoryDao.insertAdvisory(remote.copy(userId = userId))
                }
            }

            for (local in localAdvisories) {
                if (!remoteMap.containsKey(local.id)) {
                    firestoreRepository.saveSafetyAdvisory(userId, local.copy(userId = userId))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "syncSafetyAdvisories failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }
}
