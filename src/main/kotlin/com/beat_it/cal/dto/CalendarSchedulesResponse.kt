package com.beat_it.cal.dto

import java.time.OffsetDateTime

data class CalendarSchedulesResponse(
    val items: List<CalendarSchedule>
)

data class CalendarSchedule(
    val scheduleId: Long,
    val title: String,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime
)