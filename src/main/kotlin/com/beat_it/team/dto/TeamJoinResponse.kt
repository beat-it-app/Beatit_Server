package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole
import java.time.OffsetDateTime

data class TeamJoinResponse(
    val teamId: Long,
    val teamName: String,
    val teamRole: TeamRole,
    val joinedAt: OffsetDateTime,
)