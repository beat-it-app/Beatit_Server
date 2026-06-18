package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime

data class TeamCreateResponse(
    val teamId: Long,
    val teamName: String,
    val description: String?,
    val teamImageUrl: String?,
    val inviteCode: String,
    val establishedOn: LocalDate?,
    val teamType: TeamType,
    val teamRole: String,
    val createdAt: OffsetDateTime,
)