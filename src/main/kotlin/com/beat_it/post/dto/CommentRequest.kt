package com.beat_it.post.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CommentRequest (
    @Schema(description = "댓글 내용", example = "@{김민주} 확인했습니다 ~", required = true)
    val content: String,
    @Schema(description = "부모 댓글 ID (대댓글인 경우 전달, 일반 댓글인 경우 null)", example = "10", required = false)
    val parentCommentId: Long? = null,
    @Schema(description = "드롭다운에서 선택된 멘션 유저 ID 목록 (선택 시에만 전달)", example = "[2]", required = false)
    val mentionedUserIds: List<Long>? = null
)
