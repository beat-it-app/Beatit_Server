package com.beat_it.team.dto.archive

data class ArchiveUpdateRequest(
    val title: String? = null,
    val placeName: String? = null,
    val locationId: Long? = null,
    val description: String? = null,
)
