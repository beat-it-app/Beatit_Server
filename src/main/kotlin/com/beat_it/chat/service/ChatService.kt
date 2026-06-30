package com.beat_it.chat.service

import com.beat_it.auth.service.UserService
import com.beat_it.chat.dto.ChatMessageDetailResponse
import com.beat_it.chat.dto.ChatMessageRequest
import com.beat_it.chat.dto.ChatRoomCreateRequest
import com.beat_it.chat.dto.ChatRoomCreateResponse
import com.beat_it.chat.entity.ChatMember
import com.beat_it.chat.entity.ChatMessage
import com.beat_it.chat.entity.ChatMessageType
import com.beat_it.chat.entity.ChatRoom
import com.beat_it.chat.entity.ChatRoomType
import com.beat_it.chat.event.ChatRoomCreatedEvent
import com.beat_it.chat.repository.ChatMessageRepository
import com.beat_it.chat.repository.ChatRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.team.service.TeamService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val teamService: TeamService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val userService: UserService
) {

    @Transactional
    fun createChatRoom(request: ChatRoomCreateRequest, currentUserId: Long): ChatRoomCreateResponse {

        val teamId = userService.getCurrentTeamId(currentUserId)

        val allParticipantIds = request.participantIds.toMutableList().apply {
            if (!contains(currentUserId)) add(currentUserId)
        }

        val isValidTeamMembers = teamService.validateMembersInTeam(teamId, allParticipantIds)
        if (!isValidTeamMembers) {
            throw BusinessException(ErrorCode.INVALID_TEAM_PARTICIPANTS)
        }

        val roomType = if (request.participantIds.size == 1) ChatRoomType.DIRECT else ChatRoomType.GROUP

        if (request.roomName.isNullOrBlank()) {
            throw BusinessException(ErrorCode.CHAT_ROOM_NAME_REQUIRED)
        }

        val chatRoom = ChatRoom(
            teamId = teamId,
            title = request.roomName,
            type = roomType
        )

        val chatMembers = allParticipantIds.map { userId ->
            ChatMember(chatRoom = chatRoom, userId = userId)
        }
        chatRoom.members.addAll(chatMembers)

        val savedChatRoom = chatRepository.saveAndFlush(chatRoom)

        val firstMessage = ChatMessage(
            chatRoom = savedChatRoom,
            senderId = currentUserId,
            content = request.firstMessageContent,
            type = ChatMessageType.TEXT
        )
        val savedMessage = chatMessageRepository.saveAndFlush(firstMessage)

        val messageDetail = ChatMessageDetailResponse(
            messageId = savedMessage.id!!,
            chatId = savedChatRoom.id!!,
            senderId = currentUserId,
            content = savedMessage.content,
            messageType = savedMessage.type.name,
            createdAt = DateTimeUtil.format(savedMessage.createdAt)
        )

        val payload = objectMapper.writeValueAsString(messageDetail)
        kafkaTemplate.send("chat-topic", payload)

        applicationEventPublisher.publishEvent(
            ChatRoomCreatedEvent(
                chatId = savedChatRoom.id!!,
                roomName = savedChatRoom.title,
                participantIds = allParticipantIds
            )
        )

        return ChatRoomCreateResponse.of(
            chatId = savedChatRoom.id!!,
            roomName = savedChatRoom.title,
            createdAt = savedChatRoom.createdAt
        )
    }

    @Transactional
    fun sendMessage(chatId: Long, senderId: Long, request: ChatMessageRequest): ChatMessageDetailResponse {
        val chatRoom = chatRepository.findById(chatId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        val chatMessage = ChatMessage(
            chatRoom = chatRoom,
            senderId = senderId,
            content = request.content,
            type = ChatMessageType.valueOf(request.messageType)
        )
        val savedMessage = chatMessageRepository.saveAndFlush(chatMessage)

        val messageDetail = ChatMessageDetailResponse(
            messageId = savedMessage.id!!,
            chatId = chatId,
            senderId = senderId,
            content = savedMessage.content,
            messageType = savedMessage.type.name,
            createdAt = DateTimeUtil.format(savedMessage.createdAt)
        )

        val payload = objectMapper.writeValueAsString(messageDetail)
        kafkaTemplate.send("chat-topic", payload)

        return messageDetail
    }
}