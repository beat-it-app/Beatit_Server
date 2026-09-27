package com.beat_it.team.dto
import java.time.OffsetDateTime

data class ArchiveUpdateResponse(
    val archiveId: Long,
    val title: String,
    val roadAddress: String?,
    val locationId: Long,
    val description: String?,
    val archiveImageUrls: List<String>,
    val updatedAt: OffsetDateTime,
)
