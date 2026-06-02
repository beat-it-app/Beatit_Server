package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ScheduleUpdateRequest(
    val locationId: Long?,
    val title: String?,
    val content: String?,
    val startsAt: OffsetDateTime?,
    val endsAt: OffsetDateTime?,
    val participantUserIds: List<Long>? = null
)