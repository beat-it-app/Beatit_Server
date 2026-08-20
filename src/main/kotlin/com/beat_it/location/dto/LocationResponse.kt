package com.beat_it.location.dto

import com.beat_it.location.entity.Locations
import java.math.BigDecimal

data class LocationResponse(
    val locationId: Long,
    val locationName: String?,
    val roadAddress: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val mapUrl: String?,
    val phone: String?,
    val kakaoPlaceId: String?,
    val jibunAddress: String?
) {
    companion object {
        fun from(location: Locations): LocationResponse {
            return LocationResponse(
                locationId = location.locationId!!,
                locationName = location.locationName,
                roadAddress = location.roadAddress,
                latitude = location.latitude,
                longitude = location.longitude,
                mapUrl = location.mapUrl,
                phone = location.phone,
                kakaoPlaceId = location.kakaoPlaceId,
                jibunAddress = location.jibunAddress
            )
        }
    }
}
