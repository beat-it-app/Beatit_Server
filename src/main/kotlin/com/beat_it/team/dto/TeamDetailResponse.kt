package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
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
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val links: List<LinksResponse>,
    val parts: List<PartsResponse> = emptyList(),
    val archiveCount: Int,
    val cloudItemCount: Int,
)

data class LinksResponse(
    val teamLinkId: Long,
    val platformCode: PlatformCode,
    val linkUrl: String,
)

data class PartsResponse(
    val teamPartId: Long,
    val partName: String,
    val displayOrder: Int,
)