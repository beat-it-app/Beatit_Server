package com.beat_it.performance.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "공연 생성 요청 DTO")
data class PerformanceCreateRequest(
    @Schema(description = "공연 제목", example = "제 12회 정기 밴드 공연")
    val title: String,

    @Schema(description = "공연 일시", example = "2026-10-15T19:00:00+09:00")
    val performanceDateTime: OffsetDateTime,

    @Schema(description = "장소 ID (선택)", example = "1")
    val locationId: Long? = null,

    @Schema(description = "공연 장소명 (선택)", example = "홍대 롤링홀")
    val placeName: String? = null,

    @Schema(description = "공연 상세 설명 (200자 제한)", example = "비트잇 밴드의 12번째 정기 라이브 공연입니다.")
    val description: String? = null,

    @Schema(description = "예매 마감 일시 (사전/일반 예매 시 필수)", example = "2026-10-14T23:59:59+09:00")
    val bookingDeadline: OffsetDateTime? = null,

    @Schema(description = "예매 링크 (사전/일반 예매 시 필수)", example = "https://booking.example.com/beatit12")
    val bookingLink: String? = null,

    @Schema(description = "호스트 이름", example = "홍길동")
    val hostName: String,

    @Schema(description = "호스트 연락처", example = "010-1234-5678")
    val hostContact: String? = null,

    @Schema(description = "호스트 SNS/링크", example = "https://instagram.com/beatit_band")
    val hostLink: String? = null,

    @Schema(description = "티켓 가격 정보 목록 (1. 무료, 2. 사전예매, 3. 현장예매, 4. 사전+현장, 5. 일반예매)")
    val prices: List<PerformancePriceDto> = emptyList()
)
