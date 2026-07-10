package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
import com.beat_it.team.entity.enum.TeamType
import java.time.LocalDate

data class TeamDetailUpdateRequest(
    val teamName: String,
    val description: String? = null,
    val teamType: TeamType? = null,
    val establishedOn: LocalDate? = null,
    val teamImageUrl: String? = null,
    val links: List<TeamLinksRequest>? = null,
)

data class TeamLinksRequest(
    val platformCode: PlatformCode,
    val linkUrl: String,
)