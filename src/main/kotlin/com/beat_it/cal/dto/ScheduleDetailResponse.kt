package com.beat_it.cal.dto

import java.time.OffsetDateTime

data class ScheduleFileResponse(
    val fileId: Long,
    val originalFileName: String,
    val cdnUrl: String
)

data class ScheduleDetailResponse(
    val scheduleId: Long,
    val teamId: Long,
    val userId: Long,
    val locationId: Long?,
    val title: String,
    val content: String?,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val participants: List<ParticipantResponse>,
    val files: List<ScheduleFileResponse>
)

data class ParticipantResponse(
    val scheduleParticipantId: Long,
    val userId: Long
)