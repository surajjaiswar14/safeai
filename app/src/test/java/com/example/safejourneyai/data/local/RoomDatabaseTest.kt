package com.example.safejourneyai.data.local

import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.safejourneyai.data.local.entities.EmergencyContactEntity
import com.example.safejourneyai.data.local.entities.SafetyAdvisoryEntity
import com.example.safejourneyai.data.local.entities.SavedDestinationEntity
import com.example.safejourneyai.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
        .allowMainThreadQueries()
        .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    // --- User Profile CRUD Tests ---

    @Test
    fun insertAndReadUserProfile() = runBlocking {
        val profile = UserProfileEntity(
            id = "user_123",
            userId = "user_123",
            name = "Test User",
            email = "test@safejourney.ai",
            phone = "+123456789"
        )
        database.userProfileDao().insertProfile(profile)

        val retrieved = database.userProfileDao().getProfileByUserId("user_123").first()
        assertNotNull(retrieved)
        assertEquals("Test User", retrieved?.name)
        assertEquals("test@safejourney.ai", retrieved?.email)
    }

    @Test
    fun updateUserProfile() = runBlocking {
        val profile = UserProfileEntity(
            id = "user_123",
            userId = "user_123",
            name = "Original Name",
            email = "original@safejourney.ai"
        )
        database.userProfileDao().insertProfile(profile)

        val updatedProfile = profile.copy(name = "Updated Name", phone = "+987654321")
        database.userProfileDao().updateProfile(updatedProfile)

        val retrieved = database.userProfileDao().getProfileByUserId("user_123").first()
        assertNotNull(retrieved)
        assertEquals("Updated Name", retrieved?.name)
        assertEquals("+987654321", retrieved?.phone)
    }

    @Test
    fun deleteUserProfile() = runBlocking {
        val profile = UserProfileEntity(
            id = "user_123",
            userId = "user_123",
            name = "To Be Deleted",
            email = "delete@safejourney.ai"
        )
        database.userProfileDao().insertProfile(profile)

        database.userProfileDao().deleteProfile(profile)

        val retrieved = database.userProfileDao().getProfileByUserId("user_123").first()
        assertNull(retrieved)
    }

    // --- Emergency Contact CRUD Tests ---

    @Test
    fun insertAndReadEmergencyContact() = runBlocking {
        val contact = EmergencyContactEntity(
            id = 1L,
            userId = "user_123",
            name = "John Doe",
            type = "Family",
            phoneNumber = "+1987654321",
            isDefault = true
        )
        database.emergencyContactDao().insertContact(contact)

        val contacts = database.emergencyContactDao().getContactsByUserId("user_123").first()
        assertEquals(1, contacts.size)
        assertEquals("John Doe", contacts[0].name)
        assertEquals("+1987654321", contacts[0].phoneNumber)
        assertTrue(contacts[0].isDefault)
    }

    @Test
    fun updateEmergencyContact() = runBlocking {
        val contact = EmergencyContactEntity(
            id = 1L,
            userId = "user_123",
            name = "John Doe",
            type = "Family",
            phoneNumber = "+1987654321"
        )
        database.emergencyContactDao().insertContact(contact)

        val updatedContact = contact.copy(phoneNumber = "+1112223334")
        database.emergencyContactDao().updateContact(updatedContact)

        val retrieved = database.emergencyContactDao().getContactById(1L)
        assertNotNull(retrieved)
        assertEquals("+1112223334", retrieved?.phoneNumber)
    }

    @Test
    fun deleteEmergencyContact() = runBlocking {
        val contact = EmergencyContactEntity(
            id = 1L,
            userId = "user_123",
            name = "John Doe",
            type = "Family",
            phoneNumber = "+1987654321"
        )
        database.emergencyContactDao().insertContact(contact)

        database.emergencyContactDao().deleteContact(1L)

        val contacts = database.emergencyContactDao().getContactsByUserId("user_123").first()
        assertTrue(contacts.isEmpty())
    }

    // --- Saved Destination CRUD Tests ---

    @Test
    fun insertAndReadSavedDestination() = runBlocking {
        val saved = SavedDestinationEntity(
            destinationId = "jaipur",
            userId = "user_123"
        )
        database.savedDestinationDao().saveDestination(saved)

        val savedList = database.savedDestinationDao().getSavedDestinationsByUserId("user_123").first()
        assertEquals(1, savedList.size)
        assertEquals("jaipur", savedList[0].destinationId)
        assertTrue(database.savedDestinationDao().isDestinationSaved("jaipur"))
    }

    @Test
    fun updateSavedDestination() = runBlocking {
        val saved = SavedDestinationEntity(
            destinationId = "jaipur",
            userId = "user_123",
            savedAt = 1000L
        )
        database.savedDestinationDao().saveDestination(saved)

        val updatedSaved = saved.copy(savedAt = 2000L)
        database.savedDestinationDao().updateSavedDestination(updatedSaved)

        val savedList = database.savedDestinationDao().getSavedDestinationsByUserId("user_123").first()
        assertEquals(1, savedList.size)
        assertEquals(2000L, savedList[0].savedAt)
    }

    @Test
    fun deleteSavedDestination() = runBlocking {
        val saved = SavedDestinationEntity(
            destinationId = "jaipur",
            userId = "user_123"
        )
        database.savedDestinationDao().saveDestination(saved)

        database.savedDestinationDao().unsaveDestination("jaipur")

        val savedList = database.savedDestinationDao().getSavedDestinationsByUserId("user_123").first()
        assertTrue(savedList.isEmpty())
    }

    // --- Safety Advisory CRUD Tests ---

    @Test
    fun insertAndReadSafetyAdvisory() = runBlocking {
        val advisory = SafetyAdvisoryEntity(
            id = "advisory_1",
            userId = "user_123",
            destinationId = "jaipur",
            title = "Heavy Rainfall Warning",
            description = "Expect severe localized flooding near city centers.",
            severity = "CAUTION",
            category = "WEATHER"
        )
        database.safetyAdvisoryDao().insertAdvisory(advisory)

        val advisories = database.safetyAdvisoryDao().getAdvisoriesByUserId("user_123").first()
        assertEquals(1, advisories.size)
        assertEquals("Heavy Rainfall Warning", advisories[0].title)
        assertEquals("CAUTION", advisories[0].severity)
    }

    @Test
    fun updateSafetyAdvisory() = runBlocking {
        val advisory = SafetyAdvisoryEntity(
            id = "advisory_1",
            userId = "user_123",
            destinationId = "jaipur",
            title = "Heavy Rainfall Warning",
            description = "Initial alert",
            isRead = false
        )
        database.safetyAdvisoryDao().insertAdvisory(advisory)

        database.safetyAdvisoryDao().markAsRead("advisory_1")

        val advisories = database.safetyAdvisoryDao().getAdvisoriesByUserId("user_123").first()
        assertEquals(1, advisories.size)
        assertTrue(advisories[0].isRead)
    }

    @Test
    fun deleteSafetyAdvisory() = runBlocking {
        val advisory = SafetyAdvisoryEntity(
            id = "advisory_1",
            userId = "user_123",
            destinationId = "jaipur",
            title = "Heavy Rainfall Warning",
            description = "Alert to remove"
        )
        database.safetyAdvisoryDao().insertAdvisory(advisory)

        database.safetyAdvisoryDao().deleteAdvisory(advisory)

        val advisories = database.safetyAdvisoryDao().getAdvisoriesByUserId("user_123").first()
        assertTrue(advisories.isEmpty())
    }
}
