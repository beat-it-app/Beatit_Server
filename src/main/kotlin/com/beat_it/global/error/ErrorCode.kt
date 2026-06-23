package com.beat_it.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // --- 공통 에러 (COMMON) ---
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON-001", "요청 본문이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-002", "로그인이 필요한 서비스입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "찾을 수 없는 리소스입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-004", "서버 내부 오류가 발생했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-005", "해당 유저를 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-006", "해당 요청에 대한 접근 권한이 없습니다."),
    NOT_AUTHOR(HttpStatus.FORBIDDEN, "COMMON-007", "해당 리소스를 편집할 권한이 없습니다."),

    // --- 1. 사용자 관련 에러 (AUTH - SIGNUP/LOGIN/USER)  ---
    IDENTIFIER_DUPLICATED(HttpStatus.BAD_REQUEST, "SIGNUP-001", "이미 사용 중인 아이디입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SIGNUP-002", "이메일 인증번호 발송에 실패했습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "SIGNUP-003", "인증 시간이 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "SIGNUP-004", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIGNUP-005", "진행 중인 인증 요청을 찾을 수 없습니다."),
    MISSING_PROVIDER(HttpStatus.BAD_REQUEST, "SIGNUP-006", "소셜 회원가입 시 제공자 정보는 필수입니다."),
    MISSING_IDENTIFIER(HttpStatus.BAD_REQUEST, "SIGNUP-007", "일반 회원가입 시 아이디는 필수입니다."),
    MISSING_PASSWORD(HttpStatus.BAD_REQUEST, "SIGNUP-008", "일반 회원가입 시 비밀번호는 필수입니다."),

    IDENTIFIER_NOT_FOUND(HttpStatus.NOT_FOUND, "LOGIN-001", "존재하지 않는 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "LOGIN-002", "비밀번호가 일치하지 않습니다."),
    PROFILE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "LOGIN-003", "이미 프로필이 존재합니다."),
    INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "LOGIN-004", "프로필 이름은 1자 이상 10자 이하로 입력해주세요."),

    // --- 4. 일정 관련 에러 (CALENDAR) ---
    CALENDAR_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "CALENDAR-001", "변경할 내용이 없습니다."),
    CALENDAR_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "CALENDAR-002", "종료 시각은 시작 시각 이후여야 합니다."),
    CALENDAR_NO_PERMISSION(HttpStatus.FORBIDDEN, "CALENDAR-003", "일정에 대한 권한이 없습니다."),
    CALENDAR_TEAM_MISMATCH(HttpStatus.FORBIDDEN, "CALENDAR-005", "현재 팀에서 조회할 수 없는 일정입니다."),
    CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR-006", "일정을 찾을 수 없습니다."),
    CALENDAR_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "CALENDAR-007", "일정명은 필수입니다."),
    CALENDAR_START_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "CALENDAR-008", "일정 시작 시각은 필수입니다."),
    CALENDAR_END_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "CALENDAR-009", "일정 종료 시각은 필수입니다."),
    CALENDAR_INVALID_YEAR(HttpStatus.BAD_REQUEST, "CALENDAR-010", "연도 값이 올바르지 않습니다."),
    CALENDAR_INVALID_MONTH(HttpStatus.BAD_REQUEST, "CALENDAR-011", "월 값은 1~12 사이여야 합니다."),
    CALENDAR_NON_EXISTENT_DATE(HttpStatus.BAD_REQUEST, "CALENDAR-013", "해당 연월에 존재하지 않는 날짜입니다."),

    // --- 3. 팀 관련 에러 (TEAM) ---
    TEAM_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "TEAM-001", "팀에 대해 변경할 내용이 업습니다."),
    // FIXME : TEAM_NO_PERMISSION 대신 NOT_TEAM_MEMBER로 사용하는 게 어떤지! 더 직관적이고 확장 가능성이 있어보임
    TEAM_NO_PERMISSION(HttpStatus.FORBIDDEN, "TEAM-002", "팀 조회 권한이 없습니다."),
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "TEAM-002", "해당 팀의 멤버가 아닙니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM-003", "팀을 찾을 수 없습니다."),
    TEAM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "TEAM-004", "팀 이름은 필수입니다."),
    TEAM_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "TEAM-005", "팀 이름은 100자 이하여야 합니다."),
    TEAM_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "TEAM-006", "팀 설명은 500자 이하여야 합니다."),
    TEAM_NO_UPDATE_PERMISSION(HttpStatus.FORBIDDEN, "TEAM-007","팀 수정 권한이 없습니다."),
    TEAM_INVITE_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "TEAM-008", "초대 코드가 입력되지 않았습니다."),
    TEAM_INVITE_CODE_INVALID(HttpStatus.BAD_REQUEST, "TEAM-009", "유효하지 않은 초대 코드입니다."),
    TEAM_INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM-010", "존재하지 않는 팀 초대 코드입니다."),
    TEAM_ALREADY_JOINED(HttpStatus.CONFLICT, "TEAM-011", "이미 가입된 팀입니다."),
    TEAM_PENDING_DELETION(HttpStatus.BAD_REQUEST,  "TEAM-012", "이미 삭제 요청된 팀입니다."),
    TEAM_NOT_SELECTED(HttpStatus.BAD_REQUEST, "TEAM-013", "현재 선택된 팀이 없습니다. 팀을 먼저 선택해주세요."),
    
    // --- 4. 채팅 관련 에러 (CHAT) ---
    CHAT_ROOM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "CHAT-001", "채팅방 이름은 필수입니다."),
    INVALID_TEAM_PARTICIPANTS(HttpStatus.BAD_REQUEST, "CHAT-002", "팀 내 속해있는 유저만 초대 가능합니다."),
    // --- 5. 공지/투표 관련 에러 (POST) ---
    TITLE_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "POST-001", "제목과 내용은 필수 입력 사항입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-002", "이미 삭제되었거나 존재하지 않는 공지입니다."),
    POST_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "POST-003", "변경된 내용이 없습니다."),
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "POST-004", "이미 좋아요를 누르셨습니다."),
    ALREADY_DISLIKED(HttpStatus.BAD_REQUEST, "POST-005", "이미 싫어요를 누르셨습니다."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "POST-006", "댓글 내용은 공백일 수 없습니다."),

    // --- 장소 관련 에러 ---
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION-001", "장소를 찾을 수 없습니다."),

    // --- metadata 관련 에러 (file) ---
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "FILE-001", "업로드할 파일이 비어있습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FILE-002", "지원하지 않는 파일 확장자입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE-003", "파일 용량이 제한을 초과했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-004", "서버 오류로 인해 파일 업로드에 실패했습니다."),

    // --- 멤버 관련 에러 ---
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "참여 사용자를 찾을 수 없습니다."),
}