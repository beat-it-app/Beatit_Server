package com.beat_it.chat.controller

import com.beat_it.chat.dto.ChatMessageRequest
import com.beat_it.chat.dto.ChatRoomCreateRequest
import com.beat_it.chat.dto.ChatRoomCreateResponse
import com.beat_it.chat.service.ChatService
import com.beat_it.global.response.BasicResponse
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chatrooms")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChatRoom(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: ChatRoomCreateRequest
    ): ResponseEntity<BasicResponse<ChatRoomCreateResponse>> {

        val currentUserId = userDetails?.username?.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = chatService.createChatRoom(request, currentUserId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                BasicResponse.success(
                    responseData,
                    HttpStatus.CREATED,
                    "채팅방이 생성되었습니다."
                )
            )
    }

    @PostMapping("/{chatId}/messages")
    fun sendMessage(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @PathVariable chatId: Long,
        @RequestBody request: ChatMessageRequest,
    ): ResponseEntity<Any> {

        val currentUserId = userDetails?.username?.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val savedMessageDetail = chatService.sendMessage(chatId, currentUserId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                BasicResponse.success(
                    savedMessageDetail,
                    HttpStatus.CREATED,
                    "채팅 메세지가 전송되었습니다."
                )
            )
    }
}