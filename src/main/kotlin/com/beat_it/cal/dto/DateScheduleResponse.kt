package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class DateSchedulesResponse(
    val items: List<DateSchedule>
)

data class DateSchedule(
    val scheduleId: Long,
    val title: String,
    val content: String,
    val startsAt: String,
    val endsAt: String,
    val locationId: Long?
)