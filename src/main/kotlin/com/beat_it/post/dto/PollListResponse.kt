package com.beat_it.post.dto

data class PollListResponse(
    val pollListResponse: List<PollItems>,
    val totalCount: Int,
    val hasNext: Boolean
)

data class PollItems(
    val pollId: Long,
    val teamId: Long,
    val title: String,
    val closeAt: String,
    val pollCount: Int,
    val isVoted: Boolean,
)