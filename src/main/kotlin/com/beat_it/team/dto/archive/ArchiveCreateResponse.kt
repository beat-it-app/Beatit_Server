package com.beat_it.team.dto.archive

data class ArchiveCreateResponse(
    val archiveId: Long,
    val title: String,
    val placeName: String?,
    val locationId: Long,
    val archiveImageUrl: String?,
    val createdAt: String,
)