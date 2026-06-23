package com.beat_it.post.dto

import com.beat_it.post.entity.enum.PollType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime

data class PollRequest(
    val title: String,
    val content: String?,
    val pollType: PollType,
    val pollList: List<PollItem>, // 다형성을 적용한 공통 인터페이스 타입을 사용합니다.
    val allowMultipleChoice: Boolean?,
    val isAnonymous: Boolean?,
    val remindBeforeClose: Boolean?,
    val closesAt: OffsetDateTime?
)

// --- 투표 아이템 다형성 처리 ---

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY, // pollType이 부모(PostRequest)에 있으므로 EXTERNAL_PROPERTY 사용
    property = "pollType",
    visible = true // pollType 변수에도 데이터가 매핑되도록 보존
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TextItem::class, name = "TEXT"),       // PollType.TEXT 일 때
    JsonSubTypes.Type(value = DateItem::class, name = "DATE"),       // PollType.DATE 일 때
    JsonSubTypes.Type(value = MusicItem::class, name = "MUSIC"),     // PollType.MUSIC 일 때
    JsonSubTypes.Type(value = LocationItem::class, name = "LOCATION") // PollType.LOCATION 일 때
)
interface PollItem

data class TextItem(
    val content: String
) : PollItem

data class DateItem(
    val date: OffsetDateTime?
) : PollItem

data class MusicItem(
    val music: String
) : PollItem

data class LocationItem(
    val location: String // 주소 문자열, 카카오맵/구글맵 Place ID, 혹은 좌표(WKT) 등을 받을 수 있습니다.
) : PollItem