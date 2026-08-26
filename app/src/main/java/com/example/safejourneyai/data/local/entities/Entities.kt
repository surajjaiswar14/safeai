package com.example.safejourneyai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val state: String,
    val category: String,
    val description: String = "",
    val imageUrl: String,
    val safetyScore: Double,
    val safetyScoreReason: String,
    val weather: String,
    val bestTime: String,
    val scamAwareness: List<String>,
    val localRules: List<String>,
    val permitInfo: String,
    val safetyTips: List<String>,
    val nearbyHospitals: List<String>,
    val nearbyPolice: List<String>,
    val emergencyContacts: List<String>,
    val latitude: Double,
    val longitude: Double,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "saved_destinations")
data class SavedDestinationEntity(
    @PrimaryKey val destinationId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_packs")
data class OfflinePackEntity(
    @PrimaryKey val destinationId: String,
    val name: String,
    val state: String,
    val jsonContent: String,
    val sizeKb: Int = 450,
    val status: String = "DOWNLOADED",
    val weather: String = "",
    val safetyInformation: String = "",
    val scamInformation: String = "",
    val localRules: String = "",
    val permitInformation: String = "",
    val safetyTips: String = "",
    val emergencyNumbers: String = "",
    val nearbyHelpInformation: String = "",
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val phoneNumber: String,
    val description: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "emergency_services")
data class EmergencyServiceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // Hospital, Police, Pharmacy, Tourist Desk
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val distance: String,
    val description: String
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val destinationId: String = "",
    val travelType: String, // SOLO, FAMILY, TREKKING, PILGRIMAGE, ADVENTURE
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String,
    val email: String = "",
    val phone: String = "",
    val avatar: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "advisories")
data class AdvisoryEntity(
    @PrimaryKey val id: String,
    val destinationId: String = "",
    val title: String,
    val description: String,
    val severity: String, // INFO, WATCH, CAUTION, EMERGENCY
    val category: String, // WEATHER, TRAVEL, SCAM, INFORMATION, EMERGENCY
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
