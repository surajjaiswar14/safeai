package com.example.safejourneyai.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.safejourneyai.data.local.AppDatabase
import com.example.safejourneyai.data.model.ChatMessage
import com.example.safejourneyai.data.model.Destination
import com.example.safejourneyai.data.repository.AIAssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AIAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AIAssistantRepository(db)

    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val suggestedQuestions = repository.suggestedQuestions

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun sendMessage(query: String, destinationContext: Destination? = null) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isTyping.value = true
            repository.sendMessage(query, destinationContext)
            _isTyping.value = false
        }
    }
}
