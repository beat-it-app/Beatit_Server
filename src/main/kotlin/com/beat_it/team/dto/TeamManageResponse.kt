package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole

data class TeamManageResponse(
    val updatedMembers: List<TeamRoleChangedMember>
)

data class TeamRoleChangedMember(
    val userId: Long,
    val updatedRole: TeamRole,
)