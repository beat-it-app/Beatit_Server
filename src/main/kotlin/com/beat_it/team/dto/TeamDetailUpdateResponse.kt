package com.beat_it.team.dto

import java.time.LocalDate
import java.util.UUID

data class TeamDetailUpdateResponse(
    val teamId: Long,
    val teamPublicId: UUID,
    val teamName: String,
    val description: String?,
    val establishedOn: LocalDate?,
    val updatedAt: String,
    val links: List<LinksResponse>? = null,
    val parts: List<PartsResponse> = emptyList(),
)
