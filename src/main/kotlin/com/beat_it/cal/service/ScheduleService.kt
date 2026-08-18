package com.beat_it.cal.service

import com.beat_it.auth.service.UserService
import com.beat_it.cal.dto.CalendarSchedule
import com.beat_it.cal.dto.CalendarSchedulesResponse
import com.beat_it.cal.dto.DateSchedule
import com.beat_it.cal.dto.DateSchedulesResponse
import com.beat_it.cal.dto.ParticipantResponse
import com.beat_it.cal.dto.ScheduleCreateRequest
import com.beat_it.cal.dto.ScheduleCreateResponse
import com.beat_it.cal.dto.ScheduleDetailResponse
import com.beat_it.cal.dto.ScheduleFileResponse
import com.beat_it.cal.dto.ScheduleUpdateRequest
import com.beat_it.cal.entity.Schedule
import com.beat_it.cal.repository.ScheduleRepository
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.global.service.FileService
import com.beat_it.global.util.DateTimeUtil
import com.beat_it.team.service.TeamService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val teamService: TeamService,
    private val userService: UserService,
    private val fileService: FileService
    // private val locationService: LocationService
) {

    @Transactional
    fun createSchedule(userId: Long, request: ScheduleCreateRequest): ScheduleCreateResponse {

        validateScheduleCommon(request.title, request.startsAt, request.endsAt)

        val currentTeamId = userService.getCurrentTeamId(userId)

        teamService.validateTeamMember(currentTeamId, userId)

        val schedule = Schedule(
            teamId = currentTeamId,
            userId = userId,
            title = request.title!!,
            content = request.content,
            locationId = request.locationId,
            startsAt = request.startsAt!!,
            endsAt = request.endsAt!!
        )

        request.musics.forEach { music ->
            schedule.addMusic(
                musicTitle = music.musicTitle,
                musicArtist = music.musicArtist,
                musicPreviewUrl = music.musicPreviewUrl
            )
        }

        if (!request.files.isNullOrEmpty()) {
            val uploadedFiles = fileService.uploadFiles(request.files, "schedules")
            uploadedFiles.forEach { fileResult ->
                schedule.addFile(
                    originalFileName = fileResult.originalFileName,
                    storageKey = fileResult.storageKey,
                    cdnUrl = fileResult.cdnUrl
                )
            }
        }

        request.participantUserIds.forEach { participantUserId ->
            teamService.validateTeamMember(currentTeamId, participantUserId)
            schedule.addParticipant(participantUserId)
        }

        //TODO: 여러명 검증 함수 추후 변경
//        val isValidTeamMembers = teamService.validateMembersInTeam(currentTeamId, request.participantUserIds)
//        if (!isValidTeamMembers) {
//            throw BusinessException(ErrorCode.INVALID_TEAM_PARTICIPANTS)
//        }
//
//        request.participantUserIds.forEach { participantUserId ->
//            schedule.addParticipant(participantUserId)
//        }



        val savedSchedule = scheduleRepository.save(schedule)

        return ScheduleCreateResponse(
            scheduleId = savedSchedule.scheduleId!!,
            title = savedSchedule.title,
            startsAt = DateTimeUtil.format(savedSchedule.startsAt),
            endsAt = DateTimeUtil.format(savedSchedule.endsAt),
            createdAt = DateTimeUtil.format(savedSchedule.createdAt)
        )
    }

    @Transactional
    fun updateSchedule(scheduleId: Long, userId: Long, request: ScheduleUpdateRequest): ScheduleCreateResponse {

        validateScheduleCommon(request.title, request.startsAt, request.endsAt)
        val schedule = findScheduleOrThrow(scheduleId)

        validateScheduleOwner(schedule.userId, userId)

        if (isNotChanged(schedule, request)) {
            throw BusinessException(ErrorCode.CALENDAR_NO_CONTENT_TO_UPDATE)
        }

        request.participantUserIds?.let { newIds ->
            schedule.participants.clear()
            newIds.forEach { participantId ->
                teamService.validateTeamMember(schedule.teamId, participantId) // 팀 소속 검증
                schedule.addParticipant(participantId)
            }
        }

        //TODO: 여러명 검증 함수 추후 변경
//        request.participantUserIds?.let { newIds ->
//            val isValidTeamMembers = teamService.validateMembersInTeam(schedule.teamId, newIds)
//            if (!isValidTeamMembers) {
//                throw BusinessException(ErrorCode.INVALID_TEAM_PARTICIPANTS)
//            }
//
//            schedule.participants.clear()
//            newIds.forEach { participantId ->
//                schedule.addParticipant(participantId)
//            }
//        }

        val retainMusicIds = request.retainMusicIds ?: emptyList()
        schedule.musics.removeIf { it.id !in retainMusicIds }

        request.musics?.forEach { musicRequest ->
            schedule.addMusic(
                musicTitle = musicRequest.musicTitle,
                musicArtist = musicRequest.musicArtist,
                musicPreviewUrl = musicRequest.musicPreviewUrl
            )
        }

        val retainFileIds = request.retainFileIds ?: emptyList()

        val filesToRemove = schedule.files.filter { it.id !in retainFileIds }
        filesToRemove.forEach { file ->
            //TODO: fileService delete 함수 반영 시 주석 제거
            //fileService.deleteFile(file.storageKey)
        }
        schedule.files.removeIf { it.id !in retainFileIds }

        if (!request.files.isNullOrEmpty()) {
            val uploadedFiles = fileService.uploadFiles(request.files, "schedules")
            uploadedFiles.forEach { fileResult ->
                schedule.addFile(
                    originalFileName = fileResult.originalFileName,
                    storageKey = fileResult.storageKey,
                    cdnUrl = fileResult.cdnUrl
                )
            }
        }

        schedule.update(
            title = request.title!!,
            content = request.content,
            locationId = request.locationId,
            startsAt = request.startsAt!!,
            endsAt = request.endsAt!!
        )

        return ScheduleCreateResponse(
            scheduleId = schedule.scheduleId!!,
            title = schedule.title,
            startsAt = DateTimeUtil.format(schedule.startsAt),
            endsAt = DateTimeUtil.format(schedule.endsAt),
            createdAt = DateTimeUtil.format(schedule.createdAt)
        )
    }

    @Transactional
    fun deleteSchedule(scheduleId: Long, userId: Long) {
        val schedule = findScheduleOrThrow(scheduleId)

        validateScheduleOwner(schedule.userId, userId)

        //TODO: fileService delete 함수 반영 시 주석 제거
//        if (schedule.files.isNotEmpty()) {
//            schedule.files.forEach { file ->
//                fileService.deleteFile(file.storageKey)
//            }
//        }
//        scheduleRepository.delete(schedule)
    }

    @Transactional(readOnly = true)
    fun getScheduleDetail(scheduleId: Long, userId: Long): ScheduleDetailResponse {
        val schedule = findScheduleOrThrow(scheduleId)

        val currentTeamId = userService.getCurrentTeamId(userId)
        validateScheduleTeam(schedule.teamId, currentTeamId)

        val fileResponses = schedule.files.map { file ->
            ScheduleFileResponse(
                fileId = file.id!!,
                originalFileName = file.originalFileName,
                cdnUrl = file.cdnUrl
            )
        }

        return ScheduleDetailResponse(
            scheduleId = schedule.scheduleId!!,
            teamId = schedule.teamId,
            userId = schedule.userId,
            locationId = schedule.locationId,
            title = schedule.title,
            content = schedule.content,
            startsAt = DateTimeUtil.format(schedule.startsAt),
            endsAt = DateTimeUtil.format(schedule.endsAt),
            createdAt = DateTimeUtil.format(schedule.createdAt),
            updatedAt = DateTimeUtil.format(schedule.updatedAt),
            participants = schedule.participants.map { participant ->
                ParticipantResponse(
                    scheduleParticipantId = participant.scheduleParticipantId!!,
                    userId = participant.userId
                )
            },
            files = fileResponses
        )
    }

    @Transactional(readOnly = true)
    fun getCalendarSchedules(userId: Long, year: Int, month: Int): CalendarSchedulesResponse {
        validateYearAndMonth(year, month)
        val startLocalDate = LocalDate.of(year, month, 1)
        val endLocalDate = startLocalDate.withDayOfMonth(startLocalDate.lengthOfMonth())

        val zoneOffset = ZoneOffset.ofHours(9)
        val startDateTime = OffsetDateTime.of(startLocalDate, LocalTime.MIN, zoneOffset)
        val endDateTime = OffsetDateTime.of(endLocalDate, LocalTime.MAX, zoneOffset)

        val schedules = scheduleRepository.findByUserIdAndMonthRange(userId, startDateTime, endDateTime)

        val calendarSchedules = schedules.map { schedule ->
            CalendarSchedule(
                scheduleId = schedule.scheduleId ?: throw BusinessException(ErrorCode.CALENDAR_NOT_FOUND),
                title = schedule.title,
                startsAt = DateTimeUtil.format(schedule.startsAt),
                endsAt = DateTimeUtil.format(schedule.endsAt)
            )
        }

        return CalendarSchedulesResponse(items = calendarSchedules)
    }

    @Transactional(readOnly = true)
    fun getDateSchedules(userId: Long, year: Int, month: Int, date: Int): DateSchedulesResponse {
        validateYearMonthAndDate(year, month, date)
        val targetLocalDate = LocalDate.of(year, month, date)

        val zoneOffset = ZoneOffset.ofHours(9)
        val startDateTime = OffsetDateTime.of(targetLocalDate, LocalTime.MIN, zoneOffset)
        val endDateTime = OffsetDateTime.of(targetLocalDate, LocalTime.MAX, zoneOffset)

        val schedules = scheduleRepository.findByUserIdAndDailyRange(userId, startDateTime, endDateTime)

        val dateSchedules = schedules.map { schedule ->
            DateSchedule(
                scheduleId = schedule.scheduleId ?: throw BusinessException(ErrorCode.CALENDAR_NOT_FOUND),
                title = schedule.title,
                content = schedule.content ?: "",
                startsAt = DateTimeUtil.format(schedule.startsAt),
                endsAt = DateTimeUtil.format(schedule.endsAt),
                locationId = schedule.locationId
            )
        }

        return DateSchedulesResponse(items = dateSchedules)
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

        val retainMusicIds = request.retainMusicIds ?: emptyList()
        val isMusicsChanged = (schedule.musics.size != retainMusicIds.size) || !request.musics.isNullOrEmpty()

        val retainFileIds = request.retainFileIds ?: emptyList()
        val isFilesChanged = (schedule.files.size != retainFileIds.size) || !request.files.isNullOrEmpty()

        return !(isAnyFieldChanged || isParticipantsChanged || isMusicsChanged || isFilesChanged)
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

    private fun validateYearAndMonth(year: Int, month: Int) {
        if (year !in 1000..9999) {
            throw BusinessException(ErrorCode.CALENDAR_INVALID_YEAR)
        }
        if (month !in 1..12) {
            throw BusinessException(ErrorCode.CALENDAR_INVALID_MONTH)
        }
    }

    private fun validateYearMonthAndDate(year: Int, month: Int, date: Int) {
        if (year !in 1000..9999) {
            throw BusinessException(ErrorCode.CALENDAR_INVALID_YEAR)
        }

        if (month !in 1..12) {
            throw BusinessException(ErrorCode.CALENDAR_INVALID_MONTH)
        }

        try {
            java.time.YearMonth.of(year, month).atDay(date)
        } catch (e: java.time.DateTimeException) {
            throw BusinessException(ErrorCode.CALENDAR_NON_EXISTENT_DATE)
        }
    }
}