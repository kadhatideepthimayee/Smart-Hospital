package com.example.smarthospitalqueue.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthospitalqueue.data.model.ChatMessage
import com.example.smarthospitalqueue.data.model.MessageType
import com.example.smarthospitalqueue.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            "Hello! I'm Smart Health Assistant. I can help with health information, appointment guidance, hospital services, and wellness tips. How may I assist you today?",
            MessageType.AI
        )
    ))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = ChatMessage(content, MessageType.USER)
        _messages.value = _messages.value + userMessage
        
        _isLoading.value = true
        viewModelScope.launch {
            repository.sendMessage(content).collect { aiMessage ->
                _messages.value = _messages.value + aiMessage
                _isLoading.value = false
            }
        }
    }
}
