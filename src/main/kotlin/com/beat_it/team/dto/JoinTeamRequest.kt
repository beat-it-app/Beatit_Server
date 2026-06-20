package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamRole
import java.time.OffsetDateTime

data class JoinTeamRequest(
    val inviteCode: String? = null
)