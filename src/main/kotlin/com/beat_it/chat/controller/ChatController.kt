package com.beat_it.chat.controller

import com.beat_it.chat.dto.ChatRoomCreateRequest
import com.beat_it.chat.dto.ChatRoomCreateResponse
import com.beat_it.chat.service.ChatRoomService
import com.beat_it.global.response.BasicResponse
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chatrooms")
class ChatController(
    private val chatRoomService: ChatRoomService
) {

    @PostMapping
    fun createChatRoom(
        @AuthenticationPrincipal userDetails: UserDetails?,
        @RequestBody request: ChatRoomCreateRequest
    ): ResponseEntity<BasicResponse<ChatRoomCreateResponse>> {

        val currentUserId = userDetails?.username?.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = chatRoomService.createChatRoom(request, currentUserId)

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
}