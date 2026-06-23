package com.beat_it.post.dto

import com.beat_it.post.entity.enum.PollType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class PollDetailResponse(
    val pollId: Long,
    val title: String,
    val content: String?,
    val pollType: PollType,
    val allowMultipleChoice: Boolean,
    val isAnonymous: Boolean,
    val closesAt: String?,
    val remindBeforeClose: String?,
    val writerName: String,
    val writerProfileImageUrl: String,
    val createdAt: String,
    val updatedAt: String,
    val pollItems: List<PollItemResponse>,
    val isWriter: Boolean,
    val commentCount: Int,
    val commentList: List<CommentResponse>
)

// --- 응답용 투표 아이템 다형성 처리 ---
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "pollType",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TextItemResponse::class, name = "TEXT"),
    JsonSubTypes.Type(value = DateItemResponse::class, name = "DATE"),
    JsonSubTypes.Type(value = MusicItemResponse::class, name = "MUSIC"),
    JsonSubTypes.Type(value = LocationItemResponse::class, name = "LOCATION")
)
interface PollItemResponse {
    val itemId: Long       // 투표할 때 어떤 항목을 찍었는지 ID가 필요합니다.
    val voteCount: Int     // 이 항목의 총 득표수
    val isVoted: Boolean   // 현재 로그인한 유저가 이 항목에 투표했는지 여부
}

data class TextItemResponse(
    override val itemId: Long,
    override val voteCount: Int,
    override val isVoted: Boolean,
    val content: String
) : PollItemResponse

data class DateItemResponse(
    override val itemId: Long,
    override val voteCount: Int,
    override val isVoted: Boolean,
    val date: String // 포맷팅된 날짜 문자열 혹은 OffsetDateTime
) : PollItemResponse

data class MusicItemResponse(
    override val itemId: Long,
    override val voteCount: Int,
    override val isVoted: Boolean,
    val music: String
) : PollItemResponse

data class LocationItemResponse(
    override val itemId: Long,
    override val voteCount: Int,
    override val isVoted: Boolean,
    val location: String
) : PollItemResponse