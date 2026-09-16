package com.beat_it.performance.dto

import com.beat_it.performance.entity.enum.PerformanceStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "공연 수정 요청 DTO")
data class PerformanceUpdateRequest(
    @Schema(description = "공연 제목", example = "제 12회 정기 밴드 공연 (수정)")
    val title: String? = null,

    @Schema(description = "공연 일시", example = "2026-10-15T20:00:00+09:00")
    val performanceDateTime: OffsetDateTime? = null,

    @Schema(description = "장소 ID", example = "1")
    val locationId: Long? = null,

    @Schema(description = "공연 장소명", example = "홍대 KT&G 상상마당")
    val placeName: String? = null,

    @Schema(description = "공연 상세 설명 (200자 제한)")
    val description: String? = null,

    @Schema(description = "예매 마감 일시")
    val bookingDeadline: OffsetDateTime? = null,

    @Schema(description = "예매 링크")
    val bookingLink: String? = null,

    @Schema(description = "호스트 이름")
    val hostName: String? = null,

    @Schema(description = "호스트 연락처")
    val hostContact: String? = null,

    @Schema(description = "호스트 SNS/링크")
    val hostLink: String? = null,

    @Schema(description = "공연 상태 (PUBLISHED, CLOSED)")
    val performanceStatus: PerformanceStatus? = null,

    @Schema(description = "티켓 가격 정보 목록 (수정 시 전체 교체)")
    val prices: List<PerformancePriceDto>? = null,

    @Schema(description = "삭제할 상세 이미지 파일 ID 목록")
    val deleteDetailFileIds: List<Long>? = null
)
