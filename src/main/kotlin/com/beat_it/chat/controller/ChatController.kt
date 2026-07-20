package com.beat_it.chat.controller

import com.beat_it.chat.dto.ChatMessageRequest
import com.beat_it.chat.dto.ChatRoomCreateRequest
import com.beat_it.chat.dto.ChatRoomCreateResponse
import com.beat_it.chat.dto.ChatRoomDetailResponse
import com.beat_it.chat.dto.ChatRoomListResponse
import com.beat_it.chat.dto.ChatRoomUpdateRequest
import com.beat_it.chat.dto.ChatRoomUpdateResponse
import com.beat_it.chat.service.ChatService
import com.beat_it.global.response.BasicResponse
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chatrooms")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChatRoom(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: ChatRoomCreateRequest
    ): ResponseEntity<BasicResponse<ChatRoomCreateResponse>> {

        val currentUserId = userDetails.username.toLongOrNull()
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

    @PostMapping("/{chatId}/messages", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun sendMessage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable chatId: Long,
        @ModelAttribute request: ChatMessageRequest,
    ): ResponseEntity<Any> {

        val currentUserId = userDetails.username.toLongOrNull()
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

    @PatchMapping("/{chatId}")
    fun updateChatRoomName(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable chatId: Long,
        @RequestBody request: ChatRoomUpdateRequest
    ): ResponseEntity<BasicResponse<ChatRoomUpdateResponse>> {

        val currentUserId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = chatService.updateChatRoomName(chatId, currentUserId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                BasicResponse.success(
                    responseData,
                    HttpStatus.OK,
                    "채팅방 이름이 수정되었습니다."
                )
            )
    }

    @GetMapping("/{chatId}/messages")
    fun getChatRoomDetails(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable chatId: Long,
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "size", defaultValue = "30") size: Int
    ): ResponseEntity<BasicResponse<ChatRoomDetailResponse>> {

        val currentUserId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = chatService.getChatRoomDetails(chatId, currentUserId, page, size)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                BasicResponse.success(
                    responseData,
                    HttpStatus.OK,
                    "메시지 내역을 성공적으로 불러왔습니다."
                )
            )
    }

    @GetMapping
    fun getChatRooms(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BasicResponse<ChatRoomListResponse>> {

        val currentUserId = userDetails.username.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)

        val responseData = chatService.getChatRooms(currentUserId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                BasicResponse.success(
                    responseData,
                    HttpStatus.OK,
                    "채팅방 목록을 성공적으로 불러왔습니다."
                )
            )
    }
}