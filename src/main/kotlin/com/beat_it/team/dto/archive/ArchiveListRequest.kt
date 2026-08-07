package com.beat_it.team.dto.archive

data class TeamArchiveListResponse(
    val archives: List<ArchiveSimpleInfo>,
)

data class ArchiveSimpleInfo(
    val archiveId: Long,
    val title: String,
    val placeName: String?,
    val archiveImageUrl: String?,
    val likeCount: Int,
    val dislikeCount: Int,
    val commentCount: Int,
    val createdAt: String,
)