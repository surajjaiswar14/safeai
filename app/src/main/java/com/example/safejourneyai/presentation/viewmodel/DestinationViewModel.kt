package com.example.safejourneyai.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safejourneyai.data.local.AppDatabase
import com.example.safejourneyai.data.model.Destination
import com.example.safejourneyai.data.repository.DestinationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DestinationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DestinationRepository(AppDatabase.getDatabase(application).destinationDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val savedDestinations: StateFlow<List<Destination>> = repository.getSavedDestinations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlinePacks: StateFlow<List<Destination>> = repository.getOfflinePacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDestinations: StateFlow<List<Destination>> = combine(
        savedDestinations,
        offlinePacks,
        searchQuery,
        selectedCategory
    ) { saved, offline, query, category ->
        val savedIds = saved.map { it.id }.toSet()
        val offlineIds = offline.map { it.id }.toSet()

        repository.destinations.map { dest ->
            dest.copy(
                isSaved = savedIds.contains(dest.id),
                isDownloaded = offlineIds.contains(dest.id)
            )
        }.filter { dest ->
            val matchesQuery = query.isEmpty() || dest.name.contains(query, ignoreCase = true) || dest.state.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || dest.category.contains(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.destinations)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleSave(destinationId: String) {
        viewModelScope.launch {
            repository.toggleSaveDestination(destinationId)
        }
    }

    fun downloadOfflinePack(destination: Destination) {
        viewModelScope.launch {
            repository.downloadOfflinePack(destination)
        }
    }

    fun deleteOfflinePack(destinationId: String) {
        viewModelScope.launch {
            repository.deleteOfflinePack(destinationId)
        }
    }

    fun getDestinationById(id: String): Destination? {
        return repository.getDestinationById(id)
    }
}
