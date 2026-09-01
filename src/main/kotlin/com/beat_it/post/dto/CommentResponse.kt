package com.beat_it.post.dto

data class CommentResponse(
    val commentId: Long,
    val writerName: String,
    val content: String,
    val createdAt: String,
    val profileImageUrl: String?,
    val isWriter: Boolean,
    val isMine: Boolean,
    val mentionedUsers: List<MentionUserResponse> = emptyList()
)
