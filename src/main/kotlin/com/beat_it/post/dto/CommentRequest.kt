package com.beat_it.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

data class CommentRequest (
    @Schema(description = "댓글 내용", example = "확인했습니다 ~", required = true)
    val content: String
)
