package com.example.safejourneyai.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String,
    val email: String = "",
    val phone: String = "",
    val avatar: String = "",
    val avatarUrl: String? = null,
    @ColumnInfo(name = "userId") val userId: String = "current_user",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
