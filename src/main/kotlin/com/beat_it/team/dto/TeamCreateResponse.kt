package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import java.time.LocalDate
import java.util.UUID

data class TeamCreateResponse(
    val teamId: Long,
    val teamPublicId: UUID,
    val teamName: String,
    val description: String?,
    val teamImageUrl: String?,
    val inviteCode: String,
    val establishedOn: LocalDate?,
    val teamType: TeamType,
    val teamRole: String,
    val createdAt: OffsetDateTime,
)