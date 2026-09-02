package com.beat_it.team.dto

import com.beat_it.post.dto.CommentResponse

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
    val rating: ArchiveRatingResponse,
    val commentCount: Int,
    val commentList: List<CommentResponse>,
    val createdAt: String,
    val updatedAt: String,
)
