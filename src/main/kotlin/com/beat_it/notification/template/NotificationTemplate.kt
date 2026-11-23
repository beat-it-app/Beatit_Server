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

    // 7. 채팅
    CHAT_MESSAGE(NotificationCategory.CHAT, "채팅", true, true)
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
        val title = "[$teamName] $name"
        val content = "팀에 새로 합류했습니다. 환영해 주세요!"
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
        val title = "[$teamName] $name"
        val content = "팀을 탈퇴했습니다."
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
        val title = "[$teamName]"
        val content = "$part 파트로 배정되었습니다. 내 파트를 확인해 보세요!"
        return NotificationMessage(
            type = NotificationType.PART_ASSIGNED,
            category = NotificationCategory.TEAM,
            title = title,
            content = content,
            pushText = content,
            directTo = "/teams/members"
        )
    }

    // 3-1-1. 운영진/대표 권한 변경되었을 때 (수신: 대상자 / direct to: 멤버 목록 페이지)
    fun roleChanged(teamName: String, roleName: String): NotificationMessage {
        val title = "[$teamName]"
        val josa = if (roleName.endsWith("대표")) "로" else "으로"
        val content = "${roleName}${josa} 권한이 변경되었습니다."
        return NotificationMessage(
            type = NotificationType.PART_ASSIGNED,
            category = NotificationCategory.TEAM,
            title = title,
            content = content,
            pushText = content,
            directTo = "/teams/members"
        )
    }

    fun roleChanged(teamName: String, role: com.beat_it.team.entity.enum.TeamRole): NotificationMessage {
        return roleChanged(teamName, role.description)
    }

    // 3-2. 새로운 합주실/연습실 등록됐을 때 (수신: 팀원 전원 / direct to: 해당 합주실/연습실 글)
    fun studioRegistered(teamName: String, studioName: String, studioId: Long? = null): NotificationMessage {
        val title = "[$teamName]"
        val content = "새로운 연습실이 등록되었습니다: $studioName"
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
    fun archiveRegistered(
        teamName: String,
        archiveTitle: String,
        authorName: String? = null,
        archiveId: Long? = null
    ): NotificationMessage {
        val title = authorName?.takeIf { it.isNotBlank() }?.let { "[$teamName] $it" } ?: "[$teamName]"
        val content = "새로운 아카이브가 등록되었습니다: $archiveTitle"
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
        val title = "[$teamName]"
        val content = "새로운 일정이 등록되었습니다: $scheduleTitle"
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

    // 5-1-1. 일정 멤버 추가 (일정 수정 시 새로 추가된 멤버 수신 / direct to: 해당 일정)
    fun scheduleMemberAdded(teamName: String, scheduleTitle: String, scheduleId: Long? = null): NotificationMessage {
        val title = "[$teamName]"
        val content = "'$scheduleTitle' 일정에 참여 멤버로 추가되었습니다."
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
        val title = "[$teamName]"
        val content = "내일은 '$scheduleTitle' 일정이 있는 날입니다!"
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
    fun noticeRegistered(
        teamName: String,
        noticeTitle: String,
        authorName: String? = null,
        noticeId: Long? = null
    ): NotificationMessage {
        val title = authorName?.takeIf { it.isNotBlank() }?.let { "[$teamName] $it" } ?: "[$teamName]"
        val content = "새로운 공지사항이 등록되었습니다: $noticeTitle"
        return NotificationMessage(
            type = NotificationType.NOTICE_REGISTERED,
            category = NotificationCategory.NOTICE,
            title = title,
            content = content,
            pushText = content,
            directTo = noticeId?.let { "/posts/notices/$it" } ?: "/posts/notices",
            targetId = noticeId
        )
    }

    // 6-2-1. 투표 생성 (수신: 투표 대상자 (작성자 제외) / direct to: 해당 투표)
    fun pollCreated(
        teamName: String,
        pollTitle: String,
        creatorName: String? = null,
        pollId: Long? = null
    ): NotificationMessage {
        val title = "[$teamName]"
        val content = "새로운 투표가 생성되었습니다: $pollTitle"
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
    // remainingText: "24시간 전" 또는 "1시간 전"
    fun pollDeadlineApproaching(
        teamName: String,
        pollTitle: String,
        remainingText: String,
        pollId: Long? = null
    ): NotificationMessage {
        val title = "[$teamName]"
        val suffix = if (remainingText.endsWith("전")) "" else " 전"
        val content = "'$pollTitle' 투표 마감 $remainingText${suffix}입니다. 투표에 참여해 보세요!"
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
        val title = "[$teamName]"
        val content = "아직 참여하지 않은 투표가 있습니다: $pollTitle"
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
        val title = "[$teamName]"
        val content = "'$pollTitle' 투표의 모든 참여자가 투표를 완료했습니다. 결과를 확인해 보세요!"
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
        postTitle: String? = null,
        writerName: String? = null,
        commentContent: String? = null,
        postId: Long? = null,
        directTo: String? = null
    ): NotificationMessage {
        val title = writerName?.takeIf { it.isNotBlank() }?.let { "[$teamName] $it" } ?: "[$teamName]"
        val content = if (!commentContent.isNullOrBlank()) {
            "댓글을 남겼습니다: $commentContent"
        } else if (!postTitle.isNullOrBlank()) {
            "'$postTitle' 글에 새로운 댓글이 달렸습니다."
        } else {
            "내 글에 새로운 댓글이 달렸습니다."
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

    // 6-3-1-1. [공지/투표/아카이브] 답글 작성할 때 (수신: 원댓글 작성자 (본인 답글 제외) / direct to: 해당 글)
    fun replyCreated(
        teamName: String,
        postTitle: String? = null,
        writerName: String? = null,
        replyContent: String? = null,
        postId: Long? = null,
        directTo: String? = null
    ): NotificationMessage {
        val title = writerName?.takeIf { it.isNotBlank() }?.let { "[$teamName] $it" } ?: "[$teamName]"
        val content = if (!replyContent.isNullOrBlank()) {
            "답글을 남겼습니다: $replyContent"
        } else if (!postTitle.isNullOrBlank()) {
            "'$postTitle'의 내 댓글에 새로운 답글이 달렸습니다."
        } else {
            "내 댓글에 새로운 답글이 달렸습니다."
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
        commentContent: String? = null,
        postTitle: String? = null,
        postId: Long? = null,
        directTo: String? = null
    ): NotificationMessage {
        val title = "[$teamName] $mentionerName"
        val content = if (!commentContent.isNullOrBlank()) {
            "댓글에서 회원님을 언급했습니다: $commentContent"
        } else {
            "댓글에서 회원님을 언급했습니다."
        }
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
    fun meetitCreated(
        teamName: String,
        meetitTitle: String,
        creatorName: String? = null,
        meetitId: Long? = null
    ): NotificationMessage {
        val title = "[$teamName]"
        val content = "새로운 밋잇이 생성되었습니다: $meetitTitle"
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
        val title = "[$teamName]"
        val content = "아직 참여하지 않은 밋잇이 있습니다: $meetitTitle"
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
        val title = "[$teamName]"
        val content = if (!meetitTitle.isNullOrBlank()) {
            "'$meetitTitle' 밋잇의 모든 참여자가 일정을 등록했습니다. 모임 시간을 확인해 보세요!"
        } else {
            "모든 멤버가 일정을 등록했습니다. 모임 시간을 확인해 보세요!"
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

    // 7-1. 채팅 (수신: 채팅방 참여자 / direct to: 해당 채팅방 / toast: true)
    fun chatMessage(
        teamName: String,
        chatRoomName: String?,
        senderName: String,
        message: String,
        chatRoomId: Long? = null
    ): NotificationMessage {
        val title = "[$teamName] $senderName"
        val content = if (!chatRoomName.isNullOrBlank()) {
            "[$chatRoomName] $message"
        } else {
            message
        }
        return NotificationMessage(
            type = NotificationType.CHAT_MESSAGE,
            category = NotificationCategory.CHAT,
            title = title,
            content = content,
            pushText = content,
            directTo = chatRoomId?.let { "/chats/$it" } ?: "/chats",
            targetId = chatRoomId,
            isPush = true,
            isToast = true
        )
    }
}
