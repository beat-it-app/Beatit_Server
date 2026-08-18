package com.beat_it.location.dto

import java.math.BigDecimal

data class LocationSearchResponse(
    val locationName: String,
    val roadAddress: String,
    val detailAddress: String?,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val mapUrl: String
)
