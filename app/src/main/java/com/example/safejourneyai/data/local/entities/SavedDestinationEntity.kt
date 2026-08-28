package com.example.safejourneyai.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_destinations",
    indices = [Index(value = ["userId"])]
)
data class SavedDestinationEntity(
    @PrimaryKey val destinationId: String,
    val savedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "userId") val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
