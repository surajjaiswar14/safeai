package com.example.safejourneyai.data.local

import androidx.room.*
import com.example.safejourneyai.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {

    @Query("SELECT * FROM destinations ORDER BY name ASC")
    fun getAllDestinations(): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE id = :id LIMIT 1")
    fun getDestinationById(id: String): Flow<DestinationEntity?>

    @Query("SELECT * FROM destinations WHERE id = :id LIMIT 1")
    suspend fun getDestinationByIdSync(id: String): DestinationEntity?

    @Query("SELECT * FROM destinations WHERE name LIKE '%' || :query || '%' OR state LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchDestinations(query: String): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE category LIKE '%' || :category || '%'")
    fun getDestinationsByCategory(category: String): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE state LIKE '%' || :state || '%'")
    fun getDestinationsByState(state: String): Flow<List<DestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestinations(destinations: List<DestinationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: DestinationEntity)

    @Update
    suspend fun updateDestination(destination: DestinationEntity)

    @Delete
    suspend fun deleteDestination(destination: DestinationEntity)

    @Query("SELECT COUNT(*) FROM destinations")
    suspend fun getDestinationsCount(): Int

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

    @Query("SELECT * FROM offline_packs WHERE destinationId = :id LIMIT 1")
    suspend fun getPackByDestinationId(id: String): OfflinePackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflinePack(pack: OfflinePackEntity)

    @Query("DELETE FROM offline_packs WHERE destinationId = :id")
    suspend fun deleteOfflinePack(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM offline_packs WHERE destinationId = :id)")
    suspend fun isOfflinePackDownloaded(id: String): Boolean
}

@Dao
interface SavedDestinationDao {
    @Query("SELECT * FROM saved_destinations ORDER BY savedAt DESC")
    fun getAllSavedDestinations(): Flow<List<SavedDestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDestination(saved: SavedDestinationEntity)

    @Query("DELETE FROM saved_destinations WHERE destinationId = :id")
    suspend fun unsaveDestination(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_destinations WHERE destinationId = :id)")
    suspend fun isDestinationSaved(id: String): Boolean
}

@Dao
interface OfflinePackDao {
    @Query("SELECT * FROM offline_packs ORDER BY downloadedAt DESC")
    fun getAllPacks(): Flow<List<OfflinePackEntity>>

    @Query("SELECT * FROM offline_packs WHERE destinationId = :destId LIMIT 1")
    suspend fun getPackByDestinationId(destId: String): OfflinePackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: OfflinePackEntity)

    @Query("DELETE FROM offline_packs WHERE destinationId = :destId")
    suspend fun deletePack(destId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM offline_packs WHERE destinationId = :destId)")
    suspend fun isPackDownloaded(destId: String): Boolean

    @Query("UPDATE offline_packs SET status = :status WHERE destinationId = :destId")
    suspend fun updateDownloadStatus(destId: String, status: String)
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isDefault DESC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<EmergencyContactEntity>)

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContact(id: Long)

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getContactsCount(): Int
}

@Dao
interface EmergencyServiceDao {
    @Query("SELECT * FROM emergency_services ORDER BY type ASC, name ASC")
    fun getAllServices(): Flow<List<EmergencyServiceEntity>>

    @Query("SELECT * FROM emergency_services WHERE type = :type ORDER BY name ASC")
    fun getServicesByType(type: String): Flow<List<EmergencyServiceEntity>>

    @Query("SELECT * FROM emergency_services WHERE type = 'Hospital'")
    fun getHospitals(): Flow<List<EmergencyServiceEntity>>

    @Query("SELECT * FROM emergency_services WHERE type = 'Police'")
    fun getPoliceStations(): Flow<List<EmergencyServiceEntity>>

    @Query("SELECT * FROM emergency_services WHERE type = 'Pharmacy'")
    fun getPharmacies(): Flow<List<EmergencyServiceEntity>>

    @Query("SELECT * FROM emergency_services WHERE type = 'Tourist Help'")
    fun getTouristHelp(): Flow<List<EmergencyServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<EmergencyServiceEntity>)

    @Query("SELECT COUNT(*) FROM emergency_services")
    suspend fun getServicesCount(): Int
}

@Dao
interface SafetyChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE travelType = :travelType")
    fun getChecklist(travelType: String): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items")
    fun getAllChecklists(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(items: List<ChecklistItemEntity>)

    @Query("UPDATE checklist_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletion(id: String, isCompleted: Boolean)

    @Query("UPDATE checklist_items SET isCompleted = 0")
    suspend fun resetChecklist()

    @Query("SELECT COUNT(*) FROM checklist_items")
    suspend fun getChecklistCount(): Int
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
}

@Dao
interface AdvisoryDao {
    @Query("SELECT * FROM advisories ORDER BY createdAt DESC")
    fun getAllAdvisories(): Flow<List<AdvisoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisories(advisories: List<AdvisoryEntity>)

    @Query("UPDATE advisories SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("SELECT COUNT(*) FROM advisories")
    suspend fun getAdvisoriesCount(): Int
}
