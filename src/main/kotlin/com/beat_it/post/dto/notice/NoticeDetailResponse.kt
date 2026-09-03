package com.beat_it.post.dto.notice

import com.beat_it.post.dto.CommentResponse
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
    val isWriter: Boolean,
    val reaction: NoticeReactionDto,
    val commentList: List<CommentResponse>
)

data class NoticeReactionDto(
    val likeCount: Int,
    val dislikeCount: Int,
    val isLiked: Boolean,
    val isDisliked: Boolean,
    val commentCount: Int
)