package com.beat_it.team.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime

data class TeamDetailUpdateResponse(
    @JsonProperty("team_id") val teamId: Long,

    @JsonProperty("name") val teamName: String,

    val description: String?,

    @JsonProperty("established_on") val establishedOn: LocalDate?,

    @JsonProperty("updated_at") val updatedAt: OffsetDateTime,

    val links: List<TeamLinksRequest>? = null,
    )