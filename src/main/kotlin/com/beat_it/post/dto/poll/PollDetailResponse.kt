package com.beat_it.post.dto.poll

import com.beat_it.post.dto.CommentResponse
import com.beat_it.post.entity.enum.PollType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal

data class PollDetailResponse(
    val pollId: Long,
    val title: String,
    val content: String?,
    val pollType: PollType,
    val allowMultipleChoice: Boolean,
    val isAnonymous: Boolean,
    val closeAt: String?,
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
    JsonSubTypes.Type(value = MusicItemResponse::class, name = "MUSIC"),
    JsonSubTypes.Type(value = LocationItemResponse::class, name = "LOCATION")
)
interface PollItemResponse {
    val itemId: Long
    val voteCount: Int
    val isVoted: Boolean
}

data class TextItemResponse(
    override val itemId: Long,
    override val voteCount: Int,
    override val isVoted: Boolean,
    val content: String
) : PollItemResponse

data class MusicItemResponse(
    override val itemId: Long,
    override val voteCount: Int,
    override val isVoted: Boolean,
    val title: String,
    val artist: String,
    val previewUrl: String?,
    val imageUrl: String?
) : PollItemResponse

data class LocationItemResponse(
    override val itemId: Long,
    override val voteCount: Int,
    override val isVoted: Boolean,
    val location: String,
    val locationId: Long? = null,
    val locationName: String? = null,
    val roadAddress: String? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val mapUrl: String? = null,
    val phone: String? = null,
    val kakaoPlaceId: String? = null,
    val jibunAddress: String? = null
) : PollItemResponse