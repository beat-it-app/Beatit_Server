package com.beat_it.team.dto.archive

data class ArchiveUpdateResponse(
    val archiveId: Long,
    val title: String? = null,
    val placeName: String? = null,
    val locationId: Long? = null,
    val description: String? = null,
    val archiveImageUrl: String? = null,
    val updatedAt: String,
)