package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole
import java.util.UUID

data class TeamManageResponse(
    val updatedMembers: List<TeamMemberInfo>,
)

data class TeamMemberListResponse(
    val members: List<TeamMemberInfo>,
)

data class TeamMemberInfo(
    val userPublicId: UUID,
    val userName: String,
    val profileImageUrl: String?,
    val teamRole: TeamRole,
)