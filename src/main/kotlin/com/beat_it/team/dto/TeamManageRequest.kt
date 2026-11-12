package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole

data class TeamManageRequest(
    val targetRole: TeamRole,
)