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
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-008", "올바르지 않은 입력값입니다."),

    // --- 1. 사용자 관련 에러 (AUTH - SIGNUP/LOGIN/USER)  ---
    IDENTIFIER_DUPLICATED(HttpStatus.BAD_REQUEST, "SIGNUP-001", "이미 사용 중인 아이디입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SIGNUP-002", "이메일 인증번호 발송에 실패했습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "SIGNUP-003", "인증 시간이 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "SIGNUP-004", "인증번호가 일치하지 않습니다."),
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIGNUP-005", "진행 중인 인증 요청을 찾을 수 없습니다."),
    MISSING_PROVIDER(HttpStatus.BAD_REQUEST, "SIGNUP-006", "소셜 회원가입 시 제공자 정보는 필수입니다."),
    MISSING_IDENTIFIER(HttpStatus.BAD_REQUEST, "SIGNUP-007", "일반 회원가입 시 아이디는 필수입니다."),
    MISSING_PASSWORD(HttpStatus.BAD_REQUEST, "SIGNUP-008", "일반 회원가입 시 비밀번호는 필수입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SIGNUP-009", "인증되지 않은 이메일입니다."),
    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "SIGNUP-010", "이미 가입된 이메일입니다."),

    IDENTIFIER_NOT_FOUND(HttpStatus.NOT_FOUND, "LOGIN-001", "존재하지 않는 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "LOGIN-002", "비밀번호가 일치하지 않습니다."),
    PROFILE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "LOGIN-003", "이미 프로필이 존재합니다."),
    INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "LOGIN-004", "프로필 이름은 2자 이상 10자 이하로 입력해주세요."),
    ALREADY_DEFAULT_PROFILE(HttpStatus.BAD_REQUEST, "LOGIN-005", "이미 기본 프로필 이미지 상태입니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "LOGIN-006", "유효하지 않은 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "LOGIN-007", "만료된 Refresh 토큰입니다. 다시 로그인 해주세요."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "LOGIN-008", "존재하지 않거나 만료된 Refresh 토큰입니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "LOGIN-009", "프로필이 생성되지 않았습니다."),
    DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "LOGIN-010", "기존 이름과 동일한 이름으로 변경할 수 없습니다."),

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
    TEAM_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "TEAM-001", "팀에 대해 변경할 내용이 없습니다."),
    TEAM_UNAVAILABLE(HttpStatus.NOT_FOUND, "TEAM-002", "이미 삭제되었거나 존재하지 않는 팀입니다."),
    TEAM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "TEAM-003", "팀 이름은 필수입니다."),
    TEAM_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "TEAM-004", "팀 이름은 100자 이하여야 합니다."),
    TEAM_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "TEAM-005", "팀 설명은 500자 이하여야 합니다."),
    TEAM_NO_UPDATE_PERMISSION(HttpStatus.FORBIDDEN, "TEAM-006", "팀 수정 권한이 없습니다."),
    TEAM_INVITE_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "TEAM-007", "초대 코드가 입력되지 않았습니다."),
    TEAM_INVITE_CODE_INVALID(HttpStatus.BAD_REQUEST, "TEAM-008", "유효하지 않은 팀 초대 코드입니다."),
    TEAM_INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM-009", "존재하지 않는 팀 초대 코드입니다."),
    TEAM_ALREADY_JOINED(HttpStatus.CONFLICT, "TEAM-010", "이미 팀에 가입된 상태입니다."),
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "TEAM-011", "해당 팀의 멤버가 아닙니다."),
    TEAM_NOT_SELECTED(HttpStatus.BAD_REQUEST, "TEAM-012", "현재 선택된 팀이 없습니다. 팀을 먼저 선택해주세요."),
    TEAM_NO_DELETE_PERMISSION(HttpStatus.FORBIDDEN, "TEAM-013", "팀 삭제 권한이 없습니다."),
    TEAM_LEADER_CANNOT_WITHDRAW(HttpStatus.BAD_REQUEST, "TEAM-014", "리더는 팀을 탈퇴할 수 없습니다. 권한을 양도하거나 팀을 삭제해주세요."),

    // --- 4. 연습실 관련 에러 (Archive) ---
    ARCHIVE_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "ARCHIVE-001", "연습실에 대해 변경할 내용이 없습니다."),
    ARCHIVE_NO_PERMISSION(HttpStatus.FORBIDDEN, "ARCHIVE-002", "현재 팀에서 접근할 수 없는 연습실입니다."),
    ARCHIVE_NOT_FOUND(HttpStatus.NOT_FOUND, "ARCHIVE-003", "연습실을 찾을 수 없습니다."),
    ARCHIVE_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "ARCHIVE-004", "연습실 제목은 필수입니다."),
    ARCHIVE_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "ARCHIVE-005", "연습실 제목은 100자 이하여야 합니다."),
    ARCHIVE_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "ARCHIVE-006", "연습실 설명은 500자 이하여야 합니다."),
    ARCHIVE_NO_UPDATE_PERMISSION(HttpStatus.FORBIDDEN, "ARCHIVE-007", "연습실 수정 권한이 없습니다."),
    ARCHIVE_NO_DELETE_PERMISSION(HttpStatus.FORBIDDEN, "ARCHIVE-008", "연습실 삭제 권한이 없습니다."),
    ARCHIVE_LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "ARCHIVE-009", "연습실 위치 정보는 필수입니다."),
    ARCHIVE_PLACE_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "ARCHIVE-010", "연습실 장소명은 100자 이하여야 합니다."),
    ARCHIVE_IMAGE_URL_TOO_LONG(HttpStatus.BAD_REQUEST, "ARCHIVE-011", "연습실 이미지 URL은 500자 이하여야 합니다."),

    // --- 4. 채팅 관련 에러 (CHAT) ---
    CHAT_ROOM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "CHAT-001", "채팅방 이름은 필수입니다."),
    INVALID_TEAM_PARTICIPANTS(HttpStatus.BAD_REQUEST, "CHAT-002", "팀 내 속해있는 유저만 초대 가능합니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT-003", "채팅방을 찾을 수 없습니다."),
    INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "CHAT-004", "올바르지 않은 채팅 메시지 타입입니다."),
    CHAT_MESSAGE_REQUIRED(HttpStatus.BAD_REQUEST, "CHAT-005", "텍스트 메시지 내용은 필수입니다."),
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT-006", "채팅방 참여자를 찾을 수 없습니다."),

    // --- 5. 공지/투표 관련 에러 (POST) ---
    TITLE_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "POST-001", "제목과 내용은 필수 입력 사항입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-002", "이미 삭제되었거나 존재하지 않는 공지입니다."),
    POST_NO_CONTENT_TO_UPDATE(HttpStatus.BAD_REQUEST, "POST-003", "변경된 내용이 없습니다."),
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "POST-004", "이미 좋아요를 누르셨습니다."),
    ALREADY_DISLIKED(HttpStatus.BAD_REQUEST, "POST-005", "이미 싫어요를 누르셨습니다."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "POST-006", "댓글 내용은 공백일 수 없습니다."),
    POLL_CLOSED(HttpStatus.BAD_REQUEST, "POST-007", "이미 종료된 투표입니다."),
    POLL_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-008", "존재하지 않는 투표 항목입니다."),
    POLL_MULTIPLE_CHOICE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "POST-009", "중복 투표가 허용되지 않는 투표입니다."),

    // --- 6. 모임 조율 관련 에러 (MEETIT) ---
    MEETIT_TEAM_MISMATCH(HttpStatus.FORBIDDEN, "MEETIT-001", "현재 팀에서 접근할 수 없는 모임 조율입니다."),
    MEETIT_NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "MEETIT-002", "해당 모임 조율의 참여 대상이 아닙니다."),

    // --- 장소 관련 에러 ---
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION-001", "장소를 찾을 수 없습니다."),

    // --- metadata 관련 에러 (file) ---
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "FILE-001", "업로드할 파일이 비어있습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FILE-002", "지원하지 않는 파일 확장자입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE-003", "파일 용량이 제한을 초과했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-004", "서버 오류로 인해 파일 업로드에 실패했습니다."),
    FILE_REQUIRED(HttpStatus.BAD_REQUEST, "FILE-005", "미디어 타입 전송 시 파일 첨부는 필수입니다."),

    // --- 멤버 관련 에러 ---
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "참여 사용자를 찾을 수 없습니다."),

    // --- 팀 클라우드 관련 에러 (TEAM_CLOUD) ---
    TEAM_CLOUD_FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_CLOUD-001", "팀 클라우드 폴더를 찾을 수 없습니다."),
    TEAM_CLOUD_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_CLOUD-002", "팀 클라우드 아이템을 찾을 수 없습니다."),
    TEAM_CLOUD_FOLDER_TEAM_MISMATCH(HttpStatus.FORBIDDEN, "TEAM_CLOUD-003", "해당 팀의 폴더가 아닙니다."),
    TEAM_CLOUD_ITEM_TEAM_MISMATCH(HttpStatus.FORBIDDEN, "TEAM_CLOUD-004", "해당 팀 클라우드 아이템의 팀 정보가 일치하지 않습니다."),
    TEAM_CLOUD_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_CLOUD-005", "해당 파일 아이템을 찾을 수 없습니다."),
    TEAM_CLOUD_STORAGE_EXCEEDED(HttpStatus.BAD_REQUEST, "TEAM_CLOUD-006", "팀 클라우드 용량(10GB)이 초과되었습니다."),
    TEAM_CLOUD_FOLDER_ALREADY_EXISTS(HttpStatus.CONFLICT, "TEAM_CLOUD-006","이미 존재하는 폴더 이름입니다."),
    TEAM_CLOUD_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TEAM_CLOUD-007", "파일 업로드에 실패했습니다."),


}