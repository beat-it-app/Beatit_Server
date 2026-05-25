package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime

data class TeamCreateRequest(
    @JsonProperty("name") val teamName: String,

    val description: String?,

    @JsonProperty("team_type") val teamType: TeamType,

    @JsonProperty("established_on") val establishedOn: LocalDate?,

    @JsonProperty("profile_image_url") val profileImageUrl: String?,

    )