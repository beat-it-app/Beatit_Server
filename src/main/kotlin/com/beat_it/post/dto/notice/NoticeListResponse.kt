package com.beat_it.post.dto.notice

import java.time.OffsetDateTime

data class NoticeListResponse(
    val noticeListResponse: List<NoticeItems>,
    val totalCount: Int,
    val hasNext: Boolean
)

data class NoticeItems(
    val noticeId: Long,
    val title: String,
    val description: String,
    val likeCount: Int,
    val dislikeCount: Int,
    val commentCount: Int,
    val createdAt: OffsetDateTime,
    val writer: String,
    val thumbnailUrl: String?
)
