package com.beat_it.performance.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

@Schema(description = "공연 수정 요청 DTO")
data class PerformanceUpdateRequest(
    @Schema(example = "제 12회 정기 밴드 공연 (수정)")
    val title: String? = null,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(example = "2026-10-15T20:00:00+09:00")
    val performanceDateTime: OffsetDateTime? = null,

    @Schema(example = "1")
    val locationId: Long? = null,

    @Schema(example = "홍대 KT&G 상상마당")
    val placeName: String? = null,

    @field:Size(max = 200)
    val description: String? = null,

    @field:Size(max = 1000)
    val detailInfo: String? = null,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val bookingDeadline: OffsetDateTime? = null,

    val bookingLink: String? = null,

    val hostName: String? = null,

    val hostContact: String? = null,

    val hostLink: String? = null,

    val prices: List<PerformancePriceDto>? = null,

    @Schema(description = "포스터 이미지 삭제 여부 (true인 경우 기존 포스터 삭제)", example = "false")
    val deletePosterImage: Boolean? = false,

    @Schema(description = "보존할 상세 이미지 URL 목록 (null: 기존 유지/deleteDetailFileIds 동작, []: 전체 삭제, 값 지정: 해당 URL만 보존)")
    val retainDetailImageUrls: List<String>? = null,

    @Schema(description = "보존할 상세 이미지 파일 ID 목록")
    val retainDetailFileIds: List<Long>? = null,

    @Schema(description = "삭제할 상세 이미지 파일 ID 목록")
    val deleteDetailFileIds: List<Long>? = null,

    val invitationCard: InvitationCardRequest? = null,
)
