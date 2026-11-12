package com.beat_it.performance.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공연 목록 응답 DTO")
data class PerformanceListResponse(
    @Schema(description = "진행 예정 공연 목록 (공연 날짜 오름차순)")
    val upcoming: List<PerformanceListItemResponse> = emptyList(),

    @Schema(description = "지나간 공연 목록 (공연 날짜 오름차순)")
    val past: List<PerformanceListItemResponse> = emptyList(),

    @Schema(description = "전체 데이터 개수", example = "25")
    val totalCount: Long,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean
)
