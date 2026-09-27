package com.beat_it.chat.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.multipart.MultipartFile


data class ChatMessageRequest(
    val messageType: String,
    val content: String?,
    val storageKey: String? = null
)