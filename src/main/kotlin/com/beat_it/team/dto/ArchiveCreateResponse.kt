package com.beat_it.team.dto

import java.time.OffsetDateTime

data class ArchiveCreateResponse(
    val archiveId: Long,
    val title: String,
    val placeName: String?,
    val locationId: Long,
    val archiveImageUrl: String?,
    val createdAt: OffsetDateTime,
)