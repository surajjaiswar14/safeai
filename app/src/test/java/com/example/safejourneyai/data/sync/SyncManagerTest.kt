package com.example.safejourneyai.data.sync

import androidx.test.core.app.ApplicationProvider
import com.example.safejourneyai.data.local.AppDatabase
import com.example.safejourneyai.data.local.entities.EmergencyContactEntity
import com.example.safejourneyai.data.local.entities.SafetyAdvisoryEntity
import com.example.safejourneyai.data.local.entities.SavedDestinationEntity
import com.example.safejourneyai.data.local.entities.UserProfileEntity
import com.example.safejourneyai.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeFirestoreRepository : FirestoreRepository {
    var remoteProfile: UserProfileEntity? = null
    val remoteContacts = mutableListOf<EmergencyContactEntity>()
    val remoteSaved = mutableListOf<SavedDestinationEntity>()
    val remoteAdvisories = mutableListOf<SafetyAdvisoryEntity>()

    var shouldFail = false

    override suspend fun fetchUserProfile(userId: String): Result<UserProfileEntity?> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        return Result.success(remoteProfile)
    }

    override suspend fun saveUserProfile(profile: UserProfileEntity): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        remoteProfile = profile
        return Result.success(Unit)
    }

    override suspend fun fetchEmergencyContacts(userId: String): Result<List<EmergencyContactEntity>> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        return Result.success(remoteContacts)
    }

    override suspend fun saveEmergencyContact(userId: String, contact: EmergencyContactEntity): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        remoteContacts.removeAll { it.id == contact.id }
        remoteContacts.add(contact)
        return Result.success(Unit)
    }

    override suspend fun deleteEmergencyContact(userId: String, contactId: Long): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        remoteContacts.removeAll { it.id == contactId }
        return Result.success(Unit)
    }

    override suspend fun fetchSavedDestinations(userId: String): Result<List<SavedDestinationEntity>> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        return Result.success(remoteSaved)
    }

    override suspend fun saveSavedDestination(userId: String, destination: SavedDestinationEntity): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        remoteSaved.removeAll { it.destinationId == destination.destinationId }
        remoteSaved.add(destination)
        return Result.success(Unit)
    }

    override suspend fun deleteSavedDestination(userId: String, destinationId: String): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        remoteSaved.removeAll { it.destinationId == destinationId }
        return Result.success(Unit)
    }

    override suspend fun fetchSafetyAdvisories(userId: String): Result<List<SafetyAdvisoryEntity>> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        return Result.success(remoteAdvisories)
    }

    override suspend fun saveSafetyAdvisory(userId: String, advisory: SafetyAdvisoryEntity): Result<Unit> {
        if (shouldFail) return Result.failure(RuntimeException("Network failure"))
        remoteAdvisories.removeAll { it.id == advisory.id }
        remoteAdvisories.add(advisory)
        return Result.success(Unit)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SyncManagerTest {

    private lateinit var db: AppDatabase
    private lateinit var fakeFirestore: FakeFirestoreRepository
    private lateinit var syncManager: DataSyncManager

    @Before
    fun setUp() {
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        fakeFirestore = FakeFirestoreRepository()
        syncManager = DataSyncManagerImpl(db, fakeFirestore)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testSyncUserProfile_RemoteToLocal() = runBlocking {
        val testUid = "user_123"
        val serverProfile = UserProfileEntity(
            id = testUid,
            name = "Alice Cloud",
            email = "alice@cloud.com",
            userId = testUid,
            updatedAt = 2000L
        )
        fakeFirestore.remoteProfile = serverProfile

        val result = syncManager.syncUserProfile(testUid)
        assertTrue(result.isSuccess)

        val localProfile = db.userProfileDao().getProfileSync()
        assertNotNull(localProfile)
        assertEquals("Alice Cloud", localProfile?.name)
        assertEquals("alice@cloud.com", localProfile?.email)
    }

    @Test
    fun testSyncUserProfile_LocalNewerThanRemote() = runBlocking {
        val testUid = "user_456"
        val localProfile = UserProfileEntity(
            id = "current_user",
            name = "Bob Local Updated",
            email = "bob@local.com",
            userId = testUid,
            updatedAt = 5000L
        )
        db.userProfileDao().createProfile(localProfile)

        val serverProfile = UserProfileEntity(
            id = testUid,
            name = "Bob Server Old",
            email = "bob@server.com",
            userId = testUid,
            updatedAt = 1000L
        )
        fakeFirestore.remoteProfile = serverProfile

        val result = syncManager.syncUserProfile(testUid)
        assertTrue(result.isSuccess)

        // Remote profile should be updated with local newer data
        assertEquals("Bob Local Updated", fakeFirestore.remoteProfile?.name)
    }

    @Test
    fun testSyncEmergencyContacts_PullFromRemote() = runBlocking {
        val testUid = "user_789"
        val contact1 = EmergencyContactEntity(
            id = 101L,
            name = "Mom Cell",
            type = "Family",
            phoneNumber = "+123456789",
            userId = testUid,
            updatedAt = 1000L
        )
        fakeFirestore.remoteContacts.add(contact1)

        val result = syncManager.syncEmergencyContacts(testUid)
        assertTrue(result.isSuccess)

        val localContacts = db.emergencyContactDao().getAllContacts().first()
        assertEquals(1, localContacts.size)
        assertEquals("Mom Cell", localContacts[0].name)
    }

    @Test
    fun testSyncSavedDestinations_PullFromRemote() = runBlocking {
        val testUid = "user_789"
        val saved1 = SavedDestinationEntity(
            destinationId = "jaipur_pink_city",
            userId = testUid,
            updatedAt = 1000L
        )
        fakeFirestore.remoteSaved.add(saved1)

        val result = syncManager.syncSavedDestinations(testUid)
        assertTrue(result.isSuccess)

        val isSaved = db.savedDestinationDao().isDestinationSaved("jaipur_pink_city")
        assertTrue(isSaved)
    }

    @Test
    fun testSyncSafetyAdvisories_PullFromRemote() = runBlocking {
        val testUid = "user_789"
        val advisory1 = SafetyAdvisoryEntity(
            id = "adv_101",
            destinationId = "rishikesh_ganga",
            title = "High River Current Advisory",
            description = "Avoid rafting due to heavy rainfall upstream.",
            severity = "CAUTION",
            userId = testUid,
            updatedAt = 1000L
        )
        fakeFirestore.remoteAdvisories.add(advisory1)

        val result = syncManager.syncSafetyAdvisories(testUid)
        assertTrue(result.isSuccess)

        val localAdvisories = db.safetyAdvisoryDao().getAllAdvisories().first()
        assertEquals(1, localAdvisories.size)
        assertEquals("High River Current Advisory", localAdvisories[0].title)
    }

    @Test
    fun testSyncAllUserData_HandlesNetworkErrorGracefully() = runBlocking {
        val testUid = "user_err"
        fakeFirestore.shouldFail = true

        val result = syncManager.syncAllUserData(testUid)
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
}
