package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ArchiveCreateResponse(
    val archiveId: Long,
    val title: String,
    val placeName: String?,
    val locationId: Long,
    val archiveImageUrl: String?,
    val createdAt: String,
)