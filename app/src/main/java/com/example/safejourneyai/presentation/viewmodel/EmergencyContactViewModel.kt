package com.example.safejourneyai.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safejourneyai.data.local.AppDatabase
import com.example.safejourneyai.data.local.entities.EmergencyContactEntity
import com.example.safejourneyai.data.repository.EmergencyContactRepository
import com.example.safejourneyai.data.repository.EmergencyContactRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyContactViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository: EmergencyContactRepository = EmergencyContactRepositoryImpl(db)

    private val _currentUserId = MutableStateFlow("")

    val contacts: StateFlow<List<EmergencyContactEntity>> = _currentUserId.flatMapLatest { userId ->
        repository.getEmergencyContacts(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    fun addContact(name: String, phone: String, relationship: String) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            val contact = EmergencyContactEntity(
                name = name.trim(),
                type = relationship.ifBlank { "Personal" },
                phoneNumber = phone.trim(),
                userId = _currentUserId.value
            )
            repository.addEmergencyContact(_currentUserId.value, contact)
        }
    }

    fun updateContact(id: Long, name: String, phone: String, relationship: String) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            val contact = EmergencyContactEntity(
                id = id,
                name = name.trim(),
                type = relationship.ifBlank { "Personal" },
                phoneNumber = phone.trim(),
                userId = _currentUserId.value
            )
            repository.updateEmergencyContact(_currentUserId.value, contact)
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            repository.deleteEmergencyContact(_currentUserId.value, id)
        }
    }
}
