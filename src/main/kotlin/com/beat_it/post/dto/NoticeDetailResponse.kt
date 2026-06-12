package com.beat_it.post.dto

data class NoticeDetailResponse(
    val noticeId: Long,
    val title: String,
    val content: String,
    val writerName: String,
    val writerProfileImageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val images: List<String>,
    val isWriter: Boolean,
    val reaction: NoticeReactionDto,
    val commentList: List<NoticeCommentDto>
)

data class NoticeReactionDto(
    val likeCount: Int,
    val dislikeCount: Int,
    val isLiked: Boolean,
    val isDisliked: Boolean,
    val commentCount: Int
)

data class NoticeCommentDto(
    val commentId: Long,
    val writerName: String,
    val content: String,
    val createdAt: String,
    val profileImageUrl: String?,
    val isWriter: Boolean,
    val isMine: Boolean
)
