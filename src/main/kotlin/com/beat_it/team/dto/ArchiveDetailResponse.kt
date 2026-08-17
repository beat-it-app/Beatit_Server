package com.beat_it.team.dto

import com.beat_it.post.dto.CommentResponse

data class ArchiveDetailResponse(
    val archiveId: Long,
    val title: String,
    val placeName: String?,
    val description: String?,
    val archiveImageUrl: String?,
//    val location: LocationResponse?,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val isWriter: Boolean,
    val reaction: ArchiveReactionResponse,
    val commentList: List<CommentResponse>,
    val createdAt: String,
    val updatedAt: String,
)

data class LocationResponse(
    val locationId: Long,
    val locationName: String,
    val roadAddress: String,
    val latitude: String,
    val longitude: String,
)
