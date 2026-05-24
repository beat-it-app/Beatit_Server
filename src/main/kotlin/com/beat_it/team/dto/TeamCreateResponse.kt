package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class TeamCreateResponse(
    @JsonProperty("team_id")
    val teamId: Long,

    @JsonProperty("name")
    val teamName: String,

    val description: String?,

    @JsonProperty("invite_code")
    val inviteCode: String,

    @JsonProperty("team_type")
    val teamType: TeamType,

    @JsonProperty("team_role")
    val teamRole: String,

    @JsonProperty("created_at")
    val createdAt: OffsetDateTime,
)