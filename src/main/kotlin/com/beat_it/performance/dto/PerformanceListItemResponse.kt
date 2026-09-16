package com.beat_it.performance.dto

import com.beat_it.performance.entity.Performances
import com.beat_it.performance.entity.enum.PerformanceStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Schema(description = "공연 목록 아이템 DTO")
data class PerformanceListItemResponse(
    @Schema(description = "공연 ID", example = "1")
    val performanceId: Long,

    @Schema(description = "공연 Public UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    val publicId: UUID,

    @Schema(description = "팀 ID", example = "10")
    val teamId: Long,

    @Schema(description = "공연 제목", example = "제 12회 정기 밴드 공연")
    val title: String,

    @Schema(description = "공연 일시", example = "2026-10-15T19:00:00+09:00")
    val performanceDateTime: OffsetDateTime,

    @Schema(description = "공연 장소명", example = "홍대 롤링홀")
    val placeName: String?,

    @Schema(description = "포스터 이미지 URL", example = "https://cdn.example.com/poster.png")
    val posterImageUrl: String?,

    @Schema(description = "예매 마감 일시")
    val bookingDeadline: OffsetDateTime?,

    @Schema(description = "티켓 가격 정보 목록")
    val prices: List<PerformancePriceDto>,

    @Schema(description = "공연 상태 (PUBLISHED, CLOSED)")
    val performanceStatus: PerformanceStatus,

    @Schema(description = "등록 일시")
    val createdAt: OffsetDateTime
) {
    companion object {
        fun of(
            performance: Performances,
            prices: List<PerformancePriceDto>,
            posterImageUrl: String? = null
        ): PerformanceListItemResponse {
            return PerformanceListItemResponse(
                performanceId = performance.performanceId!!,
                publicId = performance.publicId,
                teamId = performance.teamId,
                title = performance.title,
                performanceDateTime = performance.performanceDateTime,
                placeName = performance.placeName,
                posterImageUrl = posterImageUrl,
                bookingDeadline = performance.bookingDeadline,
                prices = prices,
                performanceStatus = performance.performanceStatus,
                createdAt = performance.createdAt
            )
        }
    }
}
