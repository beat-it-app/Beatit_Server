package com.beat_it.post.dto.meetit

import java.time.LocalTime
import java.time.LocalDateTime

data class MeetitCreateRequest(
    val title: String,
    val candidateDates: List<String>,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val dateOnly: Boolean = false,
    val participantUserIds: List<Long>
)

data class MeetitSubmissionRequest(
    val slotStartTimes: List<LocalDateTime>
)
