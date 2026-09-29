package com.beat_it.performance.dto

import com.beat_it.location.dto.LocationResponse
import com.beat_it.performance.entity.Performances
import com.beat_it.performance.entity.enum.PerformanceStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Schema(description = "공연 상세/생성/수정 통합 응답 DTO")
data class PerformanceResponse(
    @Schema(description = "공연 ID", example = "1")
    val performanceId: Long,

    @Schema(description = "공연 Public UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    val publicId: UUID,

    @Schema(description = "팀 ID", example = "10")
    val teamId: Long,

    @Schema(description = "팀 이름", example = "비트잇 밴드")
    val teamName: String?,

    @Schema(description = "작성자(생성자) 유저 ID", example = "5")
    val createdUserId: Long,

    @Schema(description = "공연 제목", example = "제 12회 정기 밴드 공연")
    val title: String,

    @Schema(description = "공연 일시", example = "2026-10-15T19:00:00+09:00")
    val performanceDateTime: OffsetDateTime,

    @Schema(description = "장소 상세 정보 (위치 등록된 경우)")
    val location: LocationResponse?,

    @Schema(description = "공연 장소명", example = "홍대 롤링홀")
    val placeName: String?,

    @Schema(description = "포스터 파일 정보")
    val poster: PerformanceFileResponse?,

    @Schema(description = "공연 상세 이미지 목록 (0~5장)")
    val detailImages: List<PerformanceFileResponse>,

    @Schema(description = "공연 상세 설명 (200자 제한)")
    val description: String?,

    @Schema(description = "공연 상세 정보 (최대 1,000자)")
    val detailInfo: String?,

    @Schema(description = "예매 마감 일시")
    val bookingDeadline: OffsetDateTime?,

    @Schema(description = "예매 링크")
    val bookingLink: String?,

    @Schema(description = "호스트 이름", example = "홍길동")
    val hostName: String,

    @Schema(description = "호스트 연락처", example = "010-1234-5678")
    val hostContact: String?,

    @Schema(description = "호스트 SNS/링크", example = "https://instagram.com/beatit_band")
    val hostLink: String?,

    @Schema(description = "티켓 가격 정보 목록")
    val prices: List<PerformancePriceDto>,

    @Schema(description = "공연 상태 (UPCOMING, PAST)")
    val performanceStatus: PerformanceStatus,

    @Schema(description = "생성 일시")
    val createdAt: OffsetDateTime,

    @Schema(description = "수정 일시")
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun of(
            performance: Performances,
            teamName: String? = null,
            locationResponse: LocationResponse?,
            poster: PerformanceFileResponse?,
            detailImages: List<PerformanceFileResponse>,
            prices: List<PerformancePriceDto>
        ): PerformanceResponse {
            return PerformanceResponse(
                performanceId = performance.performanceId!!,
                publicId = performance.publicId,
                teamId = performance.teamId,
                teamName = teamName,
                createdUserId = performance.createdUserId,
                title = performance.title,
                performanceDateTime = performance.performanceDateTime,
                location = locationResponse,
                placeName = performance.placeName,
                poster = poster,
                detailImages = detailImages,
                description = performance.description,
                detailInfo = performance.detailInfo,
                bookingDeadline = performance.bookingDeadline,
                bookingLink = performance.bookingLink,
                hostName = performance.hostName,
                hostContact = performance.hostContact,
                hostLink = performance.hostLink,
                prices = prices,
                performanceStatus = performance.performanceStatus,
                createdAt = performance.createdAt,
                updatedAt = performance.updatedAt
            )
        }
    }
}
