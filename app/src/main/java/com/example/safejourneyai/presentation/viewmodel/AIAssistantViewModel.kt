package com.example.safejourneyai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safejourneyai.data.model.ChatMessage
import com.example.safejourneyai.data.repository.AIAssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AIAssistantViewModel : ViewModel() {

    private val repository = AIAssistantRepository()

    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val suggestedQuestions = repository.suggestedQuestions

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun sendMessage(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isTyping.value = true
            repository.sendMessage(query)
            _isTyping.value = false
        }
    }
}
