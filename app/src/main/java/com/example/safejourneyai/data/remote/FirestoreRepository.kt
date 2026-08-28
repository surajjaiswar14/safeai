package com.example.safejourneyai.data.remote

import android.util.Log
import com.example.safejourneyai.data.local.entities.EmergencyContactEntity
import com.example.safejourneyai.data.local.entities.SafetyAdvisoryEntity
import com.example.safejourneyai.data.local.entities.SavedDestinationEntity
import com.example.safejourneyai.data.local.entities.UserProfileEntity
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

interface FirestoreRepository {
    suspend fun fetchUserProfile(userId: String): Result<UserProfileEntity?>
    suspend fun saveUserProfile(profile: UserProfileEntity): Result<Unit>

    suspend fun fetchEmergencyContacts(userId: String): Result<List<EmergencyContactEntity>>
    suspend fun saveEmergencyContact(userId: String, contact: EmergencyContactEntity): Result<Unit>
    suspend fun deleteEmergencyContact(userId: String, contactId: Long): Result<Unit>

    suspend fun fetchSavedDestinations(userId: String): Result<List<SavedDestinationEntity>>
    suspend fun saveSavedDestination(userId: String, destination: SavedDestinationEntity): Result<Unit>
    suspend fun deleteSavedDestination(userId: String, destinationId: String): Result<Unit>

    suspend fun fetchSafetyAdvisories(userId: String): Result<List<SafetyAdvisoryEntity>>
    suspend fun saveSafetyAdvisory(userId: String, advisory: SafetyAdvisoryEntity): Result<Unit>
}

class FirestoreRepositoryImpl : FirestoreRepository {

    private val TAG = "FirestoreRepository"

    override suspend fun fetchUserProfile(userId: String): Result<UserProfileEntity?> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore uninitialized or unavailable"))

        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (!doc.exists()) {
                return Result.success(null)
            }
            val data = doc.data ?: return Result.success(null)
            val profile = UserProfileEntity(
                id = doc.id,
                name = (data["displayName"] ?: data["name"] ?: "Traveler").toString(),
                email = (data["email"] ?: "").toString(),
                phone = (data["phone"] ?: "").toString(),
                avatar = (data["photoUrl"] ?: data["avatar"] ?: "").toString(),
                userId = userId,
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
            )
            Result.success(profile)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch user profile for $userId: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(profile: UserProfileEntity): Result<Unit> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            val userId = profile.userId.ifEmpty { profile.id }
            val map = mapOf(
                "uid" to userId,
                "displayName" to profile.name,
                "email" to profile.email,
                "phone" to profile.phone,
                "photoUrl" to profile.avatar,
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt
            )
            firestore.collection("users").document(userId).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save user profile: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun fetchEmergencyContacts(userId: String): Result<List<EmergencyContactEntity>> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("emergency_contacts").get().await()

            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val id = (data["id"] as? Long) ?: doc.id.toLongOrNull() ?: System.currentTimeMillis()
                EmergencyContactEntity(
                    id = id,
                    name = (data["name"] ?: "").toString(),
                    type = (data["type"] ?: "").toString(),
                    phoneNumber = (data["phoneNumber"] ?: "").toString(),
                    description = (data["description"] ?: "").toString(),
                    isDefault = (data["isDefault"] as? Boolean) ?: false,
                    userId = userId,
                    createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch emergency contacts: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun saveEmergencyContact(userId: String, contact: EmergencyContactEntity): Result<Unit> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            val docId = contact.id.toString()
            val map = mapOf(
                "id" to contact.id,
                "name" to contact.name,
                "type" to contact.type,
                "phoneNumber" to contact.phoneNumber,
                "description" to contact.description,
                "isDefault" to contact.isDefault,
                "userId" to userId,
                "createdAt" to contact.createdAt,
                "updatedAt" to contact.updatedAt
            )
            firestore.collection("users").document(userId)
                .collection("emergency_contacts").document(docId).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save emergency contact: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun deleteEmergencyContact(userId: String, contactId: Long): Result<Unit> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            firestore.collection("users").document(userId)
                .collection("emergency_contacts").document(contactId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete emergency contact: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun fetchSavedDestinations(userId: String): Result<List<SavedDestinationEntity>> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("saved_destinations").get().await()

            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val destId = (data["destinationId"] ?: doc.id).toString()
                SavedDestinationEntity(
                    destinationId = destId,
                    savedAt = (data["savedAt"] as? Long) ?: System.currentTimeMillis(),
                    userId = userId,
                    createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch saved destinations: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun saveSavedDestination(userId: String, destination: SavedDestinationEntity): Result<Unit> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            val map = mapOf(
                "destinationId" to destination.destinationId,
                "savedAt" to destination.savedAt,
                "userId" to userId,
                "createdAt" to destination.createdAt,
                "updatedAt" to destination.updatedAt
            )
            firestore.collection("users").document(userId)
                .collection("saved_destinations").document(destination.destinationId).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save saved destination: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun deleteSavedDestination(userId: String, destinationId: String): Result<Unit> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            firestore.collection("users").document(userId)
                .collection("saved_destinations").document(destinationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete saved destination: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun fetchSafetyAdvisories(userId: String): Result<List<SafetyAdvisoryEntity>> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            // Fetch public advisories from /advisories as well as user advisories from /users/{userId}/safety_advisories
            val publicSnapshot = try {
                firestore.collection("advisories").get().await()
            } catch (e: Exception) {
                null
            }

            val userSnapshot = try {
                firestore.collection("users").document(userId)
                    .collection("safety_advisories").get().await()
            } catch (e: Exception) {
                null
            }

            val publicAdvisories = publicSnapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                SafetyAdvisoryEntity(
                    id = doc.id,
                    destinationId = (data["destinationId"] ?: "").toString(),
                    title = (data["title"] ?: "").toString(),
                    description = (data["description"] ?: "").toString(),
                    severity = (data["severity"] ?: "INFO").toString(),
                    category = (data["category"] ?: "TRAVEL").toString(),
                    createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                    isRead = (data["isRead"] as? Boolean) ?: false,
                    userId = userId,
                    updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                )
            } ?: emptyList()

            val userAdvisories = userSnapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                SafetyAdvisoryEntity(
                    id = doc.id,
                    destinationId = (data["destinationId"] ?: "").toString(),
                    title = (data["title"] ?: "").toString(),
                    description = (data["description"] ?: "").toString(),
                    severity = (data["severity"] ?: "INFO").toString(),
                    category = (data["category"] ?: "TRAVEL").toString(),
                    createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                    isRead = (data["isRead"] as? Boolean) ?: false,
                    userId = userId,
                    updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                )
            } ?: emptyList()

            val combined = (publicAdvisories + userAdvisories).distinctBy { it.id }
            Result.success(combined)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch safety advisories: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun saveSafetyAdvisory(userId: String, advisory: SafetyAdvisoryEntity): Result<Unit> {
        val firestore = FirebaseManager.firestore
            ?: return Result.failure(IllegalStateException("Firestore unavailable"))

        return try {
            val map = mapOf(
                "id" to advisory.id,
                "destinationId" to advisory.destinationId,
                "title" to advisory.title,
                "description" to advisory.description,
                "severity" to advisory.severity,
                "category" to advisory.category,
                "createdAt" to advisory.createdAt,
                "isRead" to advisory.isRead,
                "userId" to userId,
                "updatedAt" to advisory.updatedAt
            )
            firestore.collection("users").document(userId)
                .collection("safety_advisories").document(advisory.id).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save safety advisory: ${e.localizedMessage}")
            Result.failure(e)
        }
    }
}
