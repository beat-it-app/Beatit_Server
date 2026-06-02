package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ScheduleCreateResponse(
    val scheduleId: Long,
    val title: String,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime,
    val createdAt: OffsetDateTime
)