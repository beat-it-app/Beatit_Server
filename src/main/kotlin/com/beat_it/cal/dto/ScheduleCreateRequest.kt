package com.beat_it.cal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime

data class ScheduleCreateRequest(
    val locationId: Long?,
    val title: String?,
    val content: String?,
    val startsAt: OffsetDateTime?,
    val endsAt: OffsetDateTime?,
    val participantUserIds: List<Long> = emptyList(),
    val musicTitle: String?,
    val musicArtist: String?,
    val musicPreviewUrl: String?,
    val files: List<MultipartFile>? = null
)