package com.beat_it.team.dto

import com.beat_it.team.entity.enum.PlatformCode
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime

data class TeamDetailResponse(
    @JsonProperty("team_id") val teamId: Long? = null,
    @JsonProperty("profile_image_url") val profileImageUrl: String?,
    @JsonProperty("name") val teamName: String,
    val description: String?,
    @JsonProperty("established_on") val establishedOn: LocalDate?,
    @JsonProperty("invite_code") val inviteCode: String,
    @JsonProperty("member_count") val memberCount: Int,
    @JsonProperty("created_at") val createdAt: OffsetDateTime,
    @JsonProperty("updated_at") val updatedAt: OffsetDateTime,
    val links: List<LinksResponse>,
    val parts: List<PartsResponse>,
    @JsonProperty("archive_count") val archiveCount: Int,
    @JsonProperty("cloud_item_count") val cloudItemCount: Int,

)

data class LinksResponse(
    @JsonProperty("team_link_id") val teamLinkId: Long,
    @JsonProperty("platform_code") val platFormCode: PlatformCode,
    @JsonProperty("link_url") val linkUrl: String,
)

data class PartsResponse(
    @JsonProperty("team_part_id") val teamPartId: Long,
    @JsonProperty("part_name") val partName: String,
    @JsonProperty("display_order") val displayOrder: Int,
)