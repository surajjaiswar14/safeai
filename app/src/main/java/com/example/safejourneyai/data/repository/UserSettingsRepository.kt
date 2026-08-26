package com.example.safejourneyai.data.repository

import com.example.safejourneyai.data.local.DataStoreManager
import kotlinx.coroutines.flow.Flow

class UserSettingsRepository(private val dataStoreManager: DataStoreManager) {

    val onboardingCompleted: Flow<Boolean> = dataStoreManager.onboardingCompleted
    val themeMode: Flow<String> = dataStoreManager.themeMode
    val languageCode: Flow<String> = dataStoreManager.languageCode
    val isLoggedIn: Flow<Boolean> = dataStoreManager.isLoggedIn
    val userName: Flow<String> = dataStoreManager.userName
    val tripModeEnabled: Flow<Boolean> = dataStoreManager.tripModeEnabled
    val simulationModeEnabled: Flow<Boolean> = dataStoreManager.simulationModeEnabled
    val simulationScenario: Flow<Int> = dataStoreManager.simulationScenario

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStoreManager.setOnboardingCompleted(completed)
    }

    suspend fun setThemeMode(mode: String) {
        dataStoreManager.setThemeMode(mode)
    }

    suspend fun setLanguageCode(code: String) {
        dataStoreManager.setLanguageCode(code)
    }

    suspend fun setLoggedIn(loggedIn: Boolean, name: String = "Traveler") {
        dataStoreManager.setLoggedIn(loggedIn, name)
    }

    suspend fun setTripModeEnabled(enabled: Boolean) {
        dataStoreManager.setTripModeEnabled(enabled)
    }

    suspend fun setSimulationModeEnabled(enabled: Boolean) {
        dataStoreManager.setSimulationModeEnabled(enabled)
    }

    suspend fun setSimulationScenario(scenario: Int) {
        dataStoreManager.setSimulationScenario(scenario)
    }
}
