package com.beat_it.post.dto.poll

import java.time.OffsetDateTime

data class PollListResponse(
    val pollListInProgress: List<PollItems>,
    val pollListClosed: List<PollItems>,
    val totalCount: Int,
    val hasNext: Boolean
)

data class PollItems(
    val pollId: Long,
    val title: String,
    val closeAt: OffsetDateTime?,
    val pollCount: Int,
    val isVoted: Boolean,
)