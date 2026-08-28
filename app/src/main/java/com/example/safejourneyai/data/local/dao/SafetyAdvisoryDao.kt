package com.example.safejourneyai.data.local.dao

import androidx.room.*
import com.example.safejourneyai.data.local.entities.SafetyAdvisoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyAdvisoryDao {
    @Query("SELECT * FROM safety_advisories ORDER BY createdAt DESC")
    fun getAllAdvisories(): Flow<List<SafetyAdvisoryEntity>>

    @Query("SELECT * FROM safety_advisories ORDER BY createdAt DESC")
    suspend fun getAllAdvisoriesSync(): List<SafetyAdvisoryEntity>

    @Query("SELECT * FROM safety_advisories WHERE userId = :userId OR userId IS NULL ORDER BY createdAt DESC")
    fun getAdvisoriesByUserId(userId: String): Flow<List<SafetyAdvisoryEntity>>

    @Query("SELECT * FROM safety_advisories WHERE destinationId = :destId ORDER BY createdAt DESC")
    fun getAdvisoriesByDestination(destId: String): Flow<List<SafetyAdvisoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisory(advisory: SafetyAdvisoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvisories(advisories: List<SafetyAdvisoryEntity>)

    @Update
    suspend fun updateAdvisory(advisory: SafetyAdvisoryEntity)

    @Delete
    suspend fun deleteAdvisory(advisory: SafetyAdvisoryEntity)

    @Query("UPDATE safety_advisories SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("SELECT COUNT(*) FROM safety_advisories")
    suspend fun getAdvisoriesCount(): Int
}

typealias AdvisoryDao = SafetyAdvisoryDao
