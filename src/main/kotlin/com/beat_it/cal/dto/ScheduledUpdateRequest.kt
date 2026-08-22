package com.beat_it.cal.dto

import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime

data class MusicRequest(
    val musicTitle: String,
    val musicArtist: String,
    val musicPreviewUrl: String?,
    val musicImageUrl: String?
)

data class ScheduleUpdateRequest(
    val locationId: Long?,
    val title: String?,
    val content: String?,
    val startsAt: OffsetDateTime?,
    val endsAt: OffsetDateTime?,
    val participantUserIds: List<Long>?,
    val musics: List<MusicRequest>?,
    val retainMusicIds: List<Long>?,
    val files: List<MultipartFile>? = null,
    val retainFileIds: List<Long>? = null
)