package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class TeamInviteInfoResponse(
    val teamPublicId: UUID,
    val teamName: String,
    val teamType: TeamType,
    val teamImageUrl: String?,
    val establishedOn: LocalDate?,
    val createdAt: OffsetDateTime,
)
