package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ScheduleCreateResponse(
    @JsonProperty("schedule_id")
    val scheduleId: Long,

    val title: String,

    @JsonProperty("starts_at")
    val startsAt: LocalDateTime,

    @JsonProperty("ends_at")
    val endsAt: LocalDateTime,

    @JsonProperty("created_at")
    val createdAt: LocalDateTime
)