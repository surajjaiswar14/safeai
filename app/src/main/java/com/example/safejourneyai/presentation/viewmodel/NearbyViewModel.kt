package com.example.safejourneyai.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safejourneyai.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NearbyUiState {
    object Loading : NearbyUiState()
    object PermissionDenied : NearbyUiState()
    data class Success(
        val items: List<NearbyHelpItem>,
        val userLocationName: String
    ) : NearbyUiState()
    data class Error(val message: String) : NearbyUiState()
}

class NearbyViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository: LocationRepository = LocationRepositoryImpl(application)
    private val nearbyRepository: NearbyRepository = NearbyRepositoryImpl()

    private val _uiState = MutableStateFlow<NearbyUiState>(NearbyUiState.Loading)
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        loadNearbyData()
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun loadNearbyData() {
        viewModelScope.launch {
            if (!locationRepository.hasLocationPermission()) {
                _uiState.value = NearbyUiState.PermissionDenied
                return@launch
            }

            _uiState.value = NearbyUiState.Loading

            val locState = locationRepository.fetchCurrentLocation()
            if (locState is LocationState.PermissionDenied) {
                _uiState.value = NearbyUiState.PermissionDenied
                return@launch
            }

            val userLoc = when (locState) {
                is LocationState.Success -> locState.location
                else -> UserLocation(latitude = 19.0760, longitude = 72.8777, city = "Mumbai", state = "Maharashtra")
            }

            val result = nearbyRepository.getNearbyHelp(userLoc.latitude, userLoc.longitude, userLoc.city)
            result.onSuccess { items ->
                if (items.isEmpty()) {
                    _uiState.value = NearbyUiState.Error("No nearby emergency services found within 5km.")
                } else {
                    _uiState.value = NearbyUiState.Success(
                        items = items,
                        userLocationName = userLoc.getDisplayName()
                    )
                }
            }.onFailure { err ->
                _uiState.value = NearbyUiState.Error(err.localizedMessage ?: "Failed to load live nearby services.")
            }
        }
    }
}
