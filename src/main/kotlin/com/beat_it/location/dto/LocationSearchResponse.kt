package com.beat_it.location.dto

import java.math.BigDecimal

data class LocationSearchResponse(
    val locationName: String,
    val roadAddress: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val mapUrl: String,
    val phone: String? = null,
    val kakaoPlaceId: String? = null,
    val jibunAddress: String? = null,
    val distance: String? = null
)
