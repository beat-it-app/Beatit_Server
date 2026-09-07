package com.beat_it.cal.dto

import java.time.OffsetDateTime

data class DateSchedulesResponse(
    val items: List<DateSchedule>
)

data class DateSchedule(
    val scheduleId: Long,
    val title: String,
    val content: String,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime,
    val locationId: Long?
)