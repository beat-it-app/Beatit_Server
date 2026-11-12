package com.beat_it.team.dto

data class ArchiveListResponse(
    val archives: List<ArchiveListItemResponse>,
    val totalCount: Int,
    val hasNext: Boolean,
)

data class ArchiveListItemResponse(
    val archiveId: Long,
    val teamId: Long,
    val writerId: Long,
    val title: String,
    val roadAddress: String?,
    val archiveImageUrl: String?,
    val averageRating: Double,
    val commentCount: Int,
)
