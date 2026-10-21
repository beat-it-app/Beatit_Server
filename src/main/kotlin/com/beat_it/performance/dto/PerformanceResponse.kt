package com.beat_it.performance.dto

import com.beat_it.location.dto.LocationResponse
import com.beat_it.performance.entity.Performances
import com.beat_it.performance.entity.enum.PerformanceStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Schema(description = "공연 상세/생성/수정 통합 응답 DTO")
data class PerformanceResponse(
    val performanceId: Long,
    val performancePublicId: UUID,

    val teamId: Long,
    val teamName: String?,
    val createdUserId: Long,

    val title: String,
    val description: String?,
    val performanceDateTime: OffsetDateTime,
    val location: LocationResponse?,
    val placeName: String?,
    val poster: PerformanceFileResponse?,
    val prices: List<PerformancePriceDto>,
    val bookingDeadline: OffsetDateTime?,
    val bookingLink: String?,
    val hostName: String,
    val hostContact: String?,
    val hostLink: String?,

    val detailImages: List<PerformanceFileResponse>,
    val detailInfo: String?,

    val performanceStatus: PerformanceStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun of(
            performance: Performances,
            teamName: String? = null,
            locationResponse: LocationResponse?,
            poster: PerformanceFileResponse?,
            detailImages: List<PerformanceFileResponse>,
            prices: List<PerformancePriceDto>,
        ): PerformanceResponse {
            return PerformanceResponse(
                performanceId = performance.performanceId!!,
                performancePublicId = performance.publicId,
                teamId = performance.teamId,
                teamName = teamName,
                createdUserId = performance.createdUserId,
                title = performance.title,
                performanceDateTime = performance.performanceDateTime,
                location = locationResponse,
                placeName = performance.placeName,
                poster = poster,
                detailImages = detailImages,
                description = performance.description,
                detailInfo = performance.detailInfo,
                bookingDeadline = performance.bookingDeadline,
                bookingLink = performance.bookingLink,
                hostName = performance.hostName,
                hostContact = performance.hostContact,
                hostLink = performance.hostLink,
                prices = prices,
                performanceStatus = performance.performanceStatus,
                createdAt = performance.createdAt,
                updatedAt = performance.updatedAt
            )
        }
    }
}
