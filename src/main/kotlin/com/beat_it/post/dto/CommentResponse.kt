package com.beat_it.post.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class CommentResponse(
    val commentId: Long,
    val parentCommentId: Long? = null,
    val writerName: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val profileImageUrl: String?,
    @get:JsonProperty("isWriter")
    val isWriter: Boolean,
    @get:JsonProperty("isMine")
    val isMine: Boolean,
    val mentionedUsers: List<MentionUserResponse> = emptyList(),
    val replies: List<CommentResponse> = emptyList()
)
