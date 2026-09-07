package com.beat_it.chat.dto

import java.time.OffsetDateTime

data class ChatRoomUpdateResponse(
    val chatId: Long,
    val roomName: String,
    val updatedAt: OffsetDateTime
)