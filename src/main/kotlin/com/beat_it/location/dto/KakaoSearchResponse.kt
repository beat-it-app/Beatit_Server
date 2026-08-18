package com.beat_it.location.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoSearchResponse(
    @JsonProperty("documents")
    val documents: List<KakaoDocument>
)

data class KakaoDocument(
    @JsonProperty("place_name")
    val placeName: String,
    
    @JsonProperty("road_address_name")
    val roadAddressName: String,
    
    @JsonProperty("address_name")
    val addressName: String,
    
    @JsonProperty("x")
    val x: String,
    
    @JsonProperty("y")
    val y: String,
    
    @JsonProperty("place_url")
    val placeUrl: String
)
