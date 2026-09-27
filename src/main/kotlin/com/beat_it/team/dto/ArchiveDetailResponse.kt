package com.beat_it.team.dto

import java.time.OffsetDateTime

data class ArchiveDetailResponse(
    val archiveId: Long,
    val teamId: Long,
    val writerId: Long,
    val title: String,
    val roadAddress: String?,
    val locationId: Long,
    val description: String?,
    val archiveImageUrls: List<String>,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val isWriter: Boolean,
    val topArchive: Boolean,
    val rating: ArchiveRatingResponse,
    val commentCount: Int,
    val commentList: List<ArchiveCommentResponse>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

data class ArchiveRatingResponse(
    val averageRating: Double,
    val ratingCount: Int,
    val myRating: Int?,
)
