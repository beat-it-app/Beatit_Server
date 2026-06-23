package com.beat_it.chat.dto


data class ChatMessageRequest(
    val messageType: String,
    val content: String
)