package com.beat_it.team.dto

import java.time.OffsetDateTime

data class ArchiveListResponse(
    val archives: List<ArchiveListItemResponse>,
    val topArchiveId: Long?,
    val totalCount: Int,
    val hasNext: Boolean,
)

data class ArchiveListItemResponse(
    val archiveId: Long,
    val teamId: Long,
    val writerId: Long,
    val title: String,
    val roadAddress: String?,
    val locationId: Long,
    val archiveImageUrl: String?,
    val averageRating: Double,
    val ratingCount: Int,
    val commentCount: Int,
    val createdAt: OffsetDateTime,
)
