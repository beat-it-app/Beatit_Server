package com.beat_it.chat.service

import com.beat_it.auth.service.UserService
import com.beat_it.chat.dto.ChatMessageDetailResponse
import com.beat_it.chat.dto.ChatMessageRequest
import com.beat_it.chat.dto.ChatRoomCreateRequest
import com.beat_it.chat.dto.ChatRoomCreateResponse
import com.beat_it.chat.dto.ChatRoomDetailResponse
import com.beat_it.chat.dto.ChatRoomListResponse
import com.beat_it.chat.dto.ChatRoomSummaryDto
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
import com.beat_it.chat.repository.ChatMemberRepository
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
    private val chatMemberRepository: ChatMemberRepository,
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
        val savedMessageId = savedMessage.chatMessageId!!

        val currentMember = chatMemberRepository.findByChatRoomChatIdAndUserId(chatId, senderId)
        if (currentMember != null) {
            if (currentMember.lastChatMessageId == null || currentMember.lastChatMessageId!! < savedMessageId) {
                currentMember.lastChatMessageId = savedMessageId
            }
        }

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

    @Transactional
    fun getChatRoomDetails(chatId: Long, currentUserId: Long, page: Int, size: Int): ChatRoomDetailResponse {
        val chatRoom = chatRepository.findById(chatId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }

        validateChatRoomMember(chatRoom, currentUserId)

        val currentMember = chatMemberRepository.findByChatRoomChatIdAndUserId(chatId, currentUserId)
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        val pageable = PageRequest.of(page, size)

        val messageSlice = if (currentMember.leftAt != null) {
            chatMessageRepository.findByChatRoomChatIdAndCreatedAtAfterOrderByChatMessageIdDesc(
                chatId = chatId,
                leftAt = currentMember.leftAt!!,
                pageable = pageable
            )
        } else {
            chatMessageRepository.findByChatRoomChatIdOrderByChatMessageIdDesc(chatId, pageable)
        }

        if (messageSlice.hasContent()) {
            val latestMessageId = messageSlice.content.first().chatMessageId!!
            if (currentMember.lastChatMessageId == null || currentMember.lastChatMessageId!! < latestMessageId) {
                currentMember.lastChatMessageId = latestMessageId
            }
        }

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

    @Transactional(readOnly = true)
    fun getChatRooms(currentUserId: Long): ChatRoomListResponse {
        val chatRooms = chatRepository.findByMembersUserId(currentUserId)

        val roomSummaries = chatRooms.map { chatRoom ->
            val chatId = chatRoom.chatId!!

            val currentMember = chatRoom.members.find { it.userId == currentUserId }

            val latestMessage = chatMessageRepository.findTopByChatRoomChatIdOrderByChatMessageIdDesc(chatId)

            val unreadCount = if (latestMessage == null) {
                0L
            } else {
                val lastReadId = currentMember?.lastChatMessageId
                if (lastReadId == null) {
                    chatMessageRepository.countAllUnreadMessages(chatId, currentUserId)
                } else {
                    chatMessageRepository.countUnreadMessages(chatId, lastReadId, currentUserId)
                }
            }

            val profileImages: List<String> = if (chatRoom.type == ChatRoomType.DIRECT) {
                val otherMember = chatRoom.members.find { it.userId != currentUserId }

                if (otherMember != null) {
                    val profiles = userService.getUserProfiles(listOf(otherMember.userId))
                    profiles.map { it.profileImageUrl }
                } else {
                    emptyList()
                }
            } else {
                val memberUserIds = chatRoom.members.map { it.userId }
                val userProfiles = userService.getUserProfiles(memberUserIds)

                userProfiles
                    .take(4)
                    .map { it.profileImageUrl }
            }

            ChatRoomSummaryDto(
                chatId = chatId,
                roomName = chatRoom.title,
                lastMessage = latestMessage?.content,
                lastMessageTime = latestMessage?.let { DateTimeUtil.format(it.createdAt) },
                unreadCount = unreadCount.toInt(),
                profileImage = profileImages,
                participantCount = chatRoom.members.size
            )
        }

        return ChatRoomListResponse(chatroomList = roomSummaries)
    }

    @Transactional
    fun updateLastReadMessage(chatId: Long, userId: Long, messageId: Long) {
        val member = chatMemberRepository.findByChatRoomChatIdAndUserId(chatId, userId)
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        if (member.lastChatMessageId == null || member.lastChatMessageId!! < messageId) {
            member.lastChatMessageId = messageId
        }
    }

    @Transactional
    fun leaveChatRoom(chatId: Long, userId: Long) {
        val chatRoom = findChatRoomOrThrow(chatId)
        val member = findChatMemberOrThrow(chatId, userId)

        if (chatRoom.type == ChatRoomType.DIRECT) {
            member.leftAt = OffsetDateTime.now()
        } else {

            val remainingCount = chatMemberRepository.countByChatRoomChatId(chatId)

            if (remainingCount <= 1L) {
                chatMemberRepository.delete(member)
                chatRepository.delete(chatRoom)
                return
            }

            chatMemberRepository.delete(member)

            val userProfile = userService.getUserProfiles(listOf(userId)).firstOrNull()
            val userName = userProfile?.name ?: "알 수 없는 사용자"

            val systemMessage = ChatMessage(
                chatRoom = chatRoom,
                senderId = userId,
                content = "${userName}님이 나갔습니다.",
                type = ChatMessageType.TEXT
            )

            val savedMessage = chatMessageRepository.saveAndFlush(systemMessage)

            val messageDetail = ChatMessageDetailResponse(
                messageId = savedMessage.chatMessageId!!,
                chatId = chatId,
                senderId = userId,
                content = savedMessage.content,
                messageType = savedMessage.type.name,
                createdAt = DateTimeUtil.format(savedMessage.createdAt)
            )
            val payload = objectMapper.writeValueAsString(messageDetail)
            kafkaTemplate.send("chat-topic", payload)
        }
    }

    private fun validateChatRoomMember(chatRoom: ChatRoom, userId: Long) {
        val chatId = chatRoom.chatId ?: throw BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND)
        val isMember = chatMemberRepository.existsByChatRoomChatIdAndUserId(chatId, userId)
        if (!isMember) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateChatRoomNameNotBlank(roomName: String?) {
        if (roomName.isNullOrBlank()) {
            throw BusinessException(ErrorCode.CHAT_ROOM_NAME_REQUIRED)
        }
    }

    private fun findChatRoomOrThrow(chatId: Long): ChatRoom {
        return chatRepository.findById(chatId)
            .orElseThrow { BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND) }
    }

    private fun findChatMemberOrThrow(chatId: Long, userId: Long): ChatMember {
        return chatMemberRepository.findByChatRoomChatIdAndUserId(chatId, userId)
            ?: throw BusinessException(ErrorCode.CHAT_MEMBER_NOT_FOUND)
    }
}