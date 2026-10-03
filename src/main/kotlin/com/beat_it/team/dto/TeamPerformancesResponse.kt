package com.beat_it.team.dto

import java.time.OffsetDateTime
import java.util.UUID

data class TeamPerformancesResponse(
    val items: List<TeamPerformanceItemResponse>,
)

data class TeamPerformanceItemResponse(
    val performancePublicId: UUID,
    val title: String,
    val performanceDateTime: OffsetDateTime,
    val posterImageUrl: String?,
)
