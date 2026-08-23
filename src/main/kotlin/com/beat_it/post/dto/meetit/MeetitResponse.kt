package com.beat_it.post.dto.meetit

import java.time.OffsetDateTime

data class MeetitListResponse(
    val meetitList: List<MeetitListItemResponse>,
    val totalCount: Int,
    val hasNext: Boolean
)

enum class MyResponseStatus {
    RESPONDED, NOT_RESPONDED, NOT_PARTICIPANT
}

data class MeetitListItemResponse(
    val meetitId: Long,
    val title: String,
    val totalInvitedCount: Int,
    val respondedCount: Int,
    val myResponseStatus: MyResponseStatus
)

data class MeetitDetailResponse(
    val meetitId: Long,
    val title: String,
    val creatorId: Long,
    val startTime: String, // "HH:mm"
    val endTime: String, // "HH:mm"
    val candidateDates: List<String>,
    val totalInvitedCount: Int,
    val respondedCount: Int,
    val participants: List<MeetitParticipantInfoResponse>,
    val entireMemberOptimalSlots: List<MeetitOptimalSlotResponse>,
    val maxMemberOptimalSlots: List<MeetitOptimalSlotResponse>,
    val maxOverlappingCount: Int,
    val timetableGrid: List<MeetitGridSlotResponse>
)

data class MeetitParticipantInfoResponse(
    val userId: Long,
    val name: String,
    val profileImageUrl: String,
    val hasResponded: Boolean
)

data class MeetitOptimalSlotResponse(
    val date: String, // "yyyy-MM-dd"
    val startTime: String, // "HH:mm"
    val endTime: String, // "HH:mm"
    val availableCount: Int,
    val totalInvitedCount: Int
)

data class MeetitGridSlotResponse(
    val slotStartTime: OffsetDateTime,
    val availableUserIds: List<Long>
)
