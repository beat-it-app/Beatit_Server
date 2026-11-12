package com.beat_it.post.dto.meetit

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalTime
import java.time.LocalDateTime

@Schema(description = "밋잇 생성 요청 DTO")
data class MeetitCreateRequest(
    @field:Schema(description = "모임 제목", example = "4월 둘째주 정기 회의")
    val title: String,

    @field:Schema(description = "후보 날짜 목록 (yyyy-MM-dd)", example = "[\"2026-08-15\", \"2026-08-16\"]")
    val candidateDates: List<String>,

    @field:Schema(description = "조율 시작 시간 (HH:mm, 30분 단위. dateOnly=true일 경우 null)", example = "09:00")
    val startTime: LocalTime? = null,

    @field:Schema(description = "조율 종료 시간 (HH:mm, 30분 단위. dateOnly=true일 경우 null)", example = "18:00")
    val endTime: LocalTime? = null,

    @field:Schema(description = "날짜만 조율 여부 (기본값 false)", example = "false")
    val dateOnly: Boolean = false,

    @field:Schema(description = "참여 대상 유저 ID 목록", example = "[1, 2, 3]")
    val participantUserIds: List<Long>
)

@Schema(description = "밋잇 응답/수정 제출 DTO")
data class MeetitSubmissionRequest(
    @field:Schema(
        description = "선택한 시간 슬롯 목록 (일반: yyyy-MM-ddTHH:mm:00, 날짜 전용: yyyy-MM-ddT00:00:00)",
        example = "[\"2026-08-15T09:00:00\", \"2026-08-15T09:30:00\"]"
    )
    val slotStartTimes: List<LocalDateTime>
)
