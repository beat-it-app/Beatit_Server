package com.beat_it.chat.event

data class ChatRoomCreatedEvent(
    val chatId: Long,
    val roomName: String,
    val participantIds: List<Long>
)