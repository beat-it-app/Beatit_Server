package com.beat_it.post.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MentionUserResponse(
    @Schema(description = "멘션된 사용자 ID", example = "1")
    val userId: Long,
    @Schema(description = "멘션된 사용자 이름", example = "김민주")
    val name: String,
    @Schema(description = "멘션된 사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    val profileImageUrl: String? = null
)
