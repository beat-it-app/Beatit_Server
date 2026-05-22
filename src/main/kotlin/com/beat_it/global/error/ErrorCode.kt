package com.beat_it.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // --- 공통 에러 (COMMON) ---
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON-001", "요청 본문이 올바르지 않습니다."),
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "COMMON-002", "로그인이 필요합니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "찾을 수 없는 리소스입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-004", "서버 내부 오류가 발생했습니다."),

    // --- 일정 관련 에러 (CALENDAR) ---
    CALENDAR_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "CALENDAR-001", "변경할 내용이 없습니다."),
    CALENDAR_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "CALENDAR-002", "종료 시각은 시작 시각 이후여야 합니다."),
    CALENDAR_NO_PERMISSION(HttpStatus.FORBIDDEN, "CALENDAR-003", "일정에 대한 권한이 없습니다."),
    CALENDAR_TEAM_MISMATCH(HttpStatus.FORBIDDEN, "CALENDAR-005", "현재 팀에서 조회할 수 없는 일정입니다."),
    CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR-006", "일정을 찾을 수 없습니다."),
    CALENDAR_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "CALENDAR-007", "일정명은 필수입니다."),
    CALENDAR_START_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "CALENDAR-008", "일정 시작 시각은 필수입니다."),
    CALENDAR_END_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "CALENDAR-009", "일정 종료 시각은 필수입니다."),

    // --- 팀 관련 에러 (TEAM) ---
    TEAM_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "TEAM-001", "팀에 대해 변경할 내용이 업습니다."),
    TEAM_NO_PERMISSION(HttpStatus.FORBIDDEN, "TEAM-002", "팀 조회 권한이 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM-003", "팀을 찾을 수 없습니다."),
    TEAM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "TEAM-004", "팀 이름은 필수입니다."),
    TEAM_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "TEAM-005", "팀 이름은 100자 이하여야 합니다."),
    TEAM_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "TEAM-006", "팀 설명은 500자 이하여야 합니다."),
    TEAM_NO_UPDATE_PERMISSION(HttpStatus.FORBIDDEN, "TEAM-007","팀 수정 권한이 없습니다."),


    // --- 장소 관련 에러 ---
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION-001", "장소를 찾을 수 없습니다."),

    // --- 멤버 관련 에러 ---
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "참여 사용자를 찾을 수 없습니다."),
}