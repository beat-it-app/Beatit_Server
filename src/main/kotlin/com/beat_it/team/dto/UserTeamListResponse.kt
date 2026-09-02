package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import java.util.UUID

data class UserTeamListResponse(
    val teams: List<TeamSimpleInfo>
)

data class TeamSimpleInfo(
    val teamId: Long,
    val teamPublicId: UUID,
    val teamName: String,
    val teamType: TeamType,
    val teamImageUrl: String?,
    val createdAt: String,
)
