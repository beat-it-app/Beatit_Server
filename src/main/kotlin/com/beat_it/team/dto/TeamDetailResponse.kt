package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.util.UUID

data class TeamDetailResponse(
    val teamId: Long? = null,
    val teamPublicId: UUID,
    val teamImageUrl: String?,
    val teamName: String,
    val description: String?,
    val establishedOn: LocalDate?,
    val inviteCode: String,
    val memberCount: Int,
    val createdAt: String,
    val updatedAt: String?,
    val links: List<LinksResponse>,
    val parts: List<PartsResponse>,
    val archiveCount: Int,
    val cloudItemCount: Int,
    val upcomingSchedules: List<UpcomingScheduleResponse>,
)

data class UpcomingScheduleResponse(
    val scheduleId: Long,
    val title: String,
    val startsAt: String,
    val endsAt: String,
    val locationId: Long?,
    @get:JsonProperty("isParticipant")
    val isParticipant: Boolean,
)

data class LinksResponse(
    val teamLinkId: Long,
    val platformCode: PlatformCode,
    val linkUrl: String,
)

data class PartsResponse(
    val teamPartId: Long,
    val userPublicId: UUID,
    val userName: String,
    val profileImageUrl: String?,
    val partName: String,
    val displayOrder: Int,
)
