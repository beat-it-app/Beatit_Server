package com.beat_it.team.dto

data class ArchiveListResponse(
    val archives: List<ArchiveListItemResponse>,
    val topArchive: ArchiveListItemResponse?,
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
    val createdAt: String,
)
