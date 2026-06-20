package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole
import java.time.OffsetDateTime
import java.util.UUID

data class TeamJoinResponse(
    val teamId: Long,
    val teamPublicId: UUID,
    val teamName: String,
    val teamRole: TeamRole,
    val joinedAt: String,
)