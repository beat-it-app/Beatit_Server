package com.beat_it.performance.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

@Schema(description = "공연 생성 요청 DTO")
data class PerformanceCreateRequest(
    @field:NotBlank
    @Schema(example = "제 12회 정기 밴드 공연")
    val title: String = "",

    @field:NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(example = "2026-10-15T19:00:00+09:00")
    val performanceDateTime: OffsetDateTime? = null,

    @Schema(example = "1")
    val locationId: Long? = null,

    @Schema(example = "홍대 롤링홀")
    val placeName: String? = null,

    @field:Size(max = 200)
    @Schema(example = "빗잇 밴드의 12번째 정기 라이브 공연입니다.")
    val description: String? = null,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(example = "2026-10-14T23:59:59+09:00")
    val bookingDeadline: OffsetDateTime? = null,

    @Schema(example = "https://booking.example.com/beatit12")
    val bookingLink: String? = null,

    @field:NotBlank(message = "호스트 이름은 필수 입력값입니다.")
    @Schema(example = "홍길동")
    val hostName: String = "",

    @Schema(example = "010-1234-5678")
    val hostContact: String? = null,

    @Schema(example = "https://instagram.com/beatit_band")
    val hostLink: String? = null,

    @Schema(example = "[{\"priceType\": \"ADVANCE\", \"price\": 10000}, {\"priceType\": \"ON_SITE\", \"price\": 15000}]")
    val prices: List<PerformancePriceDto> = emptyList(),

    @field:Size(max = 1000)
    @Schema(example = "입장 안내: 공연 시작 30분 전부터 입장이 가능하며, 음료 반입은 불가합니다.")
    val detailInfo: String? = null,

    @Schema(example = "{\"performanceTeam\": \"알사탕, AB\", \"description\": \"대학생 세 팀이 모여서 청춘을 노래하는 밴드 연주를 시작합니다!\", \"setlist\": [{\"teamName\": \"알사탕\", \"musics\": [{\"musicTitle\": \"Basket case\", \"musicArtist\": \"Green day\", \"description\": \"참여자: 노영서, 김지원, 송하은\"}]}]}")
    val invitationCard: InvitationCardRequest? = null
)
