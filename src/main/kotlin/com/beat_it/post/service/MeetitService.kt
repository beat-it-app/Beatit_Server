package com.beat_it.post.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.post.dto.meetit.*
import com.beat_it.post.entity.meetit.Meetit
import com.beat_it.post.entity.meetit.MeetitParticipant
import com.beat_it.post.entity.meetit.MeetitResponse
import com.beat_it.post.repository.meetit.MeetitParticipantRepository
import com.beat_it.post.repository.meetit.MeetitRepository
import com.beat_it.post.repository.meetit.MeetitResponseRepository
import com.beat_it.team.repository.TeamMembershipRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class MeetitService(
    private val userService: UserService,
    private val meetitRepository: MeetitRepository,
    private val meetitParticipantRepository: MeetitParticipantRepository,
    private val meetitResponseRepository: MeetitResponseRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun createMeetit(userId: Long, request: MeetitCreateRequest) {
        if (request.participantUserIds.size < 2) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
        if (request.candidateDates.isEmpty()) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
        if (!request.dateOnly) {
            if (request.startTime == null || request.endTime == null) {
                throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
            }
            validateTime30MinInterval(request.startTime)
            validateTime30MinInterval(request.endTime)
        } else {
            if (request.startTime != null || request.endTime != null) {
                throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
            }
        }

        val teamId = userService.getCurrentTeamId(userId)

        val activeMemberships = teamMembershipRepository.findAllByTeamTeamIdAndUserIdInAndLeftAtIsNull(
            teamId = teamId,
            userIds = request.participantUserIds
        )
        val activeMemberUserIds = activeMemberships.map { it.userId }.toSet()
        if (activeMemberUserIds.size != request.participantUserIds.distinct().size) {
            throw BusinessException(ErrorCode.INVALID_TEAM_PARTICIPANTS)
        }

        val meetit = Meetit(
            userId = userId,
            teamId = teamId,
            title = request.title,
            candidateDates = objectMapper.writeValueAsString(request.candidateDates),
            startTime = if (request.dateOnly) null else request.startTime,
            endTime = if (request.dateOnly) null else request.endTime,
            dateOnly = request.dateOnly,
            totalInvitedCount = request.participantUserIds.size
        )
        val savedMeetit = meetitRepository.save(meetit)

        val profiles = userService.getUserProfiles(request.participantUserIds)
        val profileMap = profiles.associateBy { it.userId }

        val participants = request.participantUserIds.map { participantUserId ->
            val profileName = profileMap[participantUserId]?.name ?: "이름 없음"
            MeetitParticipant(
                meetit = savedMeetit,
                userId = participantUserId,
                name = profileName
            )
        }
        meetitParticipantRepository.saveAll(participants)
    }

    @Transactional(readOnly = true)
    fun getMeetitList(userId: Long, page: Int = 0, size: Int = 10): MeetitListResponse {
        val teamId = userService.getCurrentTeamId(userId)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val meetitsPage = meetitRepository.findByTeamId(teamId, pageable)

        val meetitList = meetitsPage.content.map { meetit ->
            val meetitId = meetit.meetitId!!
            val participants = meetitParticipantRepository.findByMeetitMeetitId(meetitId)
            val myParticipant = participants.find { it.userId == userId }

            val responses = meetitResponseRepository.findByMeetitMeetitId(meetitId)
            val respondedParticipantIds = responses.map { it.meetitParticipant.meetitParticipantId }.toSet()

            val myResponseStatus = when {
                myParticipant == null -> MyResponseStatus.NOT_PARTICIPANT
                respondedParticipantIds.contains(myParticipant.meetitParticipantId) -> MyResponseStatus.RESPONDED
                else -> MyResponseStatus.NOT_RESPONDED
            }

            val respondedCount = respondedParticipantIds.size

            MeetitListItemResponse(
                meetitId = meetitId,
                title = meetit.title,
                totalInvitedCount = meetit.totalInvitedCount,
                respondedCount = respondedCount,
                myResponseStatus = myResponseStatus,
                dateOnly = meetit.dateOnly
            )
        }

        return MeetitListResponse(
            meetitList = meetitList,
            totalCount = meetitsPage.totalElements.toInt(),
            hasNext = meetitsPage.hasNext()
        )
    }

    @Transactional(readOnly = true)
    fun getMeetitDetail(userId: Long, meetitId: Long, filter: String, userIds: List<Long>?): MeetitDetailResponse {
        val meetit = meetitRepository.findById(meetitId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }

        val teamId = userService.getCurrentTeamId(userId)
        if (meetit.teamId != teamId) {
            throw BusinessException(ErrorCode.MEETIT_TEAM_MISMATCH)
        }

        val participants = meetitParticipantRepository.findByMeetitMeetitId(meetitId)
        val responses = meetitResponseRepository.findByMeetitMeetitId(meetitId)
        val respondedParticipantIds = responses.map { it.meetitParticipant.meetitParticipantId }.toSet()

        val participantUserIds = participants.map { it.userId }
        val profilesMap = userService.getUserSimpleInfos(participantUserIds)

        val respondedParticipantsInfo = participants.filter { p ->
            respondedParticipantIds.contains(p.meetitParticipantId)
        }.map { p ->
            val profile = profilesMap[p.userId]
            MeetitParticipantInfoResponse(
                userId = p.userId,
                name = profile?.userName ?: p.name
            )
        }

        val candidateDates = objectMapper.readValue<List<String>>(meetit.candidateDates)

        val systemZone = ZoneId.systemDefault()
        val allSlotStartToUsers = mutableMapOf<OffsetDateTime, MutableList<Long>>()

        for (dateStr in candidateDates) {
            val date = LocalDate.parse(dateStr)
            if (meetit.dateOnly) {
                val offsetTime = date.atTime(LocalTime.MIN).atZone(systemZone).toOffsetDateTime()
                allSlotStartToUsers[offsetTime] = mutableListOf()
            } else {
                var current = date.atTime(meetit.startTime!!)
                val end = date.atTime(meetit.endTime!!)
                while (current.isBefore(end)) {
                    val offsetTime = current.atZone(systemZone).toOffsetDateTime()
                    allSlotStartToUsers[offsetTime] = mutableListOf()
                    current = current.plusMinutes(30)
                }
            }
        }

        for (resp in responses) {
            val respInstant = resp.slotStartTime.toInstant()
            val matchedSlot = allSlotStartToUsers.keys.find { it.toInstant() == respInstant }
            if (matchedSlot != null) {
                allSlotStartToUsers[matchedSlot]?.add(resp.meetitParticipant.userId)
            }
        }

        val allTimetableGrid = allSlotStartToUsers.map { (slotTime, userIds) ->
            MeetitGridSlotResponse(slotStartTime = slotTime, availableUserIds = userIds)
        }.sortedBy { it.slotStartTime }

        val totalCount = participants.size
        val maxOverlappingCount = allTimetableGrid.maxOfOrNull { it.availableUserIds.size } ?: 0

        val entireMemberOptimalSlots = findOptimalIntervals(allTimetableGrid, totalCount, meetit.dateOnly)
        val maxMemberOptimalSlots = findOptimalIntervals(allTimetableGrid, maxOverlappingCount, meetit.dateOnly)

        val timetableGrid = if (userIds != null && userIds.isNotEmpty()) {
            allTimetableGrid.map { slot ->
                MeetitGridSlotResponse(
                    slotStartTime = slot.slotStartTime,
                    availableUserIds = slot.availableUserIds.filter { userIds.contains(it) }
                )
            }
        } else if (filter == "OPTIMAL") {
            allTimetableGrid.map { slot ->
                MeetitGridSlotResponse(
                    slotStartTime = slot.slotStartTime,
                    availableUserIds = if (slot.availableUserIds.size >= maxOverlappingCount && maxOverlappingCount > 0) {
                        slot.availableUserIds
                    } else {
                        emptyList()
                    }
                )
            }
        } else {
            allTimetableGrid
        }

        val isParticipant = participantUserIds.contains(userId)

        return MeetitDetailResponse(
            meetitId = meetit.meetitId!!,
            title = meetit.title,
            creatorId = meetit.userId,
            startTime = meetit.startTime?.let { formatTime(it) },
            endTime = meetit.endTime?.let { formatTime(it) },
            dateOnly = meetit.dateOnly,
            candidateDates = candidateDates,
            totalInvitedCount = totalCount,
            respondedCount = respondedParticipantIds.size,
            isParticipant = isParticipant,
            respondedParticipants = respondedParticipantsInfo,
            entireMemberOptimalSlots = entireMemberOptimalSlots,
            maxMemberOptimalSlots = maxMemberOptimalSlots,
            maxOverlappingCount = maxOverlappingCount,
            timetableGrid = timetableGrid
        )
    }

    @Transactional
    fun respondMeetit(userId: Long, meetitId: Long, request: MeetitSubmissionRequest) {
        val participant = meetitParticipantRepository.findByMeetitMeetitIdAndUserId(meetitId, userId)
            ?: throw BusinessException(ErrorCode.MEETIT_NOT_PARTICIPANT)

        val meetit = participant.meetit
        val candidateDates = objectMapper.readValue<List<String>>(meetit.candidateDates).toSet()

        val systemZone = ZoneId.systemDefault()

        for (localDateTime in request.slotStartTimes) {
            val dateStr = localDateTime.toLocalDate().toString()
            if (!candidateDates.contains(dateStr)) {
                throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
            }

            val localTime = localDateTime.toLocalTime()
            if (!meetit.dateOnly) {
                if (localTime.isBefore(meetit.startTime!!) || !localTime.isBefore(meetit.endTime!!)) {
                    throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
                }
                validateTime30MinInterval(localTime)
            } else {
                if (localTime != LocalTime.MIN) {
                    throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
                }
            }
        }

        meetitResponseRepository.deleteByMeetitParticipantMeetitParticipantId(participant.meetitParticipantId!!)

        val newResponses = request.slotStartTimes.map { localDateTime ->
            MeetitResponse(
                meetit = meetit,
                meetitParticipant = participant,
                slotStartTime = localDateTime.atZone(systemZone).toOffsetDateTime()
            )
        }
        meetitResponseRepository.saveAll(newResponses)
    }

    private fun findOptimalIntervals(
        grid: List<MeetitGridSlotResponse>,
        threshold: Int,
        dateOnly: Boolean
    ): List<MeetitOptimalSlotResponse> {
        if (threshold <= 0) return emptyList()
        val result = mutableListOf<MeetitOptimalSlotResponse>()

        val slotsByDate = grid.filter { it.availableUserIds.size >= threshold }
            .groupBy { it.slotStartTime.toLocalDate() }

        for ((date, slots) in slotsByDate) {
            val sortedSlots = slots.sortedBy { it.slotStartTime }
            if (sortedSlots.isEmpty()) continue

            if (dateOnly) {
                result.add(
                    MeetitOptimalSlotResponse(
                        date = date.toString(),
                        startTime = null,
                        endTime = null
                    )
                )
                continue
            }

            var intervalStart = sortedSlots.first().slotStartTime
            var expectedNext = intervalStart.plusMinutes(30)

            for (i in 1 until sortedSlots.size) {
                val slot = sortedSlots[i]
                if (slot.slotStartTime == expectedNext) {
                    expectedNext = slot.slotStartTime.plusMinutes(30)
                } else {
                    result.add(
                        MeetitOptimalSlotResponse(
                            date = date.toString(),
                            startTime = formatTime(intervalStart.toLocalTime()),
                            endTime = formatTime(expectedNext.toLocalTime())
                        )
                    )
                    intervalStart = slot.slotStartTime
                    expectedNext = intervalStart.plusMinutes(30)
                }
            }
            result.add(
                MeetitOptimalSlotResponse(
                    date = date.toString(),
                    startTime = formatTime(intervalStart.toLocalTime()),
                    endTime = formatTime(expectedNext.toLocalTime())
                )
            )
        }
        return result.sortedWith(compareBy({ it.date }, { it.startTime ?: "" }))
    }

    private fun validateTime30MinInterval(time: LocalTime) {
        if (time.minute % 30 != 0 || time.second != 0 || time.nano != 0) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }

    private fun formatTime(time: LocalTime): String {
        return time.toString().substring(0, 5)
    }
}