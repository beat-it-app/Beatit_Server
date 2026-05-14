package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ScheduleDetailResponse(
    @JsonProperty("schedule_id") val scheduleId: Long,
    @JsonProperty("team_id") val teamId: Long,
    @JsonProperty("user_id") val userId: Long,
    @JsonProperty("location_id") val locationId: Long?,
    val title: String,
    val content: String?,
    @JsonProperty("starts_at") val startsAt: OffsetDateTime,
    @JsonProperty("ends_at") val endsAt: OffsetDateTime,
    @JsonProperty("created_at") val createdAt: OffsetDateTime,
    @JsonProperty("updated_at") val updatedAt: OffsetDateTime,
    val participants: List<ParticipantResponse>
)

data class ParticipantResponse(
    @JsonProperty("schedule_participant_id") val scheduleParticipantId: Long,
    @JsonProperty("user_id") val userId: Long
)