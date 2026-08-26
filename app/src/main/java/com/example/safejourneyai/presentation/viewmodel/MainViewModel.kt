package com.example.safejourneyai.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safejourneyai.data.local.AppDatabase
import com.example.safejourneyai.data.local.DataStoreManager
import com.example.safejourneyai.data.repository.DestinationRepository
import com.example.safejourneyai.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val destinationRepository = DestinationRepository(db.destinationDao())
    val userSettingsRepository = UserSettingsRepository(DataStoreManager(application))

    val onboardingCompleted: StateFlow<Boolean> = userSettingsRepository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = userSettingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val languageCode: StateFlow<String> = userSettingsRepository.languageCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val isLoggedIn: StateFlow<Boolean> = userSettingsRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userName: StateFlow<String> = userSettingsRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Traveler")

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

    fun loginUser(name: String) {
        viewModelScope.launch {
            userSettingsRepository.setLoggedIn(true, name)
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
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
