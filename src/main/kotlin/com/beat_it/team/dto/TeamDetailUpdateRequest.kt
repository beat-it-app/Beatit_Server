package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
import com.beat_it.team.entity.enum.TeamType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalDate
import java.util.UUID

data class TeamDetailUpdateRequest(
    val teamName: String? = null,
    val description: String? = null,
    val teamType: TeamType? = null,
    val establishedOn: LocalDate? = null,
    val teamImageUrl: String? = null,
    val links: List<TeamLinksRequest>? = null,
    @field:Valid
    val parts: List<TeamMemberPartRequest>? = null,
)

data class TeamLinksRequest(
    val platformCode: PlatformCode,
    val linkUrl: String,
)

data class TeamMemberPartRequest(
    val userPublicId: UUID,
    @field:NotBlank
    val partName: String,
    @field:PositiveOrZero
    val displayOrder: Int,
)
