package com.beat_it.team.dto.archive

data class ArchiveCreateRequest(
    val title: String,
    val placeName: String? = null,
    val locationId: Long,
    val description: String? = null,
)
