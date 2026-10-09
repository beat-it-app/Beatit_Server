package com.beat_it.notification.template


enum class NotificationCategory(val description: String) {
    TEAM("팀"),
    CALENDAR("공유캘린더"),
    NOTICE("공지사항"),
    POLL("투표"),
    MEETIT("밋잇"),
    POST("게시글"),
    CHAT("채팅")
}

enum class NotificationType(
    val category: NotificationCategory,
    val defaultTitle: String,
    val isPush: Boolean,
    val isToast: Boolean
) {
    // 2. 팀 생성/가입
    MEMBER_JOIN(NotificationCategory.TEAM, "멤버 가입", true, true),
    MEMBER_LEAVE(NotificationCategory.TEAM, "멤버 탈퇴", true, true),

    // 3. 팀 페이지
    PART_ASSIGNED(NotificationCategory.TEAM, "파트 배정", true, true),
    STUDIO_REGISTERED(NotificationCategory.TEAM, "새로운 합주실/연습실 등록", true, true),

    // 5. 공유캘린더
    SCHEDULE_REGISTERED(NotificationCategory.CALENDAR, "일정 등록", true, true),
    SCHEDULE_REMINDER(NotificationCategory.CALENDAR, "일정 알림", true, true),

    // 6-1. 공지사항 / 투표 / 밋잇 (댓글 / 반응)
    NOTICE_REGISTERED(NotificationCategory.NOTICE, "공지사항 등록", true, true),
    POLL_CREATED(NotificationCategory.POLL, "투표 생성", true, true),
    POLL_DEADLINE_APPROACHING(NotificationCategory.POLL, "투표 마감 전 알림", true, true),
    POLL_REMINDER(NotificationCategory.POLL, "투표 미참여 리마인드", true, true),
    POLL_COMPLETED(NotificationCategory.POLL, "투표 완료", true, true),
    MEETIT_CREATED(NotificationCategory.MEETIT, "새로운 밋잇 생성", true, true),
    MEETIT_REMINDER(NotificationCategory.MEETIT, "밋잇 미참여 리마인드", true, true),
    MEETIT_COMPLETED(NotificationCategory.MEETIT, "밋잇 참여 완료", true, true),

    COMMENT_CREATED(NotificationCategory.POST, "새로운 댓글", true, true),
    COMMENT_MENTIONED(NotificationCategory.POST, "댓글 멘션", true, true),
    POST_LIKED(NotificationCategory.POST, "게시글 좋아요!", true, true),

    // 7. 채팅
    CHAT_MESSAGE(NotificationCategory.CHAT, "채팅", true, false)
}

data class NotificationMessage(
    val type: NotificationType,
    val category: NotificationCategory,
    val title: String,
    val content: String,
    val pushText: String,
    val directTo: String?,
    val targetId: Long? = null,
    val isPush: Boolean = type.isPush,
    val isToast: Boolean = type.isToast
)


object NotificationTemplate {

    // 2-1. 멤버 가입 (수신: 운영진 / direct to: 멤버 목록 페이지)
    fun memberJoin(teamName: String, name: String): NotificationMessage {
        val title = "$teamName 멤버 가입"
        val content = "[$teamName] ${name}님이 새로 합류했습니다."
        return NotificationMessage(
            type = NotificationType.MEMBER_JOIN,
            category = NotificationCategory.TEAM,
            title = title,
            content = content,
            pushText = content,
            directTo = "/teams/members"
        )
    }

    // 2-2. 멤버 탈퇴 (수신: 운영진 / direct to: 멤버 목록 페이지)
    fun memberLeave(teamName: String, name: String): NotificationMessage {
        val title = "$teamName 멤버 탈퇴"
        val content = "[$teamName] ${name}님이 팀을 나갔습니다."
        return NotificationMessage(
            type = NotificationType.MEMBER_LEAVE,
            category = NotificationCategory.TEAM,
            title = title,
            content = content,
            pushText = content,
            directTo = "/teams/members"
        )
    }

    // 3-1. 파트 배정받았을 때 (수신: 파트 배정 대상자 / direct to: 멤버 목록 페이지)
    fun partAssigned(teamName: String, part: String): NotificationMessage {
        val title = "$teamName 파트 배정"
        val content = "[$teamName] $part 파트로 배정되었습니다. 내 파트를 확인해 보세요!"
        return NotificationMessage(
            type = NotificationType.PART_ASSIGNED,
            category = NotificationCategory.TEAM,
            title = title,
            content = content,
            pushText = content,
            directTo = "/teams/members"
        )
    }

    // 3-2. 새로운 합주실/연습실 등록됐을 때 (수신: 팀원 전원 / direct to: 해당 합주실/연습실 글)
    fun studioRegistered(teamName: String, studioName: String, studioId: Long? = null): NotificationMessage {
        val title = "$teamName 합주실/연습실 등록"
        val content = "[$teamName] 새로운 연습실(${studioName})이 등록되었습니다. 사용 후기를 남겨보세요!"
        return NotificationMessage(
            type = NotificationType.STUDIO_REGISTERED,
            category = NotificationCategory.TEAM,
            title = title,
            content = content,
            pushText = content,
            directTo = studioId?.let { "/studios/$it" } ?: "/studios",
            targetId = studioId
        )
    }

    // 3-2-1. 새로운 아카이브 등록됐을 때 (수신: 팀원 전원 / direct to: 해당 아카이브 글)
    fun archiveRegistered(teamName: String, archiveTitle: String, archiveId: Long? = null): NotificationMessage {
        val title = "$teamName 아카이브 등록"
        val content = "[$teamName] 새로운 아카이브(${archiveTitle})가 등록되었습니다."
        return NotificationMessage(
            type = NotificationType.STUDIO_REGISTERED,
            category = NotificationCategory.TEAM,
            title = title,
            content = content,
            pushText = content,
            directTo = archiveId?.let { "/archives/$it" } ?: "/archives",
            targetId = archiveId
        )
    }

    // 5-1. 일정 등록 (수신: 해당 일정 참여자 (작성자 제외) / direct to: 해당 일정)
    fun scheduleRegistered(teamName: String, scheduleTitle: String, scheduleId: Long? = null): NotificationMessage {
        val title = "$teamName 일정 등록"
        val content = "[$teamName] '$scheduleTitle' 일정이 등록되었습니다."
        return NotificationMessage(
            type = NotificationType.SCHEDULE_REGISTERED,
            category = NotificationCategory.CALENDAR,
            title = title,
            content = content,
            pushText = content,
            directTo = scheduleId?.let { "/schedules/$it" } ?: "/schedules",
            targetId = scheduleId
        )
    }

    // 5-2. 일정 d-1 (수신: 해당 일정 참여자 / direct to: 해당 일정)
    fun scheduleReminderD1(teamName: String, scheduleTitle: String, scheduleId: Long? = null): NotificationMessage {
        val title = "$teamName 일정 알림"
        val content = "[$teamName] 내일은 '$scheduleTitle'이 있는 날입니다!"
        return NotificationMessage(
            type = NotificationType.SCHEDULE_REMINDER,
            category = NotificationCategory.CALENDAR,
            title = title,
            content = content,
            pushText = content,
            directTo = scheduleId?.let { "/schedules/$it" } ?: "/schedules",
            targetId = scheduleId
        )
    }

    // 6-1-1. 공지사항 등록 (수신: 팀원 전원 (작성자 제외) / direct to: 해당 공지사항)
    // 인앱 알림 UI 예시: title: "{teamName} 공지사항 등록", content: "{name}님이 공지사항을 등록했습니다."
    fun noticeRegistered(
        teamName: String,
        noticeTitle: String,
        authorName: String? = null,
        noticeId: Long? = null
    ): NotificationMessage {
        val title = "$teamName 공지사항 등록"
        val inAppContent = authorName?.let { "${it}님이 공지사항을 등록했습니다." }
            ?: "[$teamName] 새 공지사항이 등록되었습니다: $noticeTitle"
        val pushContent = "[$teamName] 새 공지사항이 등록되었습니다: $noticeTitle"
        return NotificationMessage(
            type = NotificationType.NOTICE_REGISTERED,
            category = NotificationCategory.NOTICE,
            title = title,
            content = inAppContent,
            pushText = pushContent,
            directTo = noticeId?.let { "/posts/notices/$it" } ?: "/posts/notices",
            targetId = noticeId
        )
    }

    // 게시글/공지 좋아요 반응 (수신: 글 작성자 / direct to: 해당 글)
    // 인앱 알림 UI 예시: title: "게시글 좋아요!", content: "{name}님이 '{title}'에 좋아요를 눌렀습니다."
    fun postLiked(
        teamName: String,
        likerName: String,
        postTitle: String,
        postId: Long? = null
    ): NotificationMessage {
        val title = "게시글 좋아요!"
        val content = "${likerName}님이 '$postTitle'에 좋아요를 눌렀습니다."
        val pushContent = "[$teamName] $content"
        return NotificationMessage(
            type = NotificationType.POST_LIKED,
            category = NotificationCategory.POST,
            title = title,
            content = content,
            pushText = pushContent,
            directTo = postId?.let { "/posts/$it" } ?: "/posts",
            targetId = postId
        )
    }

    // 6-2-1. 투표 생성 (수신: 투표 대상자 (작성자 제외) / direct to: 해당 투표)
    fun pollCreated(teamName: String, pollTitle: String, pollId: Long? = null): NotificationMessage {
        val title = "$teamName 투표 생성"
        val content = "[$teamName] 새로운 투표(${pollTitle})가 생성되었습니다. 투표에 참여해 보세요!"
        return NotificationMessage(
            type = NotificationType.POLL_CREATED,
            category = NotificationCategory.POLL,
            title = title,
            content = content,
            pushText = content,
            directTo = pollId?.let { "/posts/polls/$it" } ?: "/posts/polls",
            targetId = pollId
        )
    }

    // 6-2-2. 투표 마감 전 알림 (수신: 투표 미참여자 / direct to: 해당 투표)
    // remainingText: "하루 전" 또는 "1시간 전"
    fun pollDeadlineApproaching(
        teamName: String,
        pollTitle: String,
        remainingText: String,
        pollId: Long? = null
    ): NotificationMessage {
        val title = "$teamName 투표 마감 알림"
        val content = "[$teamName] '$pollTitle' 투표 마감 [${remainingText}]입니다. 투표에 참여해 보세요!"
        return NotificationMessage(
            type = NotificationType.POLL_DEADLINE_APPROACHING,
            category = NotificationCategory.POLL,
            title = title,
            content = content,
            pushText = content,
            directTo = pollId?.let { "/posts/polls/$it" } ?: "/posts/polls",
            targetId = pollId
        )
    }

    // 6-2-3. 투표 미참여 리마인드 (수신: 투표 대상자 중 미참여자 / direct to: 해당 투표)
    fun pollReminder(teamName: String, pollTitle: String, pollId: Long? = null): NotificationMessage {
        val title = "$teamName 투표 미참여 알림"
        val content = "[$teamName] 아직 참여하지 않은 투표(${pollTitle})가 있습니다. 지금 투표에 참여해 보세요!"
        return NotificationMessage(
            type = NotificationType.POLL_REMINDER,
            category = NotificationCategory.POLL,
            title = title,
            content = content,
            pushText = content,
            directTo = pollId?.let { "/posts/polls/$it" } ?: "/posts/polls",
            targetId = pollId
        )
    }

    // 6-2-4. 전원 투표 완료 때 (수신: 투표 생성자 / direct to: 해당 투표)
    fun pollCompleted(teamName: String, pollTitle: String, pollId: Long? = null): NotificationMessage {
        val title = "$teamName 투표 완료"
        val content = "[$teamName] '$pollTitle' 투표의 모든 참여자가 투표를 완료했습니다. 결과를 확인해 보세요!"
        return NotificationMessage(
            type = NotificationType.POLL_COMPLETED,
            category = NotificationCategory.POLL,
            title = title,
            content = content,
            pushText = content,
            directTo = pollId?.let { "/posts/polls/$it" } ?: "/posts/polls",
            targetId = pollId
        )
    }

    // 6-3-1. [공지/투표/아카이브] 댓글 작성할 때 (수신: 글 작성자 (본인 댓글 제외) / direct to: 해당 글)
    fun commentCreated(
        teamName: String,
        postTitle: String,
        postId: Long? = null,
        directTo: String? = null
    ): NotificationMessage {
        val title = "$teamName 새 댓글"
        val content = "[$teamName] 내 글(${postTitle})에 새로운 댓글이 달렸습니다."
        return NotificationMessage(
            type = NotificationType.COMMENT_CREATED,
            category = NotificationCategory.POST,
            title = title,
            content = content,
            pushText = content,
            directTo = directTo ?: (postId?.let { "/posts/$it" } ?: "/posts"),
            targetId = postId
        )
    }

    // 6-3-1-1. [공지/투표/아카이브] 답글 작성할 때 (수신: 원댓글 작성자 (본인 답글 제외) / direct to: 해당 글)
    fun replyCreated(
        teamName: String,
        postTitle: String? = null,
        postId: Long? = null,
        directTo: String? = null
    ): NotificationMessage {
        val title = "$teamName 새 답글"
        val content = if (!postTitle.isNullOrBlank()) {
            "[$teamName] '$postTitle'의 내 댓글에 새로운 답글이 달렸습니다."
        } else {
            "[$teamName] 내 댓글에 새로운 답글이 달렸습니다."
        }
        return NotificationMessage(
            type = NotificationType.COMMENT_CREATED,
            category = NotificationCategory.POST,
            title = title,
            content = content,
            pushText = content,
            directTo = directTo ?: (postId?.let { "/posts/$it" } ?: "/posts"),
            targetId = postId
        )
    }

    // 6-3-2. [공지/투표/아카이브] 댓글에서 언급되었을 때 (수신: 언급 대상자 / direct to: 해당 글)
    fun commentMentioned(
        teamName: String,
        mentionerName: String,
        postTitle: String? = null,
        postId: Long? = null,
        directTo: String? = null
    ): NotificationMessage {
        val title = "$teamName 댓글 언급"
        val content = "[$teamName] ${mentionerName}님이 댓글에서 회원님을 언급했습니다."
        return NotificationMessage(
            type = NotificationType.COMMENT_MENTIONED,
            category = NotificationCategory.POST,
            title = title,
            content = content,
            pushText = content,
            directTo = directTo ?: (postId?.let { "/posts/$it" } ?: "/posts"),
            targetId = postId
        )
    }

    // 6-4-1. 밋잇 생성 (수신: 밋잇 참여 대상자 (작성자 제외) / direct to: 해당 밋잇)
    fun meetitCreated(teamName: String, meetitTitle: String, meetitId: Long? = null): NotificationMessage {
        val title = "$teamName 새로운 밋잇 생성"
        val content = "[$teamName] 새로운 밋잇(${meetitTitle})이 열렸습니다. 가능한 일정을 등록해 보세요!"
        return NotificationMessage(
            type = NotificationType.MEETIT_CREATED,
            category = NotificationCategory.MEETIT,
            title = title,
            content = content,
            pushText = content,
            directTo = meetitId?.let { "/posts/meetits/$it" } ?: "/posts/meetits",
            targetId = meetitId
        )
    }

    // 6-4-2. 밋잇 미참여 리마인드 (수신: 밋잇 참여 대상자 중 미참여자 / direct to: 해당 밋잇)
    fun meetitReminder(teamName: String, meetitTitle: String, meetitId: Long? = null): NotificationMessage {
        val title = "$teamName 밋잇 미참여 알림"
        val content = "[$teamName] 아직 작성하지 않은 밋잇(${meetitTitle})이 있습니다. 가능한 일정을 등록해 보세요!"
        return NotificationMessage(
            type = NotificationType.MEETIT_REMINDER,
            category = NotificationCategory.MEETIT,
            title = title,
            content = content,
            pushText = content,
            directTo = meetitId?.let { "/posts/meetits/$it" } ?: "/posts/meetits",
            targetId = meetitId
        )
    }

    // 6-4-3. 전원 참여 완료했을 때 (수신: 밋잇 생성자 / direct to: 해당 밋잇)
    fun meetitCompleted(teamName: String, meetitTitle: String? = null, meetitId: Long? = null): NotificationMessage {
        val title = "$teamName 밋잇 작성 완료"
        val content = if (!meetitTitle.isNullOrBlank()) {
            "[$teamName] '$meetitTitle' 밋잇의 모든 참여자가 작성을 완료했습니다. 모임 가능 시간을 확인해 보세요!"
        } else {
            "[$teamName] 모든 참여자가 밋잇 작성을 완료했습니다. 모임 가능 시간을 확인해 보세요!"
        }
        return NotificationMessage(
            type = NotificationType.MEETIT_COMPLETED,
            category = NotificationCategory.MEETIT,
            title = title,
            content = content,
            pushText = content,
            directTo = meetitId?.let { "/posts/meetits/$it" } ?: "/posts/meetits",
            targetId = meetitId
        )
    }

    // 7-1. 채팅 (수신: 채팅방 참여자 / direct to: 해당 채팅방 / toast: false)
    fun chatMessage(
        teamName: String,
        chatRoomName: String,
        senderName: String,
        message: String,
        chatRoomId: Long? = null
    ): NotificationMessage {
        val title = chatRoomName
        val content = "[$teamName / $chatRoomName] $senderName: $message"
        return NotificationMessage(
            type = NotificationType.CHAT_MESSAGE,
            category = NotificationCategory.CHAT,
            title = title,
            content = content,
            pushText = content,
            directTo = chatRoomId?.let { "/chats/$it" } ?: "/chats",
            targetId = chatRoomId,
            isPush = true,
            isToast = false
        )
    }
}
