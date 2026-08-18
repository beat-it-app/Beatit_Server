package com.beat_it.location.dto

import com.beat_it.location.entity.Locations
import java.math.BigDecimal
import java.util.UUID

data class LocationResponse(
    val locationId: Long,
    val publicId: UUID,
    val locationName: String?,
    val roadAddress: String?,
    val detailAddress: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val mapUrl: String?
) {
    companion object {
        fun from(location: Locations): LocationResponse {
            return LocationResponse(
                locationId = location.locationId!!,
                publicId = location.publicId,
                locationName = location.locationName,
                roadAddress = location.roadAddress,
                detailAddress = location.detailAddress,
                latitude = location.latitude,
                longitude = location.longitude,
                mapUrl = location.mapUrl
            )
        }
    }
}
