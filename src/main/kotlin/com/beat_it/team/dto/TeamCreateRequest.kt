package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import java.time.LocalDate

data class TeamCreateRequest(
    val teamName: String,
    val description: String?,
    val teamType: TeamType,
    val establishedOn: LocalDate?,
    val teamImageUrl: String?,
)