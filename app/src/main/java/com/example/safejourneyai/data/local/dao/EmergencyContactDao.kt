package com.example.safejourneyai.data.local.dao

import androidx.room.*
import com.example.safejourneyai.data.local.entities.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isDefault DESC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts ORDER BY isDefault DESC, name ASC")
    suspend fun getAllContactsSync(): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId ORDER BY isDefault DESC, name ASC")
    fun getContactsByUserId(userId: String): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): EmergencyContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<EmergencyContactEntity>)

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContactEntity(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContact(id: Long)

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getContactsCount(): Int
}
