package com.beat_it.team.dto

data class ArchiveCreateRequest(
    val title: String,
    val locationId: Long,
    val description: String? = null,
)
