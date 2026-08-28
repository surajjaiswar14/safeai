package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.SafeJourneyDatabase
import com.example.safejourneyai.data.local.entities.UserProfileEntity
import com.example.safejourneyai.data.remote.FirebaseManager
import com.example.safejourneyai.data.sync.DataSyncManager
import com.example.safejourneyai.data.sync.DataSyncManagerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val profile: UserProfileEntity) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<UserProfileEntity>
    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfileEntity>
    suspend fun signInWithGoogle(idToken: String): Result<UserProfileEntity>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signInAsGuest(): UserProfileEntity
    suspend fun signOut()
    suspend fun updateProfile(name: String, email: String, phone: String, photoUrl: String = ""): Result<UserProfileEntity>
    fun getLocalProfile(): Flow<UserProfileEntity?>
}

class AuthRepositoryImpl(
    private val db: SafeJourneyDatabase,
    private val syncManager: DataSyncManager = DataSyncManagerImpl(db)
) : AuthRepository {

    private val userProfileDao = db.userProfileDao()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState

    init {
        checkCurrentAuthStatus()
    }

    private fun triggerBackgroundSync(userId: String) {
        if (userId.isNotBlank() && userId != "guest_user" && userId != "local_user") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    syncManager.syncAllUserData(userId)
                } catch (e: Exception) {
                    // Ignore sync errors
                }
            }
        }
    }

    private fun checkCurrentAuthStatus() {
        val firebaseAuth = FirebaseManager.auth
        val currentUser = firebaseAuth?.currentUser

        if (currentUser != null) {
            val profile = UserProfileEntity(
                id = currentUser.uid,
                name = currentUser.displayName.takeIf { !it.isNullOrBlank() } ?: "Traveler",
                email = currentUser.email ?: "",
                phone = currentUser.phoneNumber ?: "",
                avatar = currentUser.photoUrl?.toString() ?: "",
                userId = currentUser.uid
            )
            _authState.value = AuthState.Authenticated(profile)
            triggerBackgroundSync(currentUser.uid)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    override suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<UserProfileEntity> {
        val auth = FirebaseManager.auth
        val firestore = FirebaseManager.firestore

        if (auth == null) {
            val profile = UserProfileEntity(
                id = "local_user_${System.currentTimeMillis()}",
                name = name,
                email = email
            )
            userProfileDao.createProfile(profile)
            _authState.value = AuthState.Authenticated(profile)
            return Result.success(profile)
        }

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw IllegalStateException("User creation failed")

            val profile = UserProfileEntity(
                id = user.uid,
                name = name,
                email = email,
                userId = user.uid
            )

            firestore?.collection("users")?.document(user.uid)?.set(
                mapOf(
                    "uid" to user.uid,
                    "displayName" to name,
                    "email" to email,
                    "phone" to "",
                    "photoUrl" to "",
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
            )?.await()

            userProfileDao.createProfile(profile)
            _authState.value = AuthState.Authenticated(profile)
            triggerBackgroundSync(user.uid)
            Result.success(profile)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Sign up failed")
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<UserProfileEntity> {
        val auth = FirebaseManager.auth

        if (auth == null) {
            val profile = UserProfileEntity(
                id = "local_user",
                name = "Traveler",
                email = email
            )
            userProfileDao.createProfile(profile)
            _authState.value = AuthState.Authenticated(profile)
            return Result.success(profile)
        }

        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw IllegalStateException("Sign in failed")

            val profile = UserProfileEntity(
                id = user.uid,
                name = user.displayName.takeIf { !it.isNullOrBlank() } ?: "Traveler",
                email = user.email ?: email,
                avatar = user.photoUrl?.toString() ?: "",
                userId = user.uid
            )

            userProfileDao.createProfile(profile)
            _authState.value = AuthState.Authenticated(profile)
            triggerBackgroundSync(user.uid)
            Result.success(profile)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Sign in failed")
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfileEntity> {
        val auth = FirebaseManager.auth
        val firestore = FirebaseManager.firestore

        if (auth == null) {
            val profile = UserProfileEntity(
                id = "google_user_offline",
                name = "Google Traveler",
                email = "google@safejourney.ai"
            )
            userProfileDao.createProfile(profile)
            _authState.value = AuthState.Authenticated(profile)
            return Result.success(profile)
        }

        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw IllegalStateException("Google sign in failed")

            val profile = UserProfileEntity(
                id = user.uid,
                name = user.displayName.takeIf { !it.isNullOrBlank() } ?: "Traveler",
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                avatar = user.photoUrl?.toString() ?: "",
                userId = user.uid
            )

            firestore?.collection("users")?.document(user.uid)?.set(
                mapOf(
                    "uid" to user.uid,
                    "displayName" to profile.name,
                    "email" to profile.email,
                    "phone" to profile.phone,
                    "photoUrl" to profile.avatar,
                    "updatedAt" to System.currentTimeMillis()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )?.await()

            userProfileDao.createProfile(profile)
            _authState.value = AuthState.Authenticated(profile)
            triggerBackgroundSync(user.uid)
            Result.success(profile)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Google sign in failed")
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        val auth = FirebaseManager.auth ?: return Result.success(Unit)
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAsGuest(): UserProfileEntity {
        val profile = UserProfileEntity(
            id = "guest_user",
            name = "Guest Traveler",
            email = "guest@safejourney.ai",
            userId = "guest_user"
        )
        userProfileDao.createProfile(profile)
        _authState.value = AuthState.Authenticated(profile)
        return profile
    }

    override suspend fun signOut() {
        FirebaseManager.auth?.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun updateProfile(name: String, email: String, phone: String, photoUrl: String): Result<UserProfileEntity> {
        val current = (_authState.value as? AuthState.Authenticated)?.profile
            ?: UserProfileEntity(id = "current_user", name = name, email = email, phone = phone, avatar = photoUrl)

        val updated = current.copy(
            name = name,
            email = email,
            phone = phone,
            avatar = photoUrl.ifEmpty { current.avatar },
            updatedAt = System.currentTimeMillis()
        )

        userProfileDao.createProfile(updated)
        _authState.value = AuthState.Authenticated(updated)

        val auth = FirebaseManager.auth
        val firestore = FirebaseManager.firestore
        if (auth?.currentUser != null && firestore != null) {
            try {
                firestore.collection("users").document(auth.currentUser!!.uid).update(
                    mapOf(
                        "displayName" to name,
                        "email" to email,
                        "phone" to phone,
                        "photoUrl" to photoUrl,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Ignore remote errors if offline
            }
        }

        return Result.success(updated)
    }

    override fun getLocalProfile(): Flow<UserProfileEntity?> {
        return userProfileDao.getProfile()
    }
}
