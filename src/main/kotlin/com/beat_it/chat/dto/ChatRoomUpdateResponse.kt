package com.beat_it.chat.dto

data class ChatRoomUpdateResponse(
    val chatId: Long,
    val roomName: String,
    val updatedAt: String
)