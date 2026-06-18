package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
import com.beat_it.team.entity.enum.TeamType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.OffsetDateTime

data class ArchiveUpdateRequest(
    val title: String? = null,
    val placeName: String? = null,
    val locationId: Long? = null,
    val description: String? = null,
    val archiveImageUrl: String? = null,
)
