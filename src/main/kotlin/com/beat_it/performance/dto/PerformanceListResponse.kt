package com.beat_it.performance.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공연 목록 페이징 응답 DTO")
data class PerformanceListResponse(
    @Schema(description = "공연 목록")
    val performances: List<PerformanceListItemResponse>,

    @Schema(description = "전체 개수", example = "25")
    val totalCount: Long,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean
)
