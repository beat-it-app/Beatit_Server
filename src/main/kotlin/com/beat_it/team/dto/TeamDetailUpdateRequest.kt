package com.beat_it.team.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime

data class TeamDetailUpdateRequest(
    @JsonProperty("team_name")
    val teamName: String,

    val description: String?,

    @JsonProperty("established_on") val establishedOn: LocalDate?,

    @JsonProperty("profile_image_url") val profileImageUrl: String?,

    val links: List<LinksRequest> = emptyList(),

)

data class LinksRequest(
    @JsonProperty("link_url") val linkUrl: String,
    @JsonProperty("plat_form_code") val platFormCode: String,
)