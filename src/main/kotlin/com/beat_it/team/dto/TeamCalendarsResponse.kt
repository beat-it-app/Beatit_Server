package com.beat_it.team.dto

import java.time.OffsetDateTime

data class TeamCalendarsResponse(
    val items: List<TeamCalendarItemResponse>,
)

data class TeamCalendarItemResponse(
    val scheduleId: Long,
    val title: String,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime,
    val locationName: String?,
)
