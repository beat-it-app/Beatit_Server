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
    val myResponseStatus: MyResponseStatus,
    val dateOnly: Boolean
)

data class MeetitDetailResponse(
    val meetitId: Long,
    val title: String,
    val creatorId: Long,
    val startTime: String?, // "HH:mm"
    val endTime: String?, // "HH:mm"
    val dateOnly: Boolean,
    val candidateDates: List<String>,
    val totalInvitedCount: Int,
    val respondedCount: Int,
    val isParticipant: Boolean,
    val respondedParticipants: List<MeetitParticipantInfoResponse>,
    val entireMemberOptimalSlots: List<MeetitOptimalSlotResponse>,
    val maxMemberOptimalSlots: List<MeetitOptimalSlotResponse>,
    val maxOverlappingCount: Int,
    val timetableGrid: List<MeetitGridSlotResponse>
)

data class MeetitParticipantInfoResponse(
    val userId: Long,
    val name: String
)

data class MeetitOptimalSlotResponse(
    val date: String, // "yyyy-MM-dd"
    val startTime: String? = null, // "HH:mm"
    val endTime: String? = null // "HH:mm"
)

data class MeetitGridSlotResponse(
    val slotStartTime: OffsetDateTime,
    val availableUserIds: List<Long>
)
