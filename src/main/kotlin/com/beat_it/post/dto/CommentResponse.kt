package com.beat_it.post.dto

import java.time.OffsetDateTime

data class CommentResponse(
    val commentId: Long,
    val parentCommentId: Long? = null,
    val writerName: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val profileImageUrl: String?,
    val isWriter: Boolean,
    val isMine: Boolean,
    val mentionedUsers: List<MentionUserResponse> = emptyList(),
    val replies: List<CommentResponse> = emptyList()
)
