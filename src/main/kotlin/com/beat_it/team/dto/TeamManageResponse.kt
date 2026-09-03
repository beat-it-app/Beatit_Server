package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole
import java.util.UUID

data class TeamManageResponse(
    val updatedMembers: List<MemberItems>,
)

data class TeamMemberListResponse(
    val members: List<MemberItems>,
)

data class MemberItems(
    val userPublicId: UUID,
    val userName: String,
    val profileImageUrl: String?,
    val teamRole: TeamRole,
    val position: String?,
)