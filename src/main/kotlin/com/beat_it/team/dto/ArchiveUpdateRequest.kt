package com.beat_it.team.dto

data class ArchiveUpdateRequest(
    val title: String? = null,
    val locationId: Long? = null,
    val description: String? = null,
)
