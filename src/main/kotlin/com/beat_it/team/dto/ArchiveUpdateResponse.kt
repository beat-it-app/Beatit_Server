package com.beat_it.team.dto

import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ArchiveUpdateResponse(
    val archiveId: Long,
    val title: String? = null,
    val placeName: String? = null,
    val locationId: Long? = null,
    val description: String? = null,
    val archiveImageUrl: String? = null,
    val updatedAt: OffsetDateTime,
)