package com.beat_it.post.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CommentRequest (
    @Schema(description = "댓글 내용", example = "@{김민주} 확인했습니다 ~", required = true)
    val content: String,
    @Schema(description = "드롭다운에서 선택된 멘션 유저 ID 목록 (선택 시에만 전달)", example = "[2]", required = false)
    val mentionedUserIds: List<Long>? = null
)
