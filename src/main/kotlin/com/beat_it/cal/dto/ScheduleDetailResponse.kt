package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ScheduleDetailResponse(
    val scheduleId: Long,
    val teamId: Long,
    val userId: Long,
    val locationId: Long?,
    val title: String,
    val content: String?,
    val startsAt: String,
    val endsAt: String,
    val createdAt: String,
    val updatedAt: String,
    val participants: List<ParticipantResponse>
)

data class ParticipantResponse(
    val scheduleParticipantId: Long,
    val userId: Long
)