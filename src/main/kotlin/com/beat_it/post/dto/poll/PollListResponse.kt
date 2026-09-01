package com.beat_it.post.dto.poll

data class PollListResponse(
    val pollListInProgress: List<PollItems>,
    val pollListClosed: List<PollItems>,
    val totalCount: Int
)

data class PollItems(
    val pollId: Long,
    val title: String,
    val closeAt: String,
    val pollCount: Int,
    val isVoted: Boolean,
)