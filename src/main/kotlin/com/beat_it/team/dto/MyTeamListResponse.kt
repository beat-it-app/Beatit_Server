package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole
import java.time.OffsetDateTime

data class MyTeamListResponse(
    val items: List<MyTeamItemResponse>
)

data class MyTeamItemResponse(
    val teamId: Long,
    val teamName: String,
    val description: String?,
    val profileImageUrl: String?,
    val teamRole: TeamRole,
    val joinedAt: OffsetDateTime,
)