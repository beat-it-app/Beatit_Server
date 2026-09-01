package com.beat_it.team.dto

data class ArchiveCreateResponse(
    val archiveId: Long,
    val teamId: Long,
    val writerId: Long,
    val title: String,
    val placeName: String?,
    val locationId: Long,
    val archiveImageUrls: List<String>,
    val createdAt: String,
)
