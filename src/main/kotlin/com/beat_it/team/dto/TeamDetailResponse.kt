package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
import com.beat_it.team.entity.enum.TeamType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class TeamDetailResponse(
    val teamId: Long? = null,
    val teamPublicId: UUID,
    val teamImageUrl: String?,
    val teamName: String,
    val description: String?,
    val teamType: TeamType,
    val establishedOn: LocalDate?,
    val inviteCode: String,
    val memberCount: Int,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val links: List<LinksResponse>,
    val members: List<TeamDetailMemberResponse>,
)

data class LinksResponse(
    val teamLinkId: Long,
    val platformCode: PlatformCode,
    val linkUrl: String,
)

data class TeamDetailMemberResponse(
    val userName: String,
    val profileImageUrl: String?,
    val position: String?,
)
