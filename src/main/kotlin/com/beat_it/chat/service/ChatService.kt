package com.beat_it.chat.service

import com.beat_it.auth.service.UserService
import com.beat_it.chat.dto.ChatMessageDetailResponse
import com.beat_it.chat.dto.ChatMessageRequest
import com.beat_it.chat.dto.ChatRoomCreateRequest
import com.beat_it.chat.dto.ChatRoomCreateResponse
import com.beat_it.chat.dto.ChatRoomDetailResponse
import com.beat_it.chat.dto.ChatRoomUpdateRequest
import com.beat_it.chat.dto.ChatRoomUpdateResponse
import com.beat_it.chat.dto.GetChatMessageQueryResponse
import com.beat_it.chat.entity.ChatFiles
import com.beat_it.chat.entity.ChatMember
import com.beat_it.chat.entity.ChatMessage
import com.beat_it.chat.entity.ChatMessageFiles
import com.beat_it.chat.entity.ChatMessageType
import com.beat_it.chat.entity.ChatRoom
import com.beat_it.chat.entity.enum.ChatRoomType
import com.beat_it.chat.entity.MediaCategory
import com.beat_it.chat.event.ChatRoomCreatedEvent
import com.beat_it.chat.repository.ChatFilesRepository
import com.beat_it.chat.repository.ChatMessageFilesRepository
import com.beat_it.chat.repository.ChatMessageRepository
import com.beat_it.chat.repository.ChatRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.team.service.TeamService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatFilesRepository: ChatFilesRepository,
    private val chatMessageFilesRepository: ChatMessageFilesRepository,
    private val teamService: TeamService,
    private val fileService: FileService,
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

        validateChatRoomNameNotBlank(request.roomName)

        val chatRoom = ChatRoom(
            teamId = teamId,
            title = request.roomName!!,
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
            messageId = savedMessage.chatMessageId!!,
            chatId = savedChatRoom.chatId!!,
            senderId = currentUserId,
            content = savedMessage.content,
            messageType = savedMessage.type.name,
            createdAt = DateTimeUtil.format(savedMessage.createdAt)
        )

        val payload = objectMapper.writeValueAsString(messageDetail)
        kafkaTemplate.send("chat-topic", payload)

        applicationEventPublisher.publishEvent(
            ChatRoomCreatedEvent(
                chatId = savedChatRoom.chatId!!,
                roomName = savedChatRoom.title,
                participantIds = allParticipantIds
            )
        )

        return ChatRoomCreateResponse(
            chatId = savedChatRoom.chatId!!,
            roomName = savedChatRoom.title,
            createdAt = DateTimeUtil.format(savedChatRoom.createdAt)
        )
    }

    @Transactional
    fun sendMessage(chatId: Long, senderId: Long, request: ChatMessageRequest): ChatMessageDetailResponse {
        val chatRoom = chatRepository.findById(chatId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        val type = try {
            ChatMessageType.valueOf(request.messageType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BusinessException(ErrorCode.INVALID_MESSAGE_TYPE)
        }

        var savedChatFile: ChatFiles? = null

        val finalContent = if (request.file != null) {
            val uploadedResult = fileService.uploadFiles(listOf(request.file), "chats/$chatId").first()

            val chatFile = ChatFiles(
                userId = senderId,
                originalFileName = uploadedResult.originalFileName,
                storageKey = uploadedResult.storageKey,
                cdnUrl = uploadedResult.cdnUrl,
                mediaCategory = when (type) {
                    ChatMessageType.IMAGE -> MediaCategory.IMAGE
                    ChatMessageType.VIDEO -> MediaCategory.VIDEO
                    ChatMessageType.FILE -> MediaCategory.DOCUMENT
                    else -> MediaCategory.AUDIO
                },
                isPublic = true
            )
            savedChatFile = chatFilesRepository.save(chatFile)

            uploadedResult.cdnUrl
        } else {
            if (type != ChatMessageType.TEXT) {
                throw BusinessException(ErrorCode.FILE_REQUIRED)
            }
            request.content ?: throw BusinessException(ErrorCode.CHAT_MESSAGE_REQUIRED)
        }

        val chatMessage = ChatMessage(
            chatRoom = chatRoom,
            senderId = senderId,
            content = finalContent,
            type = type
        )
        val savedMessage = chatMessageRepository.saveAndFlush(chatMessage)

        if (savedChatFile != null) {
            val chatMessageFile = ChatMessageFiles(
                userId = senderId,
                chatMessage = savedMessage,
                chatFile = savedChatFile
            )
            chatMessageFilesRepository.save(chatMessageFile)
        }

        val messageDetail = ChatMessageDetailResponse(
            messageId = savedMessage.chatMessageId!!,
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

    @Transactional
    fun updateChatRoomName(chatId: Long, userId: Long, request: ChatRoomUpdateRequest): ChatRoomUpdateResponse {
        val chatRoom = chatRepository.findById(chatId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        validateChatRoomMember(chatRoom, userId)

        validateChatRoomNameNotBlank(request.roomName)

        chatRoom.title = request.roomName
        val updatedChatRoom = chatRepository.saveAndFlush(chatRoom)

        return ChatRoomUpdateResponse(
            chatId = updatedChatRoom.chatId!!,
            roomName = updatedChatRoom.title,
            updatedAt = DateTimeUtil.format(updatedChatRoom.updatedAt)
        )
    }

    @Transactional(readOnly = true)
    fun getChatRoomDetails(chatId: Long, currentUserId: Long, page: Int, size: Int): ChatRoomDetailResponse {
        val chatRoom = chatRepository.findById(chatId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        validateChatRoomMember(chatRoom, currentUserId)

        val pageable = PageRequest.of(page, size)
        val messageSlice = chatMessageRepository.findByChatRoomChatIdOrderByChatMessageIdDesc(chatId, pageable)

        val senderIds = messageSlice.content.map { it.senderId }.distinct()

        val userProfileMap = userService.getUserProfiles(senderIds)
            .associateBy { it.userId }

        val messageResponses = messageSlice.content.map { message ->
            val profile = userProfileMap[message.senderId]
            val senderName = profile?.name ?: "알 수 없는 사용자"
            val profileImageUrl = profile?.profileImageUrl

            GetChatMessageQueryResponse.of(
                messageId = message.chatMessageId!!,
                senderId = message.senderId,
                senderName = senderName,
                profileImageUrl = profileImageUrl,
                content = message.content,
                messageType = message.type.name,
                createdAt = DateTimeUtil.format(message.createdAt), // DateTimeUtil 활용 일관성 유지
                isMine = (message.senderId == currentUserId)
            )
        }.reversed()

        return ChatRoomDetailResponse.of(
            chatroomName = chatRoom.title,
            participantCount = chatRoom.members.size,
            messages = messageResponses,
            hasNext = messageSlice.hasNext()
        )
    }

    private fun validateChatRoomMember(chatRoom: ChatRoom, userId: Long) {
        val isMember = chatRoom.members.any { it.userId == userId }
        if (!isMember) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateChatRoomNameNotBlank(roomName: String?) {
        if (roomName.isNullOrBlank()) {
            throw BusinessException(ErrorCode.CHAT_ROOM_NAME_REQUIRED)
        }
    }
}