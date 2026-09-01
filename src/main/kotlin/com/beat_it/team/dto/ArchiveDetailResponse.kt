package com.beat_it.team.dto

import com.beat_it.post.dto.CommentResponse
import java.math.BigDecimal

data class ArchiveDetailResponse(
    val archiveId: Long,
    val teamId: Long,
    val writerId: Long,
    val title: String,
    val placeName: String?,
    val locationId: Long,
    val description: String?,
    val archiveImageUrls: List<String>,
    val location: ArchiveLocationResponse,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val isWriter: Boolean,
    val rating: ArchiveRatingResponse,
    val commentCount: Int,
    val commentList: List<CommentResponse>,
    val createdAt: String,
    val updatedAt: String,
)

data class ArchiveLocationResponse(
    val locationId: Long,
    val locationName: String?,
    val roadAddress: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val mapUrl: String?,
)
