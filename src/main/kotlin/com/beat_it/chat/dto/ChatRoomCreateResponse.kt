package com.beat_it.chat.dto

import java.time.OffsetDateTime

data class ChatRoomCreateResponse(
    val chatId: Long,
    val roomName: String,
    val createdAt: OffsetDateTime
)