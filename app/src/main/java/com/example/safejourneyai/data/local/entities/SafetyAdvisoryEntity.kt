package com.example.safejourneyai.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "safety_advisories",
    indices = [Index(value = ["userId"]), Index(value = ["destinationId"])]
)
data class SafetyAdvisoryEntity(
    @PrimaryKey val id: String,
    val destinationId: String = "",
    val title: String,
    val description: String,
    val severity: String = "INFO", // INFO, WATCH, CAUTION, EMERGENCY
    val category: String = "TRAVEL", // WEATHER, TRAVEL, SCAM, INFORMATION, EMERGENCY
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    @ColumnInfo(name = "userId") val userId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

typealias AdvisoryEntity = SafetyAdvisoryEntity
