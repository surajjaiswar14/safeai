package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.local.dao.*
import com.example.safejourneyai.data.local.entities.*
import com.example.safejourneyai.data.model.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface SafeJourneyRepository {
    fun getAllDestinations(): Flow<List<Destination>>
    fun getSavedDestinations(): Flow<List<Destination>>
    fun getOfflinePacks(): Flow<List<Destination>>
    fun searchDestinations(query: String): Flow<List<Destination>>
    fun getDestinationsByCategory(category: String): Flow<List<Destination>>
    suspend fun getDestinationById(id: String): Destination?
    suspend fun toggleSaveDestination(id: String): Boolean
    suspend fun downloadOfflinePack(destination: Destination)
    suspend fun deleteOfflinePack(id: String)
    fun getEmergencyContacts(): Flow<List<EmergencyContactEntity>>
    suspend fun addEmergencyContact(contact: EmergencyContactEntity)
    suspend fun updateEmergencyContact(contact: EmergencyContactEntity)
    suspend fun deleteEmergencyContact(id: Long)
    fun getEmergencyServices(type: String = "ALL"): Flow<List<EmergencyServiceEntity>>
    fun getChecklist(travelType: String = "SOLO"): Flow<List<ChecklistItemEntity>>
    suspend fun updateChecklistItemCompletion(id: String, isCompleted: Boolean)
    fun getUserProfile(): Flow<UserProfileEntity?>
    suspend fun updateUserProfile(name: String, email: String, phone: String)
    fun getAdvisories(): Flow<List<AdvisoryEntity>>
}

interface RemoteSafeJourneyDataSource {
    suspend fun fetchRemoteDestinations(): List<Destination>?
}

class MockRemoteSafeJourneyDataSource : RemoteSafeJourneyDataSource {
    override suspend fun fetchRemoteDestinations(): List<Destination>? {
        return null // Offline-first local database acts as source of truth
    }
}

class SafeJourneyRepositoryImpl(
    private val db: SafeJourneyDatabase,
    private val remoteDataSource: RemoteSafeJourneyDataSource = MockRemoteSafeJourneyDataSource()
) : SafeJourneyRepository {

    private val destinationDao = db.destinationDao()
    private val savedDestinationDao = db.savedDestinationDao()
    private val offlinePackDao = db.offlinePackDao()
    private val emergencyContactDao = db.emergencyContactDao()
    private val emergencyServiceDao = db.emergencyServiceDao()
    private val safetyChecklistDao = db.safetyChecklistDao()
    private val userProfileDao = db.userProfileDao()
    private val advisoryDao = db.advisoryDao()

    override fun getAllDestinations(): Flow<List<Destination>> {
        return combine(
            destinationDao.getAllDestinations(),
            savedDestinationDao.getAllSavedDestinations(),
            offlinePackDao.getAllPacks()
        ) { destinations, saved, packs ->
            val savedIds = saved.map { it.destinationId }.toSet()
            val packIds = packs.map { it.destinationId }.toSet()

            destinations.map { entity ->
                entity.toDomainModel(
                    isSaved = savedIds.contains(entity.id),
                    isDownloaded = packIds.contains(entity.id)
                )
            }
        }
    }

    override fun getSavedDestinations(): Flow<List<Destination>> {
        return combine(
            destinationDao.getAllDestinations(),
            savedDestinationDao.getAllSavedDestinations(),
            offlinePackDao.getAllPacks()
        ) { destinations, saved, packs ->
            val savedIds = saved.map { it.destinationId }.toSet()
            val packIds = packs.map { it.destinationId }.toSet()

            destinations.filter { savedIds.contains(it.id) }.map { entity ->
                entity.toDomainModel(isSaved = true, isDownloaded = packIds.contains(entity.id))
            }
        }
    }

    override fun getOfflinePacks(): Flow<List<Destination>> {
        return combine(
            destinationDao.getAllDestinations(),
            offlinePackDao.getAllPacks()
        ) { destinations, packs ->
            val packIds = packs.map { it.destinationId }.toSet()

            destinations.filter { packIds.contains(it.id) }.map { entity ->
                entity.toDomainModel(isDownloaded = true)
            }
        }
    }

    override fun searchDestinations(query: String): Flow<List<Destination>> {
        return getAllDestinations().map { list ->
            if (query.isBlank()) list
            else list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.state.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
    }

    override fun getDestinationsByCategory(category: String): Flow<List<Destination>> {
        return getAllDestinations().map { list ->
            if (category == "All") list
            else list.filter { it.category.contains(category, ignoreCase = true) }
        }
    }

    override suspend fun getDestinationById(id: String): Destination? {
        val entity = destinationDao.getDestinationByIdSync(id) ?: return null
        val isSaved = savedDestinationDao.isDestinationSaved(id)
        val isDownloaded = offlinePackDao.isPackDownloaded(id)
        return entity.toDomainModel(isSaved = isSaved, isDownloaded = isDownloaded)
    }

    override suspend fun toggleSaveDestination(id: String): Boolean {
        val isSaved = savedDestinationDao.isDestinationSaved(id)
        if (isSaved) {
            savedDestinationDao.unsaveDestination(id)
            syncSavedDestinationToFirestore(id, isSaved = false)
            return false
        } else {
            savedDestinationDao.saveDestination(SavedDestinationEntity(destinationId = id))
            syncSavedDestinationToFirestore(id, isSaved = true)
            return true
        }
    }

    override suspend fun downloadOfflinePack(destination: Destination) {
        val jsonPayload = """{"id":"${destination.id}","name":"${destination.name}","safetyScore":${destination.safetyScore}}"""
        val entity = OfflinePackEntity(
            destinationId = destination.id,
            name = destination.name,
            state = destination.state,
            jsonContent = jsonPayload,
            weather = destination.weather,
            safetyInformation = destination.safetyScoreReason,
            scamInformation = destination.scamAwareness.joinToString(", "),
            localRules = destination.localRules.joinToString(", "),
            permitInformation = destination.permitInfo,
            safetyTips = destination.safetyTips.joinToString(", "),
            emergencyNumbers = destination.emergencyContacts.joinToString(", "),
            nearbyHelpInformation = destination.nearbyHospitals.joinToString(", ")
        )
        offlinePackDao.insertPack(entity)
        syncOfflinePackToFirestore(entity, isDeleted = false)
    }

    override suspend fun deleteOfflinePack(id: String) {
        offlinePackDao.deletePack(id)
        val pack = offlinePackDao.getPackByDestinationId(id)
        if (pack != null) {
            syncOfflinePackToFirestore(pack, isDeleted = true)
        }
    }

    override fun getEmergencyContacts(): Flow<List<EmergencyContactEntity>> {
        return emergencyContactDao.getAllContacts()
    }

    override suspend fun addEmergencyContact(contact: EmergencyContactEntity) {
        emergencyContactDao.insertContact(contact)
        syncEmergencyContactToFirestore(contact, isDeleted = false)
    }

    override suspend fun updateEmergencyContact(contact: EmergencyContactEntity) {
        emergencyContactDao.updateContact(contact)
        syncEmergencyContactToFirestore(contact, isDeleted = false)
    }

    override suspend fun deleteEmergencyContact(id: Long) {
        emergencyContactDao.deleteContact(id)
        syncEmergencyContactToFirestore(EmergencyContactEntity(id = id, name = "", type = "", phoneNumber = "", description = ""), isDeleted = true)
    }

    override fun getEmergencyServices(type: String): Flow<List<EmergencyServiceEntity>> {
        return if (type == "ALL") emergencyServiceDao.getAllServices()
        else emergencyServiceDao.getServicesByType(type)
    }

    override fun getChecklist(travelType: String): Flow<List<ChecklistItemEntity>> {
        return safetyChecklistDao.getChecklist(travelType)
    }

    override suspend fun updateChecklistItemCompletion(id: String, isCompleted: Boolean) {
        safetyChecklistDao.updateCompletion(id, isCompleted)
        syncChecklistToFirestore(id, isCompleted)
    }

    override fun getUserProfile(): Flow<UserProfileEntity?> {
        return userProfileDao.getProfile()
    }

    override suspend fun updateUserProfile(name: String, email: String, phone: String) {
        userProfileDao.updateProfile(UserProfileEntity(id = "current_user", name = name, email = email, phone = phone))
    }

    override fun getAdvisories(): Flow<List<AdvisoryEntity>> {
        return advisoryDao.getAllAdvisories()
    }

    // --- Background Firestore Synchronization Helpers ---

    private fun syncSavedDestinationToFirestore(destinationId: String, isSaved: Boolean) {
        try {
            val auth = com.example.safejourneyai.data.remote.FirebaseManager.auth
            val firestore = com.example.safejourneyai.data.remote.FirebaseManager.firestore
            val uid = auth?.currentUser?.uid ?: return
            if (firestore == null) return

            val docRef = firestore.collection("users").document(uid)
                .collection("saved_destinations").document(destinationId)

            if (isSaved) {
                docRef.set(
                    mapOf(
                        "destinationId" to destinationId,
                        "savedAt" to System.currentTimeMillis()
                    )
                )
            } else {
                docRef.delete()
            }
        } catch (e: Exception) {
            // Ignore offline/network exceptions - Room SQLite holds local truth
        }
    }

    private fun syncEmergencyContactToFirestore(contact: EmergencyContactEntity, isDeleted: Boolean) {
        try {
            val auth = com.example.safejourneyai.data.remote.FirebaseManager.auth
            val firestore = com.example.safejourneyai.data.remote.FirebaseManager.firestore
            val uid = auth?.currentUser?.uid ?: return
            if (firestore == null) return

            val docRef = firestore.collection("users").document(uid)
                .collection("emergency_contacts").document(contact.id.toString())

            if (isDeleted) {
                docRef.delete()
            } else {
                docRef.set(
                    mapOf(
                        "id" to contact.id,
                        "name" to contact.name,
                        "type" to contact.type,
                        "phoneNumber" to contact.phoneNumber,
                        "description" to contact.description,
                        "isDefault" to contact.isDefault
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore offline exceptions
        }
    }

    private fun syncOfflinePackToFirestore(pack: OfflinePackEntity, isDeleted: Boolean) {
        try {
            val auth = com.example.safejourneyai.data.remote.FirebaseManager.auth
            val firestore = com.example.safejourneyai.data.remote.FirebaseManager.firestore
            val uid = auth?.currentUser?.uid ?: return
            if (firestore == null) return

            val docRef = firestore.collection("users").document(uid)
                .collection("offline_packs").document(pack.destinationId)

            if (isDeleted) {
                docRef.delete()
            } else {
                docRef.set(
                    mapOf(
                        "destinationId" to pack.destinationId,
                        "name" to pack.name,
                        "state" to pack.state,
                        "downloadedAt" to pack.downloadedAt
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore offline exceptions
        }
    }

    private fun syncChecklistToFirestore(itemId: String, isCompleted: Boolean) {
        try {
            val auth = com.example.safejourneyai.data.remote.FirebaseManager.auth
            val firestore = com.example.safejourneyai.data.remote.FirebaseManager.firestore
            val uid = auth?.currentUser?.uid ?: return
            if (firestore == null) return

            firestore.collection("users").document(uid)
                .collection("checklists").document(itemId)
                .set(
                    mapOf(
                        "id" to itemId,
                        "isCompleted" to isCompleted,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
        } catch (e: Exception) {
            // Ignore offline exceptions
        }
    }

    private fun DestinationEntity.toDomainModel(isSaved: Boolean = false, isDownloaded: Boolean = false): Destination {
        return Destination(
            id = id,
            name = name,
            state = state,
            category = category,
            imageUrl = imageUrl,
            safetyScore = safetyScore,
            safetyScoreReason = safetyScoreReason,
            weather = weather,
            scamAwareness = scamAwareness,
            localRules = localRules,
            permitInfo = permitInfo,
            bestTime = bestTime,
            safetyTips = safetyTips,
            nearbyHospitals = nearbyHospitals,
            nearbyPolice = nearbyPolice,
            emergencyContacts = emergencyContacts,
            latitude = latitude,
            longitude = longitude,
            isSaved = isSaved,
            isDownloaded = isDownloaded
        )
    }
}
