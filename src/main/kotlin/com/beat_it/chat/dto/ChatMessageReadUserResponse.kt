package com.beat_it.chat.dto

data class ChatMessageReadUserResponse(
    val userId: Long,
    val name: String,
    val profileImageUrl: String?
)