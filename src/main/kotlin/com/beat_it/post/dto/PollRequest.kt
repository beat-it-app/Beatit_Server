package com.beat_it.post.dto

import com.beat_it.post.entity.enum.PollType
import java.time.OffsetDateTime

data class PollRequest(
    val title: String,
    val content: String?,
    val pollType: PollType,
    val pollList: List<PollItemRequest>,
    val allowMultipleChoice: Boolean?,
    val isAnonymous: Boolean?,
    val remindBeforeClose: Boolean?,
    val closesAt: OffsetDateTime?
)

data class PollItemRequest(
    val content: String? = null,
    val music: String? = null,
    val location: String? = null
)