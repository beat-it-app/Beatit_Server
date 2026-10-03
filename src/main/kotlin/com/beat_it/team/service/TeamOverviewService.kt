package com.beat_it.team.service

import com.beat_it.auth.service.UserService
import com.beat_it.location.repository.LocationsRepository
import com.beat_it.performance.repository.PerformanceFilesRepository
import com.beat_it.team.dto.TeamCalendarItemResponse
import com.beat_it.team.dto.TeamCalendarsResponse
import com.beat_it.team.dto.TeamPerformanceItemResponse
import com.beat_it.team.dto.TeamPerformancesResponse
import com.beat_it.team.repository.TeamCalendarQueryRepository
import com.beat_it.team.repository.TeamPerformanceQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class TeamOverviewService(
    private val userService: UserService,
    private val teamService: TeamService,
    private val teamCalendarQueryRepository: TeamCalendarQueryRepository,
    private val teamPerformanceQueryRepository: TeamPerformanceQueryRepository,
    private val locationsRepository: LocationsRepository,
    private val performanceFilesRepository: PerformanceFilesRepository,
) {

    @Transactional(readOnly = true)
    fun getTeamCalendars(userId: Long): TeamCalendarsResponse {
        val teamId = getValidatedCurrentTeamId(userId)
        val startAt = OffsetDateTime.now()
        val endAt = startAt.plusDays(7)

        val schedules = teamCalendarQueryRepository.findAllByTeamIdWithinRange(
            teamId = teamId,
            startAt = startAt,
            endAt = endAt,
        )

        val locationIds = schedules
            .mapNotNull { it.locationId }
            .distinct()

        val locationNameById = if (locationIds.isEmpty()) {
            emptyMap()
        } else {
            locationsRepository.findAllById(locationIds)
                .associate { location -> location.locationId!! to location.locationName }
        }

        return TeamCalendarsResponse(
            items = schedules.map { schedule ->
                TeamCalendarItemResponse(
                    scheduleId = schedule.scheduleId!!,
                    title = schedule.title,
                    startsAt = schedule.startsAt,
                    endsAt = schedule.endsAt,
                    locationName = schedule.locationId?.let(locationNameById::get),
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun getTeamPerformances(userId: Long): TeamPerformancesResponse {
        val teamId = getValidatedCurrentTeamId(userId)
        val now = OffsetDateTime.now()

        val performances = teamPerformanceQueryRepository
            .findTop10ByTeamIdAndPerformanceDateTimeGreaterThanEqualOrderByPerformanceDateTimeAsc(
                teamId = teamId,
                performanceDateTime = now,
            )

        val posterFileIds = performances
            .mapNotNull { it.posterFileId }
            .distinct()

        val posterUrlById = if (posterFileIds.isEmpty()) {
            emptyMap()
        } else {
            performanceFilesRepository.findAllById(posterFileIds)
                .associate { file -> file.performanceFileId!! to file.cdnUrl }
        }

        return TeamPerformancesResponse(
            items = performances.map { performance ->
                TeamPerformanceItemResponse(
                    performancePublicId = performance.publicId,
                    title = performance.title,
                    performanceDateTime = performance.performanceDateTime,
                    posterImageUrl = performance.posterFileId?.let(posterUrlById::get),
                )
            }
        )
    }

    private fun getValidatedCurrentTeamId(userId: Long): Long {
        val teamId = userService.getCurrentTeamId(userId)
        teamService.findTeamForCommandOrThrow(teamId)
        teamService.validateTeamMember(teamId, userId)
        return teamId
    }
}
