package com.example.smarthospitalqueue.data.model

import java.util.Date

enum class MessageType {
    USER, AI, ERROR
}

data class ChatMessage(
    val content: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)
