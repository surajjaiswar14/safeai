package com.example.safejourneyai.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safejourneyai.data.local.AppDatabase
import com.example.safejourneyai.data.local.DataStoreManager
import com.example.safejourneyai.data.local.entities.UserProfileEntity
import com.example.safejourneyai.data.repository.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val destinationRepository = DestinationRepository(db.destinationDao())
    val userSettingsRepository = UserSettingsRepository(DataStoreManager(application))
    val authRepository: AuthRepository = AuthRepositoryImpl(db)

    val authState: StateFlow<AuthState> = authRepository.authState

    val localProfile: StateFlow<UserProfileEntity?> = authRepository.getLocalProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val onboardingCompleted: StateFlow<Boolean> = userSettingsRepository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = userSettingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val languageCode: StateFlow<String> = userSettingsRepository.languageCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val isLoggedIn: StateFlow<Boolean> = authState.map { state ->
        when (state) {
            is AuthState.Authenticated -> true
            else -> false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val userName: StateFlow<String> = authState.map { state ->
        when (state) {
            is AuthState.Authenticated -> state.profile.name
            else -> localProfile.value?.name ?: "Traveler"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Traveler")

    val tripModeEnabled: StateFlow<Boolean> = userSettingsRepository.tripModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val simulationModeEnabled: StateFlow<Boolean> = userSettingsRepository.simulationModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val simulationScenario: StateFlow<Int> = userSettingsRepository.simulationScenario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setOnboardingCompleted(completed)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userSettingsRepository.setThemeMode(mode)
        }
    }

    fun setLanguageCode(code: String) {
        viewModelScope.launch {
            userSettingsRepository.setLanguageCode(code)
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<UserProfileEntity> {
        val result = authRepository.signUpWithEmail(name, email, pass)
        result.onSuccess {
            userSettingsRepository.setLoggedIn(true, it.name)
        }
        return result
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfileEntity> {
        val result = authRepository.signInWithEmail(email, pass)
        result.onSuccess {
            userSettingsRepository.setLoggedIn(true, it.name)
        }
        return result
    }

    suspend fun signInWithGoogle(idToken: String): Result<UserProfileEntity> {
        val result = authRepository.signInWithGoogle(idToken)
        result.onSuccess {
            userSettingsRepository.setLoggedIn(true, it.name)
        }
        return result
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return authRepository.sendPasswordReset(email)
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            val guest = authRepository.signInAsGuest()
            userSettingsRepository.setLoggedIn(true, guest.name)
        }
    }

    fun updateProfile(name: String, email: String, phone: String, photoUrl: String = "") {
        viewModelScope.launch {
            authRepository.updateProfile(name, email, phone, photoUrl)
            userSettingsRepository.setLoggedIn(true, name)
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            authRepository.signOut()
            userSettingsRepository.setLoggedIn(false, "Traveler")
        }
    }

    fun setTripModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setTripModeEnabled(enabled)
        }
    }

    fun setSimulationModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setSimulationModeEnabled(enabled)
        }
    }

    fun setSimulationScenario(scenario: Int) {
        viewModelScope.launch {
            userSettingsRepository.setSimulationScenario(scenario)
        }
    }
}
