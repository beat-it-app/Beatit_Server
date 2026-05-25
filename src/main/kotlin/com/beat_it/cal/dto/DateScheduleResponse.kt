package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class DateSchedulesResponse(
    val items: List<DateSchedule>
)

data class DateSchedule(
    @JsonProperty("schedule_id")
    val scheduleId: Long,

    val title: String,

    val content: String,

    @JsonProperty("starts_at")
    val startsAt: OffsetDateTime,

    @JsonProperty("ends_at")
    val endsAt: OffsetDateTime,

    @JsonProperty("location_id")
    val locationId: Long?
)