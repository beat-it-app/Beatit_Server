package com.beat_it.performance.dto

import com.beat_it.performance.entity.Performances
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Schema(description = "공연 목록 아이템 DTO")
data class PerformanceListItemResponse(
    @Schema(description = "공연 Public UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    val performancePublicId: UUID,

    @Schema(description = "공연 제목", example = "제 12회 정기 밴드 공연")
    val title: String,

    @Schema(description = "공연 일시", example = "2026-10-15T19:00:00+09:00")
    val performanceDateTime: OffsetDateTime,

    @Schema(description = "포스터 이미지 URL", example = "https://cdn.example.com/poster.png")
    val posterImageUrl: String?
) {
    companion object {
        fun of(
            performance: Performances,
            posterImageUrl: String? = null
        ): PerformanceListItemResponse {
            return PerformanceListItemResponse(
                performancePublicId = performance.publicId,
                title = performance.title,
                performanceDateTime = performance.performanceDateTime,
                posterImageUrl = posterImageUrl
            )
        }
    }
}
