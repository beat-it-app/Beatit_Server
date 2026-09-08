package com.beat_it.team.dto

import java.time.OffsetDateTime

data class ArchiveCommentResponse(
    val commentId: Long,
    val parentCommentId: Long? = null,
    val writerName: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val profileImageUrl: String?,
    val isWriter: Boolean,
    val isMine: Boolean,
    val mentionedUsers: List<ArchiveMentionUserResponse> = emptyList(),
    val replies: List<ArchiveCommentResponse> = emptyList(),
)
