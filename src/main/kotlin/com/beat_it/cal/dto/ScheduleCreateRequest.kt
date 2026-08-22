package com.beat_it.cal.dto

import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime

data class ScheduleCreateRequest(
    val locationId: Long?,
    val title: String?,
    val content: String?,
    val startsAt: OffsetDateTime?,
    val endsAt: OffsetDateTime?,
    val participantUserIds: List<Long> = emptyList(),
    val musics: List<MusicRequest> = emptyList(),
    val files: List<MultipartFile>? = null
) {
    data class MusicRequest(
        val musicTitle: String?,
        val musicArtist: String?,
        val musicPreviewUrl: String?,
        val musicImageUrl: String?
    )
}