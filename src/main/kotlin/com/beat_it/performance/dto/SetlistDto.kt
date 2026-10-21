package com.beat_it.performance.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "셋리스트 음원 정보 DTO")
data class SetlistMusicDto(
    @field:NotBlank(message = "음원 제목은 필수입니다.")
    @JsonProperty("musicTitle")
    @JsonAlias("title")
    @Schema(description = "음원 제목", example = "Basket case")
    val musicTitle: String = "",

    @field:NotBlank(message = "아티스트명은 필수입니다.")
    @JsonProperty("musicArtist")
    @JsonAlias("artist")
    @Schema(description = "아티스트명", example = "Green day")
    val musicArtist: String = "",

    @JsonProperty("musicPreviewUrl")
    @JsonAlias("previewUrl")
    @Schema(description = "음원 미리듣기 URL (선택)", example = "https://audio-ssl.itunes.apple.com/...")
    val musicPreviewUrl: String? = null,

    @JsonProperty("musicImageUrl")
    @JsonAlias("imageUrl")
    @Schema(description = "음원 앨범 커버 이미지 URL (선택)", example = "https://is1-ssl.mzstatic.com/...")
    val musicImageUrl: String? = null,

    @field:Size(max = 50, message = "음원 설명은 최대 50자까지 입력 가능합니다.")
    @JsonProperty("description")
    @Schema(description = "음원 설명 (50자 제한, 선택)", example = "참여자: 노영서, 김지원, 송하은")
    val description: String? = null
)

@Schema(description = "셋리스트 팀 정보 DTO")
data class SetlistTeamDto(
    @JsonProperty("teamName")
    @Schema(description = "팀 이름 (팀이 1개일 때는 선택, 2개 이상일 때는 필수)", example = "알사탕")
    val teamName: String? = null,

    @JsonProperty("musics")
    @JsonAlias("songs")
    @Schema(description = "음원 목록 (셋리스트 등록 시 음원 필수)")
    val musics: List<SetlistMusicDto> = emptyList()
)

@Schema(description = "모바일 초대장 요청 DTO (공연 생성 3단계)")
data class InvitationCardRequest(
    @field:NotBlank(message = "공연 팀은 필수 입력값입니다.")
    @Schema(description = "공연 팀", example = "알사탕, AB")
    val performanceTeam: String = "",

    @field:Size(max = 150, message = "상세 소개는 최대 150자까지 입력 가능합니다.")
    @Schema(description = "상세 소개 (150자 제한, 선택)", example = "대학생 세 팀이 모여서 청춘을 노래하는 밴드 연주를 시작합니다! 많이 모여서 즐겨주세요.")
    val description: String? = null,

    @Schema(description = "셋리스트 팀 목록")
    val setlist: List<SetlistTeamDto>? = null
)
