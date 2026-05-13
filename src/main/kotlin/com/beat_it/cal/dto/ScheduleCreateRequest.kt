package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ScheduleCreateRequest(
    @JsonProperty("location_id")
    val locationId: Long?,

    val title: String?,

    val content: String?,

    @JsonProperty("starts_at")
    val startsAt: LocalDateTime?,

    @JsonProperty("ends_at")
    val endsAt: LocalDateTime?,

    @JsonProperty("participant_user_ids")
    val participantUserIds: List<Long> = emptyList()
)