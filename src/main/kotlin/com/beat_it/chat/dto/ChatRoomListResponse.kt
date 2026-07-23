package com.beat_it.chat.dto

data class ChatRoomListResponse(
    val chatroomList: List<ChatRoomSummaryDto>
)

data class ChatRoomSummaryDto(
    val chatId: Long,
    val roomName: String,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int,
    val profileImage: List<String>,
    val participantCount: Int
)