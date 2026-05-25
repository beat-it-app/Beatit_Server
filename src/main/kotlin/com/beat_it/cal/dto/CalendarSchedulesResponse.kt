package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class CalendarSchedulesResponse(
    val items: List<CalendarSchedule>
)

data class CalendarSchedule(
    @JsonProperty("schedule_id")
    val scheduleId: Long,
    val title: String,
    @JsonProperty("starts_at")
    val startsAt: OffsetDateTime,
    @JsonProperty("ends_at")
    val endsAt: OffsetDateTime
)