package com.example.safejourneyai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val LANGUAGE_CODE = stringPreferencesKey("language_code") // "en", "hi", "mr"
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val TRIP_MODE_ENABLED = booleanPreferencesKey("trip_mode_enabled")
        val SIMULATION_MODE_ENABLED = booleanPreferencesKey("simulation_mode_enabled")
        val SIMULATION_SCENARIO = intPreferencesKey("simulation_scenario")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val languageCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_CODE] ?: "en"
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME] ?: "Traveler"
    }

    val tripModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TRIP_MODE_ENABLED] ?: false
    }

    val simulationModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SIMULATION_MODE_ENABLED] ?: false
    }

    val simulationScenario: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SIMULATION_SCENARIO] ?: 0
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = code
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean, name: String = "Traveler") {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
            preferences[USER_NAME] = name
        }
    }

    suspend fun setTripModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TRIP_MODE_ENABLED] = enabled
        }
    }

    suspend fun setSimulationModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SIMULATION_MODE_ENABLED] = enabled
        }
    }

    suspend fun setSimulationScenario(scenario: Int) {
        context.dataStore.edit { preferences ->
            preferences[SIMULATION_SCENARIO] = scenario
        }
    }
}
