package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ScheduleCreateRequest(
    @JsonProperty("location_id")
    val locationId: Long?,

    val title: String?,

    val content: String?,

    @JsonProperty("starts_at")
    val startsAt: OffsetDateTime?,

    @JsonProperty("ends_at")
    val endsAt: OffsetDateTime?,

    @JsonProperty("participant_user_ids")
    val participantUserIds: List<Long> = emptyList()
)