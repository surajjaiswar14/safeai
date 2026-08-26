package com.example.safejourneyai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val jsonContent: String, // Full JSON dump of destination for offline access
    val sizeKb: Int,
    val downloadedAt: Long = System.currentTimeMillis()
)
