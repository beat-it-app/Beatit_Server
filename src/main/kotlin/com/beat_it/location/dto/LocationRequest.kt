package com.beat_it.location.dto

import java.math.BigDecimal

data class LocationRequest(
    val locationName: String?,
    val roadAddress: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val mapUrl: String?,
    val phone: String?,
    val kakaoPlaceId: String?,
    val jibunAddress: String?
)
