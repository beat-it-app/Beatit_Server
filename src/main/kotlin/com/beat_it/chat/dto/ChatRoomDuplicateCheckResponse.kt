package com.beat_it.chat.dto

import com.beat_it.chat.entity.enum.ChatRoomType

data class ChatRoomDuplicateCheckResponse(
    val exists: Boolean,
    val roomType: ChatRoomType,
    val existingChatIds: List<Long>
)