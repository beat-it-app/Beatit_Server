package com.beat_it.chat.service

import com.beat_it.chat.dto.ChatRoomCreateRequest
import com.beat_it.chat.dto.ChatRoomCreateResponse
import com.beat_it.chat.entity.ChatMember
import com.beat_it.chat.entity.ChatRoom
import com.beat_it.chat.entity.ChatRoomType
import com.beat_it.chat.event.ChatRoomCreatedEvent
import com.beat_it.chat.repository.ChatRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.team.service.TeamService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatRoomService(
    private val chatRepository: ChatRepository,
    private val teamService: TeamService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createChatRoom(request: ChatRoomCreateRequest, currentUserId: Long): ChatRoomCreateResponse {
        //Todo: 팀 id 받아오기
        val teamId: Long = 1L

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
        savedChatRoom.members.addAll(chatMembers)

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
}