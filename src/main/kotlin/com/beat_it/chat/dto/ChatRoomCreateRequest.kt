package com.beat_it.chat.dto

data class ChatRoomCreateRequest(
    val roomName: String?,
    val participantIds: List<Long>,
    val firstMessageContent: String
)