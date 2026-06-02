package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.OffsetDateTime

data class TeamCreateRequest(
    val teamName: String,
    val description: String?,
    val teamType: TeamType,
    val establishedOn: LocalDate?,
    val profileImageUrl: String?,
)