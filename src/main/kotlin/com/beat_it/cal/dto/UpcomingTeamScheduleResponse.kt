package com.beat_it.cal.dto

import java.time.OffsetDateTime

data class UpcomingTeamScheduleResponse(
    val scheduleId: Long,
    val title: String,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime,
    val locationName: String?,
)
