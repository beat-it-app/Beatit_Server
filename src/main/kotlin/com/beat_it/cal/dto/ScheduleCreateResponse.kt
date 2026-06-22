package com.beat_it.cal.dto

import java.time.OffsetDateTime

data class ScheduleCreateResponse(
    val scheduleId: Long,
    val title: String,
    val startsAt: String,
    val endsAt: String,
    val createdAt: String
)