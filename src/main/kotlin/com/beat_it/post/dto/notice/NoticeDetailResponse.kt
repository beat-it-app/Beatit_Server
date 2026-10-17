package com.beat_it.post.dto.notice

import com.beat_it.post.dto.CommentResponse
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class NoticeDetailResponse(
    val noticeId: Long,
    val title: String,
    val content: String,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val images: List<String>,
    @get:JsonProperty("isWriter")
    val isWriter: Boolean,
    val reaction: NoticeReactionDto,
    val commentList: List<CommentResponse>
)

data class NoticeReactionDto(
    val likeCount: Int,
    val dislikeCount: Int,
    @get:JsonProperty("isLiked")
    val isLiked: Boolean,
    @get:JsonProperty("isDisliked")
    val isDisliked: Boolean,
    val commentCount: Int
)