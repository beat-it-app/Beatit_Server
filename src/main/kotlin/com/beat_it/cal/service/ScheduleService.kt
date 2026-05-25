package com.beat_it.cal.service

import com.beat_it.cal.dto.CalendarSchedule
import com.beat_it.cal.dto.CalendarSchedulesResponse
import com.beat_it.cal.dto.ParticipantResponse
import com.beat_it.cal.dto.ScheduleCreateRequest
import com.beat_it.cal.dto.ScheduleCreateResponse
import com.beat_it.cal.dto.ScheduleDetailResponse
import com.beat_it.cal.dto.ScheduleUpdateRequest
import com.beat_it.cal.entity.Schedule
import com.beat_it.cal.repository.ScheduleRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository
    // private val teamService: TeamService,
    // private val locationService: LocationService,
    // private val memberService: MemberService
) {

    @Transactional
    fun createSchedule(userId: Long, request: ScheduleCreateRequest): ScheduleCreateResponse {

        validateScheduleCommon(request.title, request.startsAt, request.endsAt)

        // TODO: 타 도메인 검증 (모듈 분리 대비)
        // teamService.validateTeam(1L)
        // request.locationId?.let { locationService.validateLocation(it) }



        val schedule = Schedule(
            teamId = 1L,
            userId = userId,
            title = request.title!!,
            content = request.content,
            locationId = request.locationId,
            startsAt = request.startsAt!!,
            endsAt = request.endsAt!!
        )

        request.participantUserIds.forEach { participantUserId ->
            // memberService.validateMember(participantUserId)
            schedule.addParticipant(participantUserId)
        }

        val savedSchedule = scheduleRepository.save(schedule)

        return ScheduleCreateResponse(
            scheduleId = savedSchedule.scheduleId!!,
            title = savedSchedule.title,
            startsAt = savedSchedule.startsAt,
            endsAt = savedSchedule.endsAt,
            createdAt = savedSchedule.createdAt
        )
    }

    @Transactional
    fun updateSchedule(scheduleId: Long, userId: Long, request: ScheduleUpdateRequest): ScheduleCreateResponse {

        val schedule = findScheduleOrThrow(scheduleId)

        validateScheduleOwner(schedule.userId, userId)

        if (isNotChanged(schedule, request)) {
            throw BusinessException(ErrorCode.CALENDAR_NO_CONTENT_TO_UPDATE)
        }

        validateScheduleCommon(request.title, request.startsAt, request.endsAt)

        // TODO: 타 도메인 검증 (모듈 분리 대비)
        // request.locationId?.let { locationService.validateLocation(it) }
        // request.participantUserIds?.let { memberService.validateMembers(it) }

        schedule.update(
            title = request.title!!,
            content = request.content,
            locationId = request.locationId,
            startsAt = request.startsAt!!,
            endsAt = request.endsAt!!
        )

        request.participantUserIds?.let { newIds ->
            schedule.participants.clear()
            newIds.forEach { participantId ->
                schedule.addParticipant(participantId)
            }
        }

        return ScheduleCreateResponse(
            scheduleId = schedule.scheduleId!!,
            title = schedule.title,
            startsAt = schedule.startsAt,
            endsAt = schedule.endsAt,
            createdAt = schedule.createdAt
        )
    }

    @Transactional
    fun deleteSchedule(scheduleId: Long, userId: Long) {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { BusinessException(ErrorCode.CALENDAR_NOT_FOUND) }

        validateScheduleOwner(schedule.userId, userId)

        scheduleRepository.delete(schedule)
    }

    @Transactional(readOnly = true)
    fun getScheduleDetail(scheduleId: Long): ScheduleDetailResponse {
        val schedule = findScheduleOrThrow(scheduleId)

        //TODO: 현재 내가 있는 팀 소속이 무엇인지 어떻게 넘겨받을 것인지
        //validateScheduleTeam(schedule.teamId, currentTeamId)

        return ScheduleDetailResponse(
            scheduleId = schedule.scheduleId!!,
            teamId = schedule.teamId,
            userId = schedule.userId,
            locationId = schedule.locationId,
            title = schedule.title,
            content = schedule.content,
            startsAt = schedule.startsAt,
            endsAt = schedule.endsAt,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt,
            participants = schedule.participants.map { participant ->
                ParticipantResponse(
                    scheduleParticipantId = participant.scheduleParticipantId!!,
                    userId = participant.userId
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun getCalendarSchedules(userId: Long, year: Int, month: Int): CalendarSchedulesResponse {
        // 1. 해당 월의 시작일(1일)과 종료일(말일) 계산
        val startLocalDate = LocalDate.of(year, month, 1)
        val endLocalDate = startLocalDate.withDayOfMonth(startLocalDate.lengthOfMonth())

        // 2. 쿼리에 사용할 범위 지정 (그 달의 1일 00:00:00 ~ 말일 23:59:59.999)
        // 프로젝트 타임존 컨벤션에 맞게 ZoneOffset을 조정하세요. (여기서는 KST인 +09:00 가정)
        val zoneOffset = ZoneOffset.ofHours(9)
        val startDateTime = OffsetDateTime.of(startLocalDate, LocalTime.MIN, zoneOffset)
        val endDateTime = OffsetDateTime.of(endLocalDate, LocalTime.MAX, zoneOffset)

        // 3. Repository를 통해 조건에 맞는 일정 조회 (기간 겹침 조건 적용)
        val schedules = scheduleRepository.findByUserIdAndMonthRange(userId, startDateTime, endDateTime)

        // 4. Entity 리스트를 DTO 리스트로 매핑하여 반환
        val calendarSchedules = schedules.map { schedule ->
            CalendarSchedule(
                scheduleId = schedule.scheduleId ?: throw IllegalStateException("일정 ID가 존재하지 않습니다."),
                title = schedule.title,
                startsAt = schedule.startsAt,
                endsAt = schedule.endsAt
            )
        }

        return CalendarSchedulesResponse(items = calendarSchedules)
    }

    private fun validateScheduleCommon(title: String?, startsAt: OffsetDateTime?, endsAt: OffsetDateTime?) {
        if (title.isNullOrBlank()) {
            throw BusinessException(ErrorCode.CALENDAR_TITLE_REQUIRED)
        }

        if (startsAt == null) {
            throw BusinessException(ErrorCode.CALENDAR_START_TIME_REQUIRED)
        }
        if (endsAt == null) {
            throw BusinessException(ErrorCode.CALENDAR_END_TIME_REQUIRED)
        }

        if (endsAt.isBefore(startsAt)) {
            throw BusinessException(ErrorCode.CALENDAR_INVALID_TIME_RANGE)
        }
    }

    private fun isNotChanged(schedule: Schedule, request: ScheduleUpdateRequest): Boolean {
        val isAnyFieldChanged =
            (request.title != null && request.title != schedule.title) ||
                    (request.content != null && request.content != schedule.content) ||
                    (request.locationId != null && request.locationId != schedule.locationId) ||
                    (request.startsAt != null && request.startsAt != schedule.startsAt) ||
                    (request.endsAt != null && request.endsAt != schedule.endsAt)

        val isParticipantsChanged = request.participantUserIds != null &&
                !schedule.isParticipantsSame(request.participantUserIds)

        return !(isAnyFieldChanged || isParticipantsChanged)
    }

    private fun findScheduleOrThrow(scheduleId: Long): Schedule {
        return scheduleRepository.findById(scheduleId)
            .orElseThrow { BusinessException(ErrorCode.CALENDAR_NOT_FOUND) }
    }

    private fun validateScheduleOwner(ownerId: Long, requesterId: Long) {
        if (ownerId != requesterId) {
            throw BusinessException(ErrorCode.CALENDAR_NO_PERMISSION)
        }
    }

    private fun validateScheduleTeam(scheduleTeamId: Long, currentTeamId: Long) {
        if (scheduleTeamId != currentTeamId) {
            throw BusinessException(ErrorCode.CALENDAR_TEAM_MISMATCH)
        }
    }
}