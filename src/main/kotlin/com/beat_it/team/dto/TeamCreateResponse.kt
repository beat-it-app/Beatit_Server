package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class TeamCreateResponse(
    val teamId: Long,
    val teamPublicId: UUID,
    val teamName: String,
    val description: String?,
    val inviteCode: String,
    val teamType: TeamType,
    val teamRole: String,
    val createdAt: String,
)