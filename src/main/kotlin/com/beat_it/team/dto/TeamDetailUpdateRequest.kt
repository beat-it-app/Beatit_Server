package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime

data class TeamDetailUpdateRequest(
    @JsonProperty("name") val teamName: String? = null,

    val description: String? = null,

    @JsonProperty("team_type") val teamType: TeamType? = null,

    @JsonProperty("established_on") val establishedOn: LocalDate? = null,

    @JsonProperty("profile_image_url") val profileImageUrl: String? = null,

    val links: List<TeamLinksRequest>? = null,

    )

data class TeamLinksRequest(
    @JsonProperty("platform_code") val platformCode: PlatformCode,
    @JsonProperty("link_url") val linkUrl: String,
)