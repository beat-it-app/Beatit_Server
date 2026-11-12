package com.beat_it.team.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ArchiveCommentResponse(
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
    val mentionedUsers: List<ArchiveMentionUserResponse> = emptyList(),
    val replies: List<ArchiveCommentResponse> = emptyList(),
)
