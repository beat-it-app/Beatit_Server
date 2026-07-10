package com.beat_it.chat.dto

data class ChatRoomCreateResponse(
    val chatId: Long,
    val roomName: String,
    val createdAt: String
)